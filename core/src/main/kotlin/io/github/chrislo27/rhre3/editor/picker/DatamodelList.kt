package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel


class DatamodelList : ScrollList<Datamodel>() {

    override val maxScroll: Int
        get() = 0

    var smoothScroll: Float = 0f

}