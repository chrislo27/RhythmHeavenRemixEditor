package io.github.chrislo27.rhre3.oopsies


class ActionGroup<A : ActionHistory<A>> : ReversibleAction<A> {

    var list: List<ReversibleAction<A>> = mutableListOf()

    constructor(list: List<ReversibleAction<A>>) {
        this.list = list
    }

    override fun redo(context: A) {
        list.forEach {
            it.redo(context)
        }
    }

    override fun undo(context: A) {
        list.forEach {
            it.undo(context)
        }
    }
}
