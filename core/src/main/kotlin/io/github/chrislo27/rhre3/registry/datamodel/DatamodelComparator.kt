package io.github.chrislo27.rhre3.registry.datamodel


object DatamodelComparator : Comparator<Datamodel> {

    override fun compare(o1: Datamodel, o2: Datamodel): Int {
        if (o1 is Cue && o2 !is Cue) {
            return 1
        } else if (o1 !is Cue && o2 is Cue) {
            return -1
        }

        return 0
    }

}