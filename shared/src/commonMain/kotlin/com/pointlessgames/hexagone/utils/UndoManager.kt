package com.pointlessgames.hexagone.utils

internal class UndoManager<T>() {

    private var index: Int = -1
    private val stateStack: MutableList<T> = mutableListOf()

    fun insertState(state: T) {
        if (canRedo()) {
            clearRedoQueue()
        }

        if (stateStack.getOrNull(index) == state) return

        index++
        stateStack.add(state)
    }

    fun undo(): T {
        if (!canUndo()) error("Undo is not possible")

        return stateStack[--index]
    }

    fun redo(): T {
        if (!canRedo()) error("Redo is not possible")

        return stateStack[++index]
    }

    fun clear(): T? {
        index = -1
        val firstState = stateStack.getOrNull(0)
        stateStack.clear()

        return firstState
    }

    fun canUndo(): Boolean = !canRedo() && index >= 0 || canRedo() && index > 0
    fun canRedo(): Boolean = stateStack.isNotEmpty() && index < stateStack.lastIndex

    private fun clearRedoQueue() {
        // Remove all elements after the index
        stateStack.subList(index + 1, stateStack.size).clear()
    }
}
