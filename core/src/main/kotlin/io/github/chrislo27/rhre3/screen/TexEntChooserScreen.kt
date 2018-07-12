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
import io.github.chrislo27.rhre3.entity.model.special.TextureEntity
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.attemptRememberDirectory
import io.github.chrislo27.rhre3.util.getDefaultDirectory
import io.github.chrislo27.rhre3.util.persistDirectory
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.TextLabel
import javafx.application.Platform
import javafx.stage.FileChooser
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest


class TexEntChooserScreen(main: RHRE3Application, val entity: TextureEntity)
    : ToolboksScreen<RHRE3Application, TexEntChooserScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: GenericStage<TexEntChooserScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    @Volatile
    private var isChooserOpen = false
        set(value) {
            field = value
            stage.backButton.enabled = !isChooserOpen
        }
    private val mainLabel: TextLabel<TexEntChooserScreen>

    private fun createFileChooser() =
            FileChooser().apply {
                this.initialDirectory = attemptRememberDirectory(main, PreferenceKeys.FILE_CHOOSER_TEXENT)
                        ?: getDefaultDirectory()
                val key = "screen.texent.fileFilter"
                val extensions = arrayOf("*.png", "*.jpg")

                this.extensionFilters.clear()
                val filter = FileChooser.ExtensionFilter(Localization[key], *extensions)

                this.extensionFilters += filter
                this.selectedExtensionFilter = this.extensionFilters.first()

                this.title = Localization["screen.texent.fileChooserTitle"]
            }

    init {
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
        stage.titleLabel.text = "screen.texent.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette
        stage.centreStage.elements += object : TextLabel<TexEntChooserScreen>(palette, stage.centreStage,
                                                                          stage.centreStage) {
            override fun frameUpdate(screen: TexEntChooserScreen) {
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
            Platform.runLater {
                isChooserOpen = true
                val fileChooser = createFileChooser()
                val file: File? = fileChooser.showOpenDialog(null)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_TEXENT, fileChooser.initialDirectory)
                    Gdx.app.postRunnable {
                        try {
                            val remix = editor.remix

                            // Attempt load texture
                            val texture = Texture(FileHandle(file))
                            val hash: String = MessageDigest.getInstance("SHA-1").let {
                                it.update(file.readBytes())
                                BigInteger(1, it.digest()).toString(16)
                            }

                            if (remix.textureCache.containsKey(hash)) {
                                texture.dispose()
                            } else {
                                remix.textureCache[hash] = texture
                            }
                            entity.textureHash = hash

                            stage.onBackButtonClick()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            updateLabels(t)
                        }
                    }
                } else {
                    stage.onBackButtonClick()
                }
            }
        }
    }

    private fun updateLabels(throwable: Throwable? = null) {
        val label = mainLabel
        if (throwable == null) {
            label.text = ""
        } else {
            label.text = Localization["screen.texent.failed", throwable::class.java.canonicalName]
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