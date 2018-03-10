package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.GameMetadata
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.attemptRememberDirectory
import io.github.chrislo27.rhre3.util.getDefaultDirectory
import io.github.chrislo27.rhre3.util.persistDirectory
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import javafx.application.Platform
import javafx.stage.FileChooser
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.io.File


class SaveRemixScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, SaveRemixScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: Stage<SaveRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    @Volatile
    private var isChooserOpen = false
        set(value) {
            field = value
            stage as GenericStage
            stage.backButton.enabled = !isChooserOpen
        }
    private val mainLabel: TextLabel<SaveRemixScreen>

    private fun createFileChooser() =
            FileChooser().apply {
                this.initialDirectory = attemptRememberDirectory(main, PreferenceKeys.FILE_CHOOSER_SAVE)
                        ?: getDefaultDirectory()
                val key = "screen.save.fileFilter"
                val extensions = arrayOf("*.${RHRE3.REMIX_FILE_EXTENSION}")

                this.extensionFilters.clear()
                val filter = FileChooser.ExtensionFilter(Localization[key], *extensions)

                this.extensionFilters += filter
                this.selectedExtensionFilter = this.extensionFilters.first()

                this.title = Localization["screen.save.fileChooserTitle"]
            }

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_saveremix"))
        stage.titleLabel.text = "screen.save.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette
        stage.centreStage.elements += object : TextLabel<SaveRemixScreen>(palette, stage.centreStage,
                                                                          stage.centreStage) {
            override fun frameUpdate(screen: SaveRemixScreen) {
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
        mainLabel = object : TextLabel<SaveRemixScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun frameUpdate(screen: SaveRemixScreen) {
                super.frameUpdate(screen)
            }
        }.apply {
            this.location.set(screenHeight = 0.75f, screenY = 0.25f)
            this.textAlign = Align.center
            this.isLocalizationKey = false
            this.text = ""
        }
        stage.centreStage.elements += mainLabel

        stage.updatePositions()
        updateLabels(null)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            (stage as GenericStage).onBackButtonClick()
        }
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen) {
            Platform.runLater {
                isChooserOpen = true
                val fileChooser = createFileChooser()
                val lastSaveFile = editor.lastSaveFile
                if (lastSaveFile != null) {
                    fileChooser.initialDirectory = lastSaveFile.parent().file()
                    fileChooser.initialFileName = lastSaveFile.name()
                }
                val file: File? = fileChooser.showSaveDialog(null)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_SAVE, fileChooser.initialDirectory)
                    launch(CommonPool) {
                        try {
                            val correctFile = if (file.extension != RHRE3.REMIX_FILE_EXTENSION)
                                file.parentFile.resolve("${file.name}.${RHRE3.REMIX_FILE_EXTENSION}")
                            else
                                file

                            Remix.saveTo(editor.remix, correctFile, false)
                            editor.setFileHandles(FileHandle(correctFile))
                            editor.remix.markAsSaved()

                            mainLabel.text = Localization["screen.save.success"]
                            Gdx.app.postRunnable(GameMetadata::persist)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            updateLabels(t)
                        }
                    }
                } else {
                    (stage as GenericStage).onBackButtonClick()
                }
            }
        }
    }

    private fun updateLabels(throwable: Throwable? = null) {
        val label = mainLabel
        if (throwable == null) {
            label.text = ""
        } else {
            label.text = Localization["screen.save.failed", throwable::class.java.canonicalName]
        }
    }

    override fun show() {
        super.show()
        openPicker()
        updateLabels()
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}