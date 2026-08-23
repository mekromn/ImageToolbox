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

package com.t8rin.imagetoolbox.core.data.image

import android.content.Context
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.editing.EditRecipe
import com.t8rin.imagetoolbox.core.domain.image.editing.EditRecipeRepository
import com.t8rin.imagetoolbox.core.domain.image.editing.EditSourceIdentity
import com.t8rin.imagetoolbox.core.domain.image.editing.StoredEditRecipe
import com.t8rin.imagetoolbox.core.domain.image.editing.matches
import com.t8rin.imagetoolbox.core.domain.json.JsonParser
import com.t8rin.imagetoolbox.core.utils.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-private durable recipe storage.
 *
 * Recipes are intentionally separate from preview/cache files. Clearing image
 * caches must not delete user edit intent. Each source URI maps to one stable
 * JSON document; the stronger fingerprint inside the document decides whether
 * it is safe to auto-apply to the current source.
 */
@Singleton
internal class AndroidEditRecipeRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val jsonParser: JsonParser,
    private val dispatchersHolder: DispatchersHolder
) : EditRecipeRepository {

    private val recipeDirectory = File(
        context.filesDir,
        "reference_lab/recipes"
    )

    override suspend fun load(source: EditSourceIdentity): StoredEditRecipe? =
        withContext(dispatchersHolder.ioDispatcher) {
            if (source.uri.isBlank()) return@withContext null

            val file = recipeFile(source.uri)
            if (!file.isFile) return@withContext null

            runCatching {
                val recipe = jsonParser.fromJson<EditRecipe>(
                    json = file.readText(),
                    type = EditRecipe::class.java
                ) ?: return@runCatching null

                StoredEditRecipe(
                    recipe = recipe,
                    sourceMatches = recipe.source.matches(source)
                )
            }.onFailure {
                // Preserve unreadable/stale work for diagnostics/recovery. Never
                // silently delete a user's recipe because a parser failed.
                it.makeLog("AndroidEditRecipeRepository load")
            }.getOrNull()
        }

    override suspend fun save(recipe: EditRecipe): Unit =
        withContext(dispatchersHolder.ioDispatcher) {
            require(recipe.source.uri.isNotBlank()) {
                "Cannot persist an EditRecipe with a blank source URI"
            }

            val json = jsonParser.toJson(
                obj = recipe,
                type = EditRecipe::class.java
            ) ?: error("Could not serialize EditRecipe schema ${recipe.schemaVersion}")

            recipeDirectory.mkdirs()

            val destination = recipeFile(recipe.source.uri)
            val temporary = File(
                recipeDirectory,
                "${destination.name}.tmp"
            )

            try {
                FileOutputStream(temporary).use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                    stream.flush()
                    stream.fd.sync()
                }

                if (!temporary.renameTo(destination)) {
                    // Some Android/filesystem combinations may refuse rename over
                    // an existing target. Fall back to replacement while retaining
                    // the temp-first write that protects against partial JSON.
                    temporary.copyTo(
                        target = destination,
                        overwrite = true
                    )
                    temporary.delete()
                }
            } catch (throwable: Throwable) {
                temporary.delete()
                throw throwable
            }
        }

    override suspend fun delete(sourceUri: String): Unit =
        withContext(dispatchersHolder.ioDispatcher) {
            if (sourceUri.isNotBlank()) {
                recipeFile(sourceUri).delete()
            }
        }

    private fun recipeFile(sourceUri: String): File = File(
        recipeDirectory,
        "${sourceKey(sourceUri)}.json"
    )

    private fun sourceKey(sourceUri: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(sourceUri.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
}
