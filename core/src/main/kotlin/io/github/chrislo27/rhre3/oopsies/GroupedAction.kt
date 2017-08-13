package io.github.chrislo27.rhre3.oopsies


class GroupedAction<A : ActionHistory<A>> : ReversibleAction<A> {

    var list: MutableList<ReversibleAction<A>> = mutableListOf()

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
