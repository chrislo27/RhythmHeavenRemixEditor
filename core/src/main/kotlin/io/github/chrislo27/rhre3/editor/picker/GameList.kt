package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.Game


class GameList : ScrollList<Game>() {

    override val maxScroll: Int
        get() = (list.size - Editor.ICON_COUNT_Y).coerceAtLeast(0)

}