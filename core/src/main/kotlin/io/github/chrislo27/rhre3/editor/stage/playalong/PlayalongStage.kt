package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*
import kotlin.properties.Delegates


class PlayalongStage(val editor: Editor, val editorStage: EditorStage,
                     val palette: UIPalette, parent: UIElement<EditorScreen>?, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private val remix: Remix get() = editor.remix
    private val playalong: Playalong get() = remix.playalong
    private val main: RHRE3Application get() = editor.main

    val lowerStage: Stage<EditorScreen>
    val noEntitiesLabel: TextLabel<EditorScreen>

    override var visible: Boolean by Delegates.observable(super.visible) { _, _, new -> if (new) onShow() else onHide() }

    init {
        val palette = main.uiPalette
        lowerStage = Stage(this, this.camera).apply {
            this.location.set(screenHeight = 0.25f)
            this.location.set(0f, 0f, 1f)
        }
        this.elements += lowerStage
        lowerStage.elements += ColourPane(lowerStage, lowerStage).apply {
            this.colour.set(0f, 0f, 0f, 0.65f)
        }
        noEntitiesLabel = object : TextLabel<EditorScreen>(palette, lowerStage, lowerStage) {
            override fun getRealText(): String {
                return if (!isLocalizationKey) super.getRealText() else Localization[text, "[#DDDDDD]${Localization[Series.OTHER.localization]} ➡ ${GameRegistry.data.playalongGame.group} ➡ ${GameRegistry.data.playalongGame.name}[]"]
            }
        }.apply {
            this.isLocalizationKey = true
            this.text = "playalong.noCues"
        }
        lowerStage.elements += noEntitiesLabel

        this.elements += FlickingStage(this, this).apply {
            this.colour.set(Color.valueOf("00BC67"))
            this.location.set(screenX = 0.6f)
            this.location.set(location.screenX, 0f, 1f - location.screenX, 1f)
            this.visible = false
        }
    }

    fun onShow() {
        noEntitiesLabel.visible = playalong.inputActions.isEmpty()
    }

    fun onHide() {

    }

    override fun keyUp(keycode: Int): Boolean {
        val ret = super.keyUp(keycode)
        if (ret) return ret
        return playalong.onKeyUp(keycode)
    }

    override fun keyDown(keycode: Int): Boolean {
        val ret = super.keyDown(keycode)
        if (ret) return ret
        return playalong.onKeyDown(keycode)
    }
}