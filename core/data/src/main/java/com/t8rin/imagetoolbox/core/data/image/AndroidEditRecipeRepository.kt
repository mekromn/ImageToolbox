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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
 *
 * Replacement is temp-first and crash-recoverable on API 24+: the previous
 * complete document is renamed to a backup before the new complete temp file is
 * moved into place. Load falls back to the backup if a process dies between the
 * two renames.
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

    private val storageMutex = Mutex()

    override suspend fun load(source: EditSourceIdentity): StoredEditRecipe? =
        withContext(dispatchersHolder.ioDispatcher) {
            storageMutex.withLock {
                if (source.uri.isBlank()) return@withLock null

                val destination = recipeFile(source.uri)
                val backup = backupFile(destination)
                val readable = when {
                    destination.isFile -> destination
                    backup.isFile -> backup
                    else -> return@withLock null
                }

                runCatching {
                    val recipe = jsonParser.fromJson<EditRecipe>(
                        json = readable.readText(),
                        type = EditRecipe::class.java
                    ) ?: return@runCatching null

                    StoredEditRecipe(
                        recipe = recipe,
                        sourceMatches = recipe.source.matches(source)
                    )
                }.onFailure {
                    // Preserve unreadable/stale work for diagnostics/recovery.
                    // Never silently delete a user's recipe because parsing failed.
                    it.makeLog("AndroidEditRecipeRepository load")
                }.getOrNull()
            }
        }

    override suspend fun save(recipe: EditRecipe): Unit =
        withContext(dispatchersHolder.ioDispatcher) {
            storageMutex.withLock {
                require(recipe.source.uri.isNotBlank()) {
                    "Cannot persist an EditRecipe with a blank source URI"
                }

                val json = jsonParser.toJson(
                    obj = recipe,
                    type = EditRecipe::class.java
                ) ?: error("Could not serialize EditRecipe schema ${recipe.schemaVersion}")

                recipeDirectory.mkdirs()

                val destination = recipeFile(recipe.source.uri)
                val temporary = temporaryFile(destination)
                val backup = backupFile(destination)

                temporary.delete()

                try {
                    FileOutputStream(temporary).use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                        stream.flush()
                        stream.fd.sync()
                    }

                    backup.delete()
                    if (destination.exists() && !destination.renameTo(backup)) {
                        throw IOException("Could not preserve previous EditRecipe before replacement")
                    }

                    if (!temporary.renameTo(destination)) {
                        if (backup.exists()) {
                            backup.renameTo(destination)
                        }
                        throw IOException("Could not move completed EditRecipe into place")
                    }

                    backup.delete()
                } catch (throwable: Throwable) {
                    temporary.delete()
                    if (!destination.exists() && backup.exists()) {
                        backup.renameTo(destination)
                    }
                    throw throwable
                }
            }
        }

    override suspend fun delete(sourceUri: String): Unit =
        withContext(dispatchersHolder.ioDispatcher) {
            storageMutex.withLock {
                if (sourceUri.isNotBlank()) {
                    val destination = recipeFile(sourceUri)
                    destination.delete()
                    temporaryFile(destination).delete()
                    backupFile(destination).delete()
                }
            }
        }

    private fun recipeFile(sourceUri: String): File = File(
        recipeDirectory,
        "${sourceKey(sourceUri)}.json"
    )

    private fun temporaryFile(destination: File): File = File(
        recipeDirectory,
        "${destination.name}.tmp"
    )

    private fun backupFile(destination: File): File = File(
        recipeDirectory,
        "${destination.name}.bak"
    )

    private fun sourceKey(sourceUri: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(sourceUri.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
}
