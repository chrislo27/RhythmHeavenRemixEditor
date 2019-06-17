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
import io.github.chrislo27.rhre3.RemixRecovery
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.sfxdb.GameMetadata
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.*
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.TextLabel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SaveRemixScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, SaveRemixScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: GenericStage<SaveRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    @Volatile
    private var isChooserOpen = false
        set(value) {
            field = value
            stage.backButton.enabled = !isChooserOpen
        }
    @Volatile
    private var isSaving: Boolean = false
    private val mainLabel: TextLabel<SaveRemixScreen>

    init {
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_saveremix"))
        stage.titleLabel.text = "screen.save.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        stage.centreStage.elements += object : LoadingIcon<SaveRemixScreen>(main.uiPalette, stage.centreStage) {
            override var visible: Boolean = true
                get() = field && isSaving
        }.apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.125f, screenY = 0.125f / 2f)
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
        mainLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
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
            stage.onBackButtonClick()
        }
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen) {
            GlobalScope.launch {
                isChooserOpen = true
                val lastSaveFile = editor.lastSaveFile
                val filters = listOf(FileChooserExtensionFilter(Localization["screen.save.fileFilter"], "*.${RHRE3.REMIX_FILE_EXTENSION}"))
                FileChooser.saveFileChooser(Localization["screen.save.fileChooserTitle"], lastSaveFile?.parent()?.file() ?: attemptRememberDirectory(main, PreferenceKeys.FILE_CHOOSER_SAVE) ?: getDefaultDirectory(), lastSaveFile?.file(), filters, filters.first()) { file ->
                    isChooserOpen = false
                    if (file != null) {
                        val newInitialDirectory = if (!file.isDirectory) file.parentFile else file
                        persistDirectory(main, PreferenceKeys.FILE_CHOOSER_SAVE, newInitialDirectory)
                        GlobalScope.launch {
                            try {
                                val correctFile = if (file.extension != RHRE3.REMIX_FILE_EXTENSION)
                                    file.parentFile.resolve("${file.name}.${RHRE3.REMIX_FILE_EXTENSION}")
                                else
                                    file

                                val remix = editor.remix
                                isSaving = true
                                Remix.saveTo(remix, correctFile, false)
                                val newfh = FileHandle(correctFile)
                                editor.setFileHandles(newfh)
                                RemixRecovery.cacheChecksumOfFile(newfh)

                                mainLabel.text = Localization["screen.save.success"]
                                Gdx.app.postRunnable(GameMetadata::persist)
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                updateLabels(t)
                            }
                            isSaving = false
                        }
                    } else {
                        stage.onBackButtonClick()
                    }
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
        isSaving = false
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}