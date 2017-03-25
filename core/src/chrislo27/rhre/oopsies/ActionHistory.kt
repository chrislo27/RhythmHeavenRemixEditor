package chrislo27.rhre.oopsies

import java.util.*

/**
 * Supports undoing and redoing on this instance.
 * @param SELF The real impl
 */
@Suppress("UNCHECKED_CAST")
open class ActionHistory<SELF : ActionHistory<SELF>> {

	private val undos: Deque<ReversibleAction<SELF>> = ArrayDeque()
	private val redos: Deque<ReversibleAction<SELF>> = ArrayDeque()

	/**
	 * Mutate this object, adding the action on the undo stack, and clearing all redos.
	 */
	fun mutate(action: ReversibleAction<SELF>) {
		redos.clear()
		undos.push(action)

		action.redo(this as SELF)
	}

	fun undo(): Boolean {
		if (!canUndo()) return false

		val action = undos.pop()
		action.undo(this as SELF)

		redos.push(action)

		return true
	}

	fun redo(): Boolean {
		if (!canRedo()) return false

		val action = redos.pop()
		action.redo(this as SELF)

		undos.push(action)

		return true
	}

	fun canUndo() = undos.size > 0

	fun canRedo() = redos.size > 0

	fun clear() {
		undos.clear()
		redos.clear()
	}

}
