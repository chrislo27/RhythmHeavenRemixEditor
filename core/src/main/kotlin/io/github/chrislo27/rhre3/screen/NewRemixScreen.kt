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
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class NewRemixScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, NewRemixScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: Stage<NewRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    @Volatile private var isChooserOpen = false
        set(value) {
            field = value
            stage as GenericStage
            stage.backButton.enabled = !isChooserOpen
        }
    private val mainLabel: TextLabel<NewRemixScreen>

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_newremix"))
        stage.titleLabel.text = "screen.new.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette

        stage.bottomStage.elements +=
                object : Button<NewRemixScreen>(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                                                             clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)),
                                                stage.bottomStage, stage.bottomStage) {
                    override fun onLeftClick(xPercent: Float, yPercent: Float) {
                        super.onLeftClick(xPercent, yPercent)
                        editor.remix = editor.createRemix()
                        this@NewRemixScreen.stage.onBackButtonClick()
                        Gdx.app.postRunnable {
                            System.gc()
                        }
                    }
                }.apply {
                    this.location.set(screenX = 0.25f, screenWidth = 0.5f)
                    this.addLabel(TextLabel(palette, this, this.stage).apply {
                        this.textAlign = Align.center
                        this.text = "screen.new.button"
                        this.isLocalizationKey = true
                    })
                }
        mainLabel = object : TextLabel<NewRemixScreen>(palette, stage.centreStage, stage.centreStage) {
        }.apply {
            this.location.set(screenX = 0.5f, screenWidth = 0.5f - 0.125f)
            this.textAlign = Align.left
            this.isLocalizationKey = true
            this.text = "screen.new.warning"
        }
        stage.centreStage.elements += mainLabel
        val warn = ImageLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_warn"))
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenY = 0.125f, screenHeight = 0.75f)
        }
        stage.centreStage.elements += warn
        warn.apply {
            stage.updatePositions()
            this.location.set(screenWidth = stage.percentageOfWidth(this.location.realHeight))
            this.location.set(screenX = 0.5f - this.location.screenWidth)
        }

        stage.updatePositions()
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            (stage as GenericStage).onBackButtonClick()
        }
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}