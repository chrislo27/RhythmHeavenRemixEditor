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
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.JavafxStub
import io.github.chrislo27.rhre3.util.attemptRememberDirectory
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
    private val loadButton: LoadButton
    private val mainLabel: TextLabel<OpenRemixScreen>
    @Volatile private var remix: Remix? = null
        set(value) {
            field = value
            loadButton.visible = field != null
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
        mainLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            //            this.location.set(screenHeight = 0.75f, screenY = 0.25f)
            this.textAlign = Align.center
            this.isLocalizationKey = false
            this.text = ""
        }
        stage.centreStage.elements += mainLabel
        loadButton = LoadButton(palette, stage.bottomStage, stage.bottomStage).apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.textAlign = Align.center
                this.text = "screen.open.button"
                this.isLocalizationKey = true
            })
            this.visible = false
        }
        stage.bottomStage.elements += loadButton

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
                        remix = null
                        val zipFile = ZipFile(file)
                        val isRHRE2 = zipFile.getEntry("remix.json") == null

                        // TODO handle RHRE2
                        val result = if (isRHRE2) TODO() else Remix.unpack(editor.createRemix(), zipFile)

                        fun goodBad(str: String, bad: Boolean, badness: String = "ORANGE"): String {
                            return if (bad) "[$badness]$str[]" else "[LIGHT_GRAY]$str[]"
                        }

                        remix = result.first
                        val remix = remix!!
                        val missingAssets = result.second
                        mainLabel.text = Localization["screen.open.info",
                                goodBad(remix.version.toString(), remix.version != RHRE3.VERSION),
                                goodBad(remix.databaseVersion.toString(), remix.databaseVersion != GameRegistry.data.version),
                                goodBad(missingAssets.first.toString(), missingAssets.first > 0, "RED"),
                                goodBad(missingAssets.second.toString(), missingAssets.second > 0, "RED")]
                        if (GameRegistry.data.version < remix.databaseVersion) {
                            mainLabel.text += "\n" + Localization["screen.open.oldDatabase"]
                        } else if (remix.version < RHRE3.VERSION) {
                            mainLabel.text += "\n" +
                                    Localization[if (isRHRE2)
                                        "screen.open.rhre2Warning"
                                    else
                                        "screen.open.oldVersion"]
                        }
                        if (remix.version > RHRE3.VERSION) {
                            mainLabel.text += "\n" + Localization["screen.open.oldVersion2"]
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        mainLabel.text = Localization["screen.open.failed", t::class.java.canonicalName]
                        remix?.dispose()
                        remix = null
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
        remix = null
    }

    override fun hide() {
        super.hide()
        mainLabel.text = ""
        remix = null
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }

    inner class LoadButton(palette: UIPalette, parent: UIElement<OpenRemixScreen>,
                           stage: Stage<OpenRemixScreen>)
        : Button<OpenRemixScreen>(palette, parent, stage) {

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            val remix = remix ?: return
            editor.remix = remix
            (this@OpenRemixScreen.stage as GenericStage).onBackButtonClick()
        }
    }
}
