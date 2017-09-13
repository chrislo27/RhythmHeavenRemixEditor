package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.view.ViewType
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class ViewChooserStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {


    init {
        this.elements += ColourPane(this, this).apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.colour.a = 0.8f
        }

        this.elements += TextLabel(palette, this, this).apply {
            this.location.set(screenX = 0f, screenWidth = 1f, screenY = 0.875f, screenHeight = 0.125f)

            this.textAlign = Align.center
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "editor.viewChooser.title"
        }

        val start = 0.875f
        val padding = 0.0125f

        ViewType.VALUES.forEachIndexed { index, enum ->
            this.elements += object : TrueCheckbox<EditorScreen>(palette, this, this) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    if (checked) {
                        editor.views.add(enum)
                    } else {
                        editor.views.remove(enum)
                    }
                    editor.remix.recomputeCachedData()
                }
            }.apply {
                this.location.set(screenX = 0.05f, screenWidth = 0.9f,
                                  screenHeight = 0.125f - padding,
                                  screenY = start - (0.125f * (index + 1)))

                this.textLabel.apply {
                    this.isLocalizationKey = true
                    this.fontScaleMultiplier = 0.8f
                    this.textWrapping = false
                    this.textAlign = Align.left
                    this.text = enum.localizationKey
                }

                this.checked = false
            }
        }
    }


}