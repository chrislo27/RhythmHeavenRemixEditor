package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.JavafxStub
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
import java.io.File
import java.util.zip.ZipFile


class OpenRemixScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, OpenRemixScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: Stage<OpenRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    @Volatile private var isChooserOpen = false
        set(value) {
            field = value
            stage as GenericStage
            stage.backButton.enabled = !isChooserOpen
        }

    private val fileChooser: FileChooser = FileChooser().apply {
        this.initialDirectory = attemptRememberDirectory(main,
                                                         PreferenceKeys.FILE_CHOOSER_LOAD) ?: getDefaultDirectory()

        fun applyLocalizationChanges() {
            this.extensionFilters.clear()
            val filter = FileChooser.ExtensionFilter(Localization["screen.save.fileFilter"],
                                                     "*.${RHRE3.REMIX_FILE_EXTENSION}")

            this.extensionFilters += filter
            this.extensionFilters += FileChooser.ExtensionFilter(Localization["screen.open.fileFilterRHRE2"],
                                                                 "*.brhre2")
            this.selectedExtensionFilter = this.extensionFilters.first()

            this.title = Localization["screen.open.fileChooserTitle"]
        }

        applyLocalizationChanges()

        Localization.listeners += { old ->
            applyLocalizationChanges()
        }
    }

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
        stage.titleLabel.text = "screen.open.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette

        stage.centreStage.elements += object : TextLabel<OpenRemixScreen>(palette, stage.centreStage,
                                                                            stage.centreStage) {
            override fun frameUpdate(screen: OpenRemixScreen) {
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

        stage.updatePositions()
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
                val file: File? = fileChooser.showOpenDialog(JavafxStub.application.primaryStage)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_LOAD, fileChooser.initialDirectory)
                    try {
                        val zipFile = ZipFile(file)
                        val isRHRE2 = zipFile.getEntry("remix.json") == null
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                } else {
                    (stage as GenericStage).onBackButtonClick()
                }
            }
        }
    }

    override fun show() {
        super.show()
        openPicker()
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}
