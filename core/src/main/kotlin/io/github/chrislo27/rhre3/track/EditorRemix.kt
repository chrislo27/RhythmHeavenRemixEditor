package io.github.chrislo27.rhre3.track

import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor


class EditorRemix(main: RHRE3Application, val editor: Editor) : Remix(main) {
    override var doUpdatePlayalong: Boolean
        get() = editor.stage.playalongStage.visible
        set(_) {}
    override var cuesMuted: Boolean = super.cuesMuted
        get() = field && editor.stage.tapalongStage.visible
}