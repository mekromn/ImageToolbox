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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditRecipeTest {

    private val source = EditSourceIdentity(
        uri = "content://media/external/images/media/42",
        width = 4000,
        height = 3000,
        mimeType = "image/jpeg"
    )

    @Test
    fun operationOrderingAndAlgorithmVersionAreStableRecipeState() {
        val exposure = EditOperation(
            id = "exposure-1",
            type = EditOperationTypes.Exposure,
            algorithmId = "reference-lab.exposure",
            algorithmVersion = 1,
            processingStage = EditProcessingStage.SceneReferredLinear,
            parameters = mapOf("ev" to EditValue.Decimal(0.75))
        )
        val contrast = EditOperation(
            id = "contrast-1",
            type = EditOperationTypes.Contrast,
            algorithmId = "reference-lab.contrast",
            algorithmVersion = 2
        )
        val saturation = EditOperation(
            id = "saturation-1",
            type = EditOperationTypes.Saturation,
            algorithmId = "reference-lab.saturation",
            algorithmVersion = 1
        )

        val recipe = EditRecipe.empty(source)
            .append(exposure)
            .append(contrast)
            .append(saturation)
            .move("saturation-1", 1)

        assertEquals(
            listOf("exposure-1", "saturation-1", "contrast-1"),
            recipe.operations.map { it.id }
        )
        assertEquals(2, recipe.operationById("contrast-1")?.algorithmVersion)
    }

    @Test
    fun gainMapCanBeMarkedStaleWithoutDroppingMapEdits() {
        val mapEdit = EditOperation(
            id = "map-curve-1",
            type = "gain_map_curve",
            algorithmId = "reference-lab.gain-map-curve",
            algorithmVersion = 1,
            processingStage = EditProcessingStage.SceneReferredLinear
        )
        val gainMap = GainMapRecipe(
            sourceMode = GainMapSourceMode.GeneratedFromSdr,
            generatorAlgorithmId = "reference-lab.smart-gain-map",
            generatorAlgorithmVersion = 1,
            mapOperations = listOf(mapEdit),
            metadata = GainMapMetadata(
                displayRatioForFullHdr = 4.0
            )
        )

        val stale = EditRecipe.empty(source)
            .copy(gainMap = gainMap)
            .markGainMapStale("SDR rendition changed")

        assertEquals(GainMapDependencyState.Stale, stale.gainMap?.dependencyState)
        assertEquals("SDR rendition changed", stale.gainMap?.staleReason)
        assertEquals(listOf(mapEdit), stale.gainMap?.mapOperations)
    }

    @Test
    fun secondWaveBlendAndLosslessFieldsExistFromSchemaOne() {
        val operation = EditOperation(
            id = "color-1",
            type = "color_mixer",
            algorithmId = "reference-lab.color-mixer",
            algorithmVersion = 1,
            blendMode = EditBlendMode.Color,
            losslessIntent = LosslessIntent.PreferPassthrough
        )

        assertEquals(EditBlendMode.Color, operation.blendMode)
        assertEquals(LosslessIntent.PreferPassthrough, operation.losslessIntent)
        assertEquals(1, EditRecipe.CURRENT_SCHEMA_VERSION)
    }

    @Test
    fun masksDefaultToCanonicalSourceNormalizedCoordinates() {
        val mask = EditMask(
            id = "mask-1",
            type = "brush",
            algorithmId = "reference-lab.brush-mask",
            algorithmVersion = 1
        )

        assertEquals(EditCoordinateSpace.SourceNormalized, mask.coordinateSpace)
    }

    @Test
    fun profileAssignmentAndConversionAreDifferentSemanticOperations() {
        assertNotEquals(EditOperationTypes.ProfileAssign, EditOperationTypes.ProfileConvert)
    }

    @Test
    fun recipeIsEmptyOnlyWhenItContainsNoEditIntent() {
        val empty = EditRecipe.empty(source)
        val withMask = empty.copy(
            masks = listOf(
                EditMask(
                    id = "mask-1",
                    type = "luminance_range",
                    algorithmId = "reference-lab.luminance-mask",
                    algorithmVersion = 1
                )
            )
        )

        assertTrue(empty.isEmpty)
        assertTrue(!withMask.isEmpty)
    }

    @Test
    fun continuousInteractionProducesOneUndoStep() {
        val history = EditRecipeHistory(initialRecipe = EditRecipe.empty(source))
        val operation = EditOperation(
            id = "exposure-1",
            type = EditOperationTypes.Exposure,
            algorithmId = "reference-lab.exposure",
            algorithmVersion = 1,
            processingStage = EditProcessingStage.SceneReferredLinear,
            parameters = mapOf("ev" to EditValue.Decimal(0.0))
        )

        history.beginInteraction()
        history.setRecipe(
            history.recipe.append(operation)
        )
        history.setRecipe(
            history.recipe.replace(
                operation.copy(parameters = mapOf("ev" to EditValue.Decimal(0.5)))
            )
        )
        history.setRecipe(
            history.recipe.replace(
                operation.copy(parameters = mapOf("ev" to EditValue.Decimal(1.0)))
            )
        )
        assertTrue(history.endInteraction())

        assertTrue(history.canUndo)
        assertFalse(history.recipe.isEmpty)
        assertTrue(history.undo())
        assertTrue(history.recipe.isEmpty)
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)
    }

    @Test
    fun undoRedoRestoresWholeRecipeIncludingGainMapDependencyState() {
        val history = EditRecipeHistory(initialRecipe = EditRecipe.empty(source))
        val withGainMap = history.recipe.copy(
            gainMap = GainMapRecipe(
                sourceMode = GainMapSourceMode.GeneratedFromSdr,
                generatorAlgorithmId = "reference-lab.smart-gain-map",
                generatorAlgorithmVersion = 1
            )
        )
        history.setRecipe(withGainMap)
        history.setRecipe(withGainMap.markGainMapStale("Exposure changed"))

        assertEquals(GainMapDependencyState.Stale, history.recipe.gainMap?.dependencyState)
        assertTrue(history.undo())
        assertEquals(GainMapDependencyState.Current, history.recipe.gainMap?.dependencyState)
        assertTrue(history.redo())
        assertEquals(GainMapDependencyState.Stale, history.recipe.gainMap?.dependencyState)
    }

    @Test
    fun cancelInteractionRestoresStartingRecipeWithoutCreatingUndoEntry() {
        val history = EditRecipeHistory(initialRecipe = EditRecipe.empty(source))
        history.beginInteraction()
        history.setRecipe(
            history.recipe.append(
                EditOperation(
                    id = "contrast-1",
                    type = EditOperationTypes.Contrast,
                    algorithmId = "reference-lab.contrast",
                    algorithmVersion = 1
                )
            )
        )

        assertTrue(history.cancelInteraction())
        assertTrue(history.recipe.isEmpty)
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }
}
