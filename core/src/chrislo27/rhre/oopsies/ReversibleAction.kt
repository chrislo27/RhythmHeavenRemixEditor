package chrislo27.rhre.oopsies


interface ReversibleAction<A : ActionHistory<A>> {

	/**
	 * The action that mutates the context.
	 */
	fun redo(context: A)

	/**
	 * The action that undoes the mutation.
	 */
	fun undo(context: A)

}