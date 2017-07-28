package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.UIPalette


class TestScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, TestScreen>(main) {

    val palette: UIPalette = UIPalette(main.fonts[main.defaultFontKey], main.fonts[main.defaultFontLargeKey], 1f,
                                       Color(1f, 1f, 1f, 1f), Color(0f, 0f, 0f, 0.75f),
                                       Color(0f, 0.5f, 0.5f, 0.75f), Color(0.25f, 0.25f, 0.25f, 0.75f))
    override val stage: Stage<TestScreen> = GenericStage(palette, null, main.defaultCamera)
    val textField: TextField<TestScreen>

    init {
        // init stage
        stage as GenericStage
        stage.titleLabel.apply {
            this.text = "Test title"
            this.isLocalizationKey = false
        }

        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("rhre3_icon_512"))
        }

        textField = TextField(main.uiPalette, stage.centreStage, stage.centreStage)
        textField.apply {
            this.background = true
            this.textAlign = Align.topLeft
//            this.multiline = true
            this.location.set(screenHeight = 0.5f, screenWidth = 0.25f)
            this.text = "ok"
        }

        stage.centreStage.elements += textField

        stage.updatePositions()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render(delta)
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}