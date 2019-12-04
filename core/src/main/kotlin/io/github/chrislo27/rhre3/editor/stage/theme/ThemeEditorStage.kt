package io.github.chrislo27.rhre3.editor.stage.theme

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class ThemeEditorStage(val editor: Editor, val palette: UIPalette, parent: ThemeChooserStage, camera: OrthographicCamera, pixelWidth: Float, pixelHeight: Float)
    : Stage<EditorScreen>(parent, camera, pixelWidth, pixelHeight) {

    private val themeChooserStage: ThemeChooserStage = parent

    private val contentStage: Stage<EditorScreen>
    private val chooserButtonBar: Stage<EditorScreen>

    init {
        contentStage = Stage(this, this.camera, 346f, 392f).apply {
            location.set(screenX = 0f, screenY = 0f, screenWidth = 0f, screenHeight = 0f,
                         pixelX = 0f, pixelY = 0f, pixelWidth = 346f, pixelHeight = 392f)
        }
        this.elements += contentStage
        chooserButtonBar = Stage(contentStage, contentStage.camera, 346f, 34f).apply {
            location.set(screenX = 0f, screenY = 0f, screenWidth = 0f, screenHeight = 0f,
                         pixelX = 0f, pixelY = 0f, pixelWidth = 0f, pixelHeight = 34f)
            this.elements += Button(palette, this, this.stage).apply {
                this.location.set(0f, 0f, 0f, 1f, 0f, 0f, 346f - 34f * 2f - 8f * 2f, 0f)
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = true
                    this.text = "editor.themeEditor"
                    this.textWrapping = true
                    this.fontScaleMultiplier = 0.75f
                    this.location.set(pixelWidth = -4f, pixelX = 2f)
                })
            }

            this.elements += object : Button<EditorScreen>(palette, this, this.stage) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    
                }
            }.apply {
                this.location.set(0f, 0f, 0f, 1f, 346f - 34f * 2f - 8f, 0f, 34f, 0f)
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeChooser.reset"
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))
                })
            }

            this.elements += object : Button<EditorScreen>(palette, this, this.stage) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)

                    Gdx.net.openURI("file:///${LoadedThemes.THEMES_FOLDER.file().absolutePath}")
                }
            }.apply {
                this.location.set(0f, 0f, 0f, 1f, 346f - 34f, 0f, 34f, 0f)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
                })
            }
        }
        contentStage.elements += chooserButtonBar
    }

}