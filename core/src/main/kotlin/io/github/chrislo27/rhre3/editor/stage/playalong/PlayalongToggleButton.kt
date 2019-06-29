package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.PlayalongSettingsScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper


class PlayalongToggleButton(val editorStage: EditorStage, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    private val main: RHRE3Application get() = editorStage.main
    private val label = TextLabel(palette, this, this.stage).apply {
        this.isLocalizationKey = false
        this.text = "\uE0E0"
        this.textWrapping = false
        this.textColor = Color(1f, 1f, 1f, 1f)
    }

    init {
        this.addLabel(label)
    }

    override var tooltipText: String?
        set(_) {}
        get() = Localization["editor.playalong"]

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (editorStage.playalongStage.visible) {
            label.textColor?.fromHsv(MathHelper.getSawtoothWave(1.5f) * 360f, 0.3f, 0.75f)
        } else {
            label.textColor?.set(1f, 1f, 1f, 1f)
        }

        super.render(screen, batch, shapeRenderer)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val stage = editorStage
        val visible = !stage.playalongStage.visible
        val editor = stage.editor
        stage.elements.filterIsInstance<Stage<*>>().forEach {
            it.visible = !visible
        }
        stage.playalongStage.visible = visible
        stage.subtitleStage.visible = true // Exception made for subtitles
        stage.tapalongStage.visible = false
        stage.presentationModeStage.visible = false
        stage.paneLikeStages.forEach { it.visible = false }
        stage.buttonBarStage.visible = true
        stage.messageBarStage.visible = true
        stage.centreAreaStage.visible = true
        if (visible) {
            editor.currentTool = Tool.SELECTION
        }
        stage.updateSelected()
        editor.updateMessageLabel()
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        main.screen = PlayalongSettingsScreen(main)
    }

}