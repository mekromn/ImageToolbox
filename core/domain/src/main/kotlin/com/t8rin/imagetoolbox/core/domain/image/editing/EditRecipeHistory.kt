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
 * Small platform-independent history engine for [EditRecipe].
 *
 * The existing SingleEdit history can carry this recipe state while legacy
 * baked-bitmap tools are migrated. Continuous UI interactions should bracket
 * updates with [beginInteraction] / [endInteraction] so one slider drag is one
 * undo entry rather than hundreds of intermediate snapshots.
 */
class EditRecipeHistory(
    initialRecipe: EditRecipe = EditRecipe.empty(),
    val maxHistorySize: Int = DEFAULT_MAX_HISTORY_SIZE
) {

    init {
        require(maxHistorySize > 0) { "maxHistorySize must be positive" }
    }

    private val undoStack = ArrayDeque<EditRecipe>()
    private val redoStack = ArrayDeque<EditRecipe>()

    private var interactionStart: EditRecipe? = null

    var recipe: EditRecipe = initialRecipe
        private set

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    val isInteractionInProgress: Boolean
        get() = interactionStart != null

    fun beginInteraction() {
        if (interactionStart == null) {
            interactionStart = recipe
        }
    }

    fun endInteraction(): Boolean {
        val start = interactionStart ?: return false
        interactionStart = null

        if (start == recipe) return false

        pushUndo(start)
        redoStack.clear()
        return true
    }

    fun cancelInteraction(): Boolean {
        val start = interactionStart ?: return false
        interactionStart = null

        if (start == recipe) return false

        recipe = start
        return true
    }

    /**
     * Replaces current recipe. During a bracketed interaction this does not push
     * intermediate states; [endInteraction] commits the original starting state.
     */
    fun setRecipe(
        next: EditRecipe,
        recordHistory: Boolean = true
    ): Boolean {
        if (next == recipe) return false

        if (interactionStart != null) {
            recipe = next
            return true
        }

        if (recordHistory) {
            pushUndo(recipe)
            redoStack.clear()
        }

        recipe = next
        return true
    }

    /**
     * Replaces recipe after loading/restoring authoritative persistent state.
     * Existing undo/redo is intentionally discarded because it refers to the old
     * session state.
     */
    fun restore(recipe: EditRecipe) {
        interactionStart = null
        undoStack.clear()
        redoStack.clear()
        this.recipe = recipe
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false

        interactionStart = null
        redoStack.addLast(recipe)
        recipe = undoStack.removeLast()
        trim(redoStack)
        return true
    }

    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false

        interactionStart = null
        pushUndo(recipe)
        recipe = redoStack.removeLast()
        return true
    }

    fun clearHistory() {
        interactionStart = null
        undoStack.clear()
        redoStack.clear()
    }

    private fun pushUndo(recipe: EditRecipe) {
        undoStack.addLast(recipe)
        trim(undoStack)
    }

    private fun trim(stack: ArrayDeque<EditRecipe>) {
        while (stack.size > maxHistorySize) {
            stack.removeFirst()
        }
    }

    companion object {
        const val DEFAULT_MAX_HISTORY_SIZE: Int = 100
    }
}
