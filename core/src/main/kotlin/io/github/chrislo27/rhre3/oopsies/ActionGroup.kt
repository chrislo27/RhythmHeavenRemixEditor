package io.github.chrislo27.rhre3.oopsies


class ActionGroup<A : ActionHistory<A>>(var list: List<ReversibleAction<A>>) : ReversibleAction<A> {

    constructor(vararg actions: ReversibleAction<A>) : this(listOf(*actions))

    override fun redo(context: A) {
        list.forEach {
            it.redo(context)
        }
    }

    override fun undo(context: A) {
        list.asReversed().forEach {
            it.undo(context)
        }
    }
}
