package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel


class RemixGeneratorScreen(main: RHRE3Application, val editor: Editor)
    : ToolboksScreen<RHRE3Application, RemixGeneratorScreen>(main) {

    private val remix: Remix get() = editor.remix

    override val stage: GenericStage<RemixGeneratorScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val seedField: TextField<RemixGeneratorScreen>

    init {
        val palette = main.uiPalette
        stage.titleLabel.text = "screen.remixGen.title"
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_inspections_big"))
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        val labelWidth = 0.2f
        val labelPadding = 0.025f
        val labelHeight = 0.125f
        val fieldHeight = 0.1f

        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.remixGen.seed"
            this.textAlign = Align.bottomLeft
            this.textColor = Color.LIGHT_GRAY
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenHeight = labelHeight, screenY = 0.8f - labelHeight * -0.5f)
        }
        seedField = TextField(palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenHeight = fieldHeight, screenY = 0.8f - labelHeight * 0.5f)
            this.background = true
        }
        stage.centreStage.elements += seedField
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.remixGen.seed.empty"
            this.textAlign = Align.left
            this.textColor = Color.LIGHT_GRAY
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenHeight = labelHeight, screenY = 0.8f - labelHeight * 1.5f)
        }

        stage.bottomStage.elements += object : Button<RemixGeneratorScreen>(palette, stage.bottomStage, stage.bottomStage){

        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.remixGen.generate"
                this.textWrapping = false
            })
            this.location.set(screenX = 0.2f, screenWidth = 0.6f)
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