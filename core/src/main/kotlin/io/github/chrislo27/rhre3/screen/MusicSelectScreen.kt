package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.track.MusicData
import io.github.chrislo27.rhre3.util.attemptRememberDirectory
import io.github.chrislo27.rhre3.util.err.MusicLoadingException
import io.github.chrislo27.rhre3.util.getDefaultDirectory
import io.github.chrislo27.rhre3.util.persistDirectory
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import javafx.application.Platform
import javafx.stage.FileChooser
import java.io.File


class MusicSelectScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, MusicSelectScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: Stage<MusicSelectScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    @Volatile
    private var isChooserOpen = false
    @Volatile
    private var isLoading = false
    private val mainLabel: TextLabel<MusicSelectScreen>

    private fun createFileChooser() =
            FileChooser().apply {
                this.initialDirectory = attemptRememberDirectory(main,
                                                                 PreferenceKeys.FILE_CHOOSER_MUSIC) ?: getDefaultDirectory()
                val key = "screen.music.fileFilter"
                val extensions = arrayOf("*.ogg", "*.mp3", "*.wav")

                this.extensionFilters.clear()
                val filter = FileChooser.ExtensionFilter(Localization[key], *extensions)

                this.extensionFilters += filter
                this.selectedExtensionFilter = this.extensionFilters.first()

                this.title = Localization["screen.music.fileChooserTitle"]
            }

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_songchoose"))
        stage.titleLabel.text = "screen.music.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen && !isLoading) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette

        stage.bottomStage.elements += MusicFileChooserButton(palette, stage.bottomStage, stage.bottomStage).apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.textAlign = Align.center
                this.text = "screen.music.select"
                this.isLocalizationKey = true
//                this.fontScaleMultiplier = 0.9f
            })
        }
        stage.centreStage.elements += object : TextLabel<MusicSelectScreen>(palette, stage.centreStage,
                                                                            stage.centreStage) {
            override fun frameUpdate(screen: MusicSelectScreen) {
                super.frameUpdate(screen)
                this.visible = isChooserOpen
            }
        }.apply {
            this.location.set(screenHeight = 0.25f)
            this.textAlign = Align.center
            this.isLocalizationKey = true
            this.text = "closeChooser"
            this.visible = false
        }
        mainLabel = object : TextLabel<MusicSelectScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun frameUpdate(screen: MusicSelectScreen) {
                super.frameUpdate(screen)
            }
        }.apply {
            //            this.location.set(screenHeight = 0.75f, screenY = 0.25f)
            this.textAlign = Align.center
            this.isLocalizationKey = false
            this.text = ""
        }
        stage.centreStage.elements += mainLabel

        stage.centreStage.elements += object : LoadingIcon<MusicSelectScreen>(palette, stage.centreStage) {
            override var visible: Boolean = true
                get() = super.visible && isLoading
        }.apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.125f, screenY = 0.125f / 2f)
        }

        stage.updatePositions()
        updateLabels(null)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        stage as GenericStage
        stage.backButton.enabled = !(isChooserOpen || isLoading)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            stage.onBackButtonClick()
        }
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen) {
            Platform.runLater {
                isChooserOpen = true
                val fileChooser = createFileChooser()
                val file: File? = fileChooser.showOpenDialog(null)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    isLoading = true
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_MUSIC, fileChooser.initialDirectory)
                    try {
                        updateLabels(null)
                        val handle = FileHandle(file)
                        val musicData = MusicData(handle, editor.remix)
                        editor.remix.music = musicData
                        isLoading = false
                        updateLabels(null)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        updateLabels(t)
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    private fun updateLabels(throwable: Throwable? = null) {
        val label = mainLabel
        if (throwable == null) {
            val music = editor.remix.music
            if (isLoading) {
                label.text = Localization["screen.music.loadingMusic"]
            } else {
                label.text = Localization["screen.music.currentMusic",
                        if (music == null) Localization["screen.music.noMusic"] else music.handle.name()]
            }
//            if (music != null) {
//                if (music.handle.extension().equals("wav", true)) {
//                    label.text += "\n\n${Localization["screen.music.warning.wav"]}"
//                } else if (music.handle.extension().equals("mp3", true)) {
//                    label.text += "\n\n${Localization["screen.music.warning.mp3"]}"
//                }
//            }
        } else {
            label.text = when (throwable) {
                is MusicLoadingException -> throwable.getLocalizedText()
                else -> Localization["screen.music.invalid", throwable::class.java.canonicalName]
            }
        }
    }

    override fun show() {
        super.show()
        updateLabels()
        openPicker()
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }

    inner class MusicFileChooserButton(palette: UIPalette, parent: UIElement<MusicSelectScreen>,
                                       stage: Stage<MusicSelectScreen>)
        : Button<MusicSelectScreen>(palette, parent, stage) {

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            openPicker()
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)
            if (!isChooserOpen) {
                editor.remix.music = null
                updateLabels()
            }
        }

        override fun frameUpdate(screen: MusicSelectScreen) {
            super.frameUpdate(screen)
            this.enabled = !(isChooserOpen || isLoading)
        }
    }
}

