/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t8rin.imagetoolbox.core.domain.image.editing

/**
 * Authoritative non-destructive edit intent for Reference Lab.
 *
 * Rendered/cached bitmaps are acceleration artifacts. They must never become the
 * only representation of an edit that should survive undo/redo, process death,
 * algorithm upgrades, or full-resolution export.
 */
data class EditRecipe(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val source: EditSourceIdentity = EditSourceIdentity(),
    val operations: List<EditOperation> = emptyList(),
    val masks: List<EditMask> = emptyList(),
    val renditionState: SdrHdrRenditionState = SdrHdrRenditionState(),
    val gainMap: GainMapRecipe? = null
) {

    val isEmpty: Boolean
        get() = operations.isEmpty() && masks.isEmpty() && gainMap == null

    fun operationById(id: String): EditOperation? = operations.firstOrNull { it.id == id }

    fun firstOperationOfType(type: String): EditOperation? = operations.firstOrNull { it.type == type }

    fun append(operation: EditOperation): EditRecipe = copy(
        operations = operations + operation
    )

    fun replace(operation: EditOperation): EditRecipe {
        val index = operations.indexOfFirst { it.id == operation.id }
        if (index < 0) return this

        return copy(
            operations = operations.toMutableList().apply {
                this[index] = operation
            }
        )
    }

    fun remove(operationId: String): EditRecipe = copy(
        operations = operations.filterNot { it.id == operationId }
    )

    fun move(operationId: String, newIndex: Int): EditRecipe {
        val oldIndex = operations.indexOfFirst { it.id == operationId }
        if (oldIndex < 0 || operations.size < 2) return this

        val targetIndex = newIndex.coerceIn(0, operations.lastIndex)
        if (targetIndex == oldIndex) return this

        return copy(
            operations = operations.toMutableList().apply {
                val operation = removeAt(oldIndex)
                add(targetIndex, operation)
            }
        )
    }

    fun markGainMapStale(reason: String): EditRecipe = copy(
        gainMap = gainMap?.copy(
            dependencyState = GainMapDependencyState.Stale,
            staleReason = reason
        )
    )

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        fun empty(source: EditSourceIdentity = EditSourceIdentity()): EditRecipe = EditRecipe(
            source = source
        )
    }
}

/**
 * Identity/fingerprint fields are intentionally optional beyond URI and size so
 * the first recipe version can be created immediately, then strengthened when
 * file metadata/hash information is available.
 */
data class EditSourceIdentity(
    val uri: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val modifiedAtMillis: Long? = null,
    val contentHash: String? = null
)

/**
 * One ordered recipe operation.
 *
 * [type] is the stable semantic operation name. [algorithmId] and
 * [algorithmVersion] pin the exact implementation so improving an algorithm
 * later does not silently reinterpret old recipes.
 */
data class EditOperation(
    val id: String,
    val type: String,
    val algorithmId: String,
    val algorithmVersion: Int,
    val enabled: Boolean = true,
    val opacity: Double = 1.0,
    val blendMode: EditBlendMode = EditBlendMode.Normal,
    val processingStage: EditProcessingStage = EditProcessingStage.DisplayReferred,
    val parameters: Map<String, EditValue> = emptyMap(),
    val maskId: String? = null,
    val losslessIntent: LosslessIntent = LosslessIntent.NotRequired
) {
    init {
        require(id.isNotBlank()) { "Edit operation id must not be blank" }
        require(type.isNotBlank()) { "Edit operation type must not be blank" }
        require(algorithmId.isNotBlank()) { "Edit operation algorithmId must not be blank" }
        require(algorithmVersion > 0) { "Edit operation algorithmVersion must be positive" }
        require(opacity in 0.0..1.0) { "Edit operation opacity must be in [0, 1]" }
    }
}

/** Canonical masks live in source-normalized coordinates unless explicitly versioned otherwise. */
data class EditMask(
    val id: String,
    val type: String,
    val algorithmId: String,
    val algorithmVersion: Int,
    val coordinateSpace: EditCoordinateSpace = EditCoordinateSpace.SourceNormalized,
    val parameters: Map<String, EditValue> = emptyMap()
) {
    init {
        require(id.isNotBlank()) { "Edit mask id must not be blank" }
        require(type.isNotBlank()) { "Edit mask type must not be blank" }
        require(algorithmId.isNotBlank()) { "Edit mask algorithmId must not be blank" }
        require(algorithmVersion > 0) { "Edit mask algorithmVersion must be positive" }
    }
}

/**
 * Generic typed parameter tree kept independent from Android/UI types so recipe
 * state can later be serialized, migrated, sidecar-exported, and tested.
 */
sealed interface EditValue {
    data class Decimal(val value: Double) : EditValue
    data class Integer(val value: Long) : EditValue
    data class Flag(val value: Boolean) : EditValue
    data class Text(val value: String) : EditValue
    data class ListValue(val value: List<EditValue>) : EditValue
    data class ObjectValue(val value: Map<String, EditValue>) : EditValue
    data object Null : EditValue
}

enum class EditBlendMode {
    Normal,
    Luminosity,
    Color,
    Hue,
    Saturation
}

/**
 * Declares the representation in which an operation is mathematically defined.
 * This prevents accidentally running a scene-linear operation on display-coded
 * values or vice versa.
 */
enum class EditProcessingStage {
    Encoded,
    SceneReferredLinear,
    DisplayReferred,
    Perceptual,
    Geometry
}

enum class EditCoordinateSpace {
    SourceNormalized
}

enum class LosslessIntent {
    NotRequired,
    PreferPassthrough,
    RequireBitExact
}

/**
 * Dual-rendition state is part of the recipe from schema v1 so Ultra HDR editing
 * never has to bolt an incompatible second state model onto saved edits later.
 */
data class SdrHdrRenditionState(
    val editMode: RenditionEditMode = RenditionEditMode.Linked,
    val hdrMaster: HdrMasterState = HdrMasterState()
)

enum class RenditionEditMode {
    Linked,
    EditSdrPreserveHdr,
    EditHdrPreserveSdr
}

data class HdrMasterState(
    val referenceWhiteNits: Double? = null,
    val targetPeakNits: Double? = null,
    val targetHeadroomRatio: Double? = null,
    val highlightKnee: Double? = null,
    val shoulderStrength: Double? = null,
    val blackFloor: Double? = null,
    val maximumLocalBoostStops: Double? = null,
    val specularEmphasis: Double? = null
)

/** Gain-map creation/edit state owned by the same recipe as normal image edits. */
data class GainMapRecipe(
    val sourceMode: GainMapSourceMode,
    val generatorAlgorithmId: String,
    val generatorAlgorithmVersion: Int,
    val dependencyState: GainMapDependencyState = GainMapDependencyState.Current,
    val staleReason: String? = null,
    val hdrReferenceSource: EditSourceIdentity? = null,
    val mapOperations: List<EditOperation> = emptyList(),
    val metadata: GainMapMetadata = GainMapMetadata(),
    val channelMode: GainMapChannelMode = GainMapChannelMode.Luminance,
    val mapScale: Double? = null
) {
    init {
        require(generatorAlgorithmId.isNotBlank()) { "Gain-map generator algorithmId must not be blank" }
        require(generatorAlgorithmVersion > 0) { "Gain-map generator algorithmVersion must be positive" }
        require(mapScale == null || mapScale > 0.0) { "Gain-map scale must be positive" }
    }
}

enum class GainMapSourceMode {
    GeneratedFromSdr,
    DerivedFromSdrHdrPair,
    ExtractedFromUltraHdr,
    DerivedFromHdr
}

enum class GainMapDependencyState {
    Current,
    Stale
}

enum class GainMapChannelMode {
    Luminance,
    Rgb
}

data class GainMapMetadata(
    val ratioMin: RgbTriple? = null,
    val ratioMax: RgbTriple? = null,
    val gamma: RgbTriple? = null,
    val epsilonSdr: RgbTriple? = null,
    val epsilonHdr: RgbTriple? = null,
    val minDisplayRatioForHdrTransition: Double? = null,
    val displayRatioForFullHdr: Double? = null,
    val direction: GainMapDirection = GainMapDirection.SdrToHdr,
    val alternativeImagePrimaries: String? = null
)

data class RgbTriple(
    val red: Double,
    val green: Double,
    val blue: Double
)

enum class GainMapDirection {
    SdrToHdr,
    HdrToSdr
}

/** Stable semantic names for architecture-significant operations. */
object EditOperationTypes {
    const val Exposure = "exposure"
    const val Brightness = "brightness"
    const val Contrast = "contrast"
    const val Highlights = "highlights"
    const val Shadows = "shadows"
    const val Whites = "whites"
    const val Blacks = "blacks"
    const val Temperature = "temperature"
    const val Tint = "tint"
    const val Saturation = "saturation"
    const val Vibrance = "vibrance"
    const val Structure = "structure"
    const val Sharpening = "sharpening"
    const val ProfileAssign = "profile_assign"
    const val ProfileConvert = "profile_convert"
}
