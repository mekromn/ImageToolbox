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
 * Durable storage boundary for non-destructive Reference Lab edits.
 *
 * Implementations live outside core:domain. A recipe is keyed by its source URI
 * but still carries a stronger source fingerprint so callers can decide whether
 * a stored recipe is current, stale/rebindable, or unsafe to auto-apply.
 */
interface EditRecipeRepository {

    /**
     * Loads the recipe keyed by [source.uri] and compares its recorded source
     * fingerprint with the current [source] identity. Stale work is returned,
     * never silently deleted; callers decide whether to rebind/recover it.
     */
    suspend fun load(source: EditSourceIdentity): StoredEditRecipe?

    suspend fun save(recipe: EditRecipe)

    suspend fun delete(sourceUri: String)

}

/**
 * Loading never silently discards stale work. [sourceMatches] describes whether
 * the caller-provided/current identity matches the identity recorded in recipe.
 */
data class StoredEditRecipe(
    val recipe: EditRecipe,
    val sourceMatches: Boolean
)

fun EditSourceIdentity.matches(other: EditSourceIdentity): Boolean {
    if (uri != other.uri) return false

    val comparablePairs = listOf(
        sizeBytes to other.sizeBytes,
        modifiedAtMillis to other.modifiedAtMillis,
        contentHash to other.contentHash
    )

    if (comparablePairs.any { (left, right) ->
            left != null && right != null && left != right
        }
    ) {
        return false
    }

    if (width > 0 && other.width > 0 && width != other.width) return false
    if (height > 0 && other.height > 0 && height != other.height) return false
    if (mimeType != null && other.mimeType != null && mimeType != other.mimeType) return false

    return true
}
