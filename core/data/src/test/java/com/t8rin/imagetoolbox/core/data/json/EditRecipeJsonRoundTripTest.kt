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

package com.t8rin.imagetoolbox.core.data.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.t8rin.imagetoolbox.core.domain.image.editing.EditBlendMode
import com.t8rin.imagetoolbox.core.domain.image.editing.EditCoordinateSpace
import com.t8rin.imagetoolbox.core.domain.image.editing.EditMask
import com.t8rin.imagetoolbox.core.domain.image.editing.EditOperation
import com.t8rin.imagetoolbox.core.domain.image.editing.EditProcessingStage
import com.t8rin.imagetoolbox.core.domain.image.editing.EditRecipe
import com.t8rin.imagetoolbox.core.domain.image.editing.EditSourceIdentity
import com.t8rin.imagetoolbox.core.domain.image.editing.EditValue
import com.t8rin.imagetoolbox.core.domain.image.editing.GainMapChannelMode
import com.t8rin.imagetoolbox.core.domain.image.editing.GainMapDependencyState
import com.t8rin.imagetoolbox.core.domain.image.editing.GainMapDirection
import com.t8rin.imagetoolbox.core.domain.image.editing.GainMapMetadata
import com.t8rin.imagetoolbox.core.domain.image.editing.GainMapRecipe
import com.t8rin.imagetoolbox.core.domain.image.editing.GainMapSourceMode
import com.t8rin.imagetoolbox.core.domain.image.editing.HdrMasterState
import com.t8rin.imagetoolbox.core.domain.image.editing.LosslessIntent
import com.t8rin.imagetoolbox.core.domain.image.editing.RenditionEditMode
import com.t8rin.imagetoolbox.core.domain.image.editing.RgbTriple
import com.t8rin.imagetoolbox.core.domain.image.editing.SdrHdrRenditionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EditRecipeJsonRoundTripTest {

    private val moshi = Moshi.Builder()
        .add(EditValueJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(EditRecipe::class.java)

    @Test
    fun completeRecipeRoundTripsWithoutLosingTypedIntent() {
        val source = EditSourceIdentity(
            uri = "content://media/external/images/media/42",
            width = 4080,
            height = 3072,
            mimeType = "image/jpeg",
            sizeBytes = 12_345_678,
            modifiedAtMillis = 987_654_321,
            contentHash = "deadbeef"
        )
        val mask = EditMask(
            id = "mask-1",
            type = "brush",
            algorithmId = "reference-lab.brush-mask",
            algorithmVersion = 3,
            coordinateSpace = EditCoordinateSpace.SourceNormalized,
            parameters = mapOf(
                "points" to EditValue.ListValue(
                    listOf(
                        EditValue.ObjectValue(
                            mapOf(
                                "x" to EditValue.Decimal(0.25),
                                "y" to EditValue.Decimal(0.75)
                            )
                        )
                    )
                )
            )
        )
        val operation = EditOperation(
            id = "exposure-1",
            type = "exposure",
            algorithmId = "reference-lab.exposure",
            algorithmVersion = 2,
            enabled = true,
            opacity = 0.85,
            blendMode = EditBlendMode.Luminosity,
            processingStage = EditProcessingStage.SceneReferredLinear,
            parameters = mapOf(
                "ev" to EditValue.Decimal(1.25),
                "iterations" to EditValue.Integer(2),
                "preserveHighlights" to EditValue.Flag(true),
                "label" to EditValue.Text("reference"),
                "optional" to EditValue.Null
            ),
            maskId = mask.id,
            losslessIntent = LosslessIntent.NotRequired
        )
        val recipe = EditRecipe(
            source = source,
            operations = listOf(operation),
            masks = listOf(mask),
            renditionState = SdrHdrRenditionState(
                editMode = RenditionEditMode.EditHdrPreserveSdr,
                hdrMaster = HdrMasterState(
                    referenceWhiteNits = 203.0,
                    targetPeakNits = 1600.0,
                    targetHeadroomRatio = 7.88,
                    maximumLocalBoostStops = 3.0
                )
            ),
            gainMap = GainMapRecipe(
                sourceMode = GainMapSourceMode.DerivedFromSdrHdrPair,
                generatorAlgorithmId = "reference-lab.gain-map-fit",
                generatorAlgorithmVersion = 4,
                dependencyState = GainMapDependencyState.Stale,
                staleReason = "HDR target changed",
                hdrReferenceSource = source.copy(uri = "content://hdr-reference"),
                mapOperations = listOf(
                    operation.copy(
                        id = "gain-map-curve-1",
                        type = "gain_map_curve",
                        algorithmId = "reference-lab.gain-map-curve"
                    )
                ),
                metadata = GainMapMetadata(
                    ratioMin = RgbTriple(1.0, 1.0, 1.0),
                    ratioMax = RgbTriple(8.0, 7.5, 7.0),
                    gamma = RgbTriple(1.0, 1.0, 1.0),
                    epsilonSdr = RgbTriple(0.001, 0.001, 0.001),
                    epsilonHdr = RgbTriple(0.001, 0.001, 0.001),
                    minDisplayRatioForHdrTransition = 1.0,
                    displayRatioForFullHdr = 8.0,
                    direction = GainMapDirection.SdrToHdr,
                    alternativeImagePrimaries = "BT2020"
                ),
                channelMode = GainMapChannelMode.Rgb,
                mapScale = 0.25
            )
        )

        val json = adapter.toJson(recipe)
        val restored = adapter.fromJson(json)

        assertNotNull(restored)
        assertEquals(recipe, restored)
    }
}
