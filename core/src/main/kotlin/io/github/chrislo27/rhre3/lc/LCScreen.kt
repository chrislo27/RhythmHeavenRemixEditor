package io.github.chrislo27.rhre3.lc

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.screen.HidesVersionText
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.bg.Background
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class LCScreen(main: RHRE3Application, val reason: String)
    : ToolboksScreen<RHRE3Application, LCScreen>(main), HidesVersionText {

    inner class NothingBackground : Background("nothingBackground-${this@LCScreen.hashCode()}") {
        override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
            batch.setColor(0f, 0f, 0f, 1f)
            batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
            batch.setColor(1f, 1f, 1f, 1f)
        }
    }

    override val stage: GenericStage<LCScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    init {
        GenericStage.backgroundImpl = NothingBackground()
        stage.backButton.apply {
            enabled = false
            visible = false
        }
        stage.titleIcon.visible = false
        stage.titleLabel.visible = false

        stage.centreStage.elements += TextLabel(main.uiPalette, stage.centreStage, stage.centreStage).apply {
            textWrapping = false
            isLocalizationKey = false
            text = reason
            textAlign = Align.center
            fontScaleMultiplier = 0.75f
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}