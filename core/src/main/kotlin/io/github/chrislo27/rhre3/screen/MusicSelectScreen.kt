package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*


class MusicSelectScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, MusicSelectScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }

    private val editor: Editor
        get() = editorScreen.editor

    override val stage: Stage<MusicSelectScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_songchoose"))
        stage.titleLabel.text = "screen.music.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        val palette = main.uiPalette

        stage.bottomStage.elements += MusicFileChooserButton(palette, stage.bottomStage, stage.bottomStage).apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.textAlign = Align.center
                this.text = "screen.music.select"
                this.isLocalizationKey = true
                this.fontScaleMultiplier = 0.9f
            })
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

    inner class MusicFileChooserButton(palette: UIPalette, parent: UIElement<MusicSelectScreen>,
                                       stage: Stage<MusicSelectScreen>)
        : Button<MusicSelectScreen>(palette, parent, stage) {



    }
}

