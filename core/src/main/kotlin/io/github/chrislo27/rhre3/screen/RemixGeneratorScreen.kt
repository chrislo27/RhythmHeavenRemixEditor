package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry


class RemixGeneratorScreen(main: RHRE3Application, val editor: Editor)
    : ToolboksScreen<RHRE3Application, RemixGeneratorScreen>(main) {

    override val stage: GenericStage<RemixGeneratorScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val remix: Remix get() = editor.remix

    init {
        stage.titleLabel.text = "screen.remixGen.title"
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_inspections_big"))
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}