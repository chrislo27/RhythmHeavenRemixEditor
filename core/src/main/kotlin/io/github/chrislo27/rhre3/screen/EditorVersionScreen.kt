package io.github.chrislo27.rhre3.screen

import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.ChangelogStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.ui.Stage


class EditorVersionScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, EditorVersionScreen>(main) {

    override val stage: Stage<EditorVersionScreen> = ChangelogStage(main.uiPalette, null, main.defaultCamera)

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}