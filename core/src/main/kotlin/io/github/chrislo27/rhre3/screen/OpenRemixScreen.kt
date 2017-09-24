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
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
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
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
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
    @Volatile private var isLoading = false

    private fun createFileChooser()
            = FileChooser().apply {
        this.initialDirectory = attemptRememberDirectory(main,
                                                         PreferenceKeys.FILE_CHOOSER_LOAD) ?: getDefaultDirectory()

        this.extensionFilters.clear()
        val filter = FileChooser.ExtensionFilter(Localization["screen.open.fileFilterBoth"],
                                                 "*.${RHRE3.REMIX_FILE_EXTENSION}",
                                                 "*.brhre2")

        this.extensionFilters += filter
        this.selectedExtensionFilter = this.extensionFilters.first()

        this.title = Localization["screen.open.fileChooserTitle"]
    }

    private val loadButton: LoadButton
    private val mainLabel: TextLabel<OpenRemixScreen>
    @Volatile private var remix: Remix? = null
        set(value) {
            field = value
            loadButton.visible = field != null
        }

    private fun List<Entity>.applyFilter(): List<ModelEntity<*>> {
        return filter { entity -> entity is CueEntity || entity is MultipartEntity<*> }
                .filterIsInstance<ModelEntity<*>>()
                .distinctBy { it.datamodel.id }
    }

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
        stage.titleLabel.text = "screen.open.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen && !isLoading) {
                editor.remix.entities.applyFilter().forEach { entity ->
                    if (entity is CueEntity) {
                        entity.datamodel.loadSounds()
                    } else if (entity is MultipartEntity<*>) {
                        entity.loadInternalSounds()
                    }
                }
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
                val fileChooser = createFileChooser()
                val file: File? = fileChooser.showOpenDialog(JavafxStub.application.primaryStage)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    isLoading = true
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_LOAD, fileChooser.initialDirectory)
                    launch(CommonPool) {
                        try {
                            remix = null
                            System.gc()
                            val zipFile = ZipFile(file)
                            val isRHRE2 = zipFile.getEntry("remix.json") == null

                            val result = if (isRHRE2)
                                Remix.unpackRHRE2(editor.createRemix(), zipFile)
                            else
                                Remix.unpack(editor.createRemix(), zipFile)

                            val toLoad = result.remix.entities.applyFilter()
                            val toUnload = editor.remix.entities.applyFilter().filter { it !in toLoad }

                            val coroutine: Job = launch(CommonPool) {
                                toLoad.forEach { entity ->
                                    if (entity is CueEntity) {
                                        entity.datamodel.loadSounds()
                                    } else if (entity is MultipartEntity<*>) {
                                        entity.loadInternalSounds()
                                    }
                                }
                                toUnload.forEach { entity ->
                                    if (entity is CueEntity) {
                                        entity.datamodel.unloadSounds()
                                    } else if (entity is MultipartEntity<*>) {
                                        entity.unloadInternalSounds()
                                    }
                                }
                            }

                            fun goodBad(str: String, bad: Boolean, badness: String = "ORANGE"): String {
                                return if (bad) "[$badness]$str[]" else "[LIGHT_GRAY]$str[]"
                            }

                            loadButton.alsoDo = {
                                runBlocking {
                                    coroutine.join()
                                }

                                if (!result.isAutosave) {
                                    editor.prepAutosaveFile(FileHandle(file))
                                }
                            }

                            remix = result.remix
                            val remix = remix!!
                            val missingAssets = result.missing
                            val databaseStr = if (isRHRE2)
                                goodBad(remix.version.toString(), true)
                            else
                                goodBad(remix.databaseVersion.toString(),
                                        remix.databaseVersion != GameRegistry.data.version)

                            mainLabel.text = ""

                            if (result.isAutosave) {
                                mainLabel.text += Localization["screen.open.autosave"] + "\n\n"
                            }

                            mainLabel.text += Localization["screen.open.info",
                                    goodBad(remix.version.toString(), remix.version != RHRE3.VERSION),
                                    databaseStr,
                                    goodBad(missingAssets.first.toString(), missingAssets.first > 0, "RED"),
                                    goodBad(if (isRHRE2) "?" else missingAssets.second.toString(),
                                            missingAssets.second > 0, "RED")]
                            if (GameRegistry.data.version < remix.databaseVersion) {
                                mainLabel.text += "\n" + Localization["screen.open.oldDatabase"]
                            } else if (remix.version < RHRE3.VERSION) {
                                mainLabel.text += "\n" +
                                        Localization[if (isRHRE2)
                                            "screen.open.rhre2Warning"
                                        else
                                            "screen.open.oldWarning"]
                            }
                            if (remix.version > RHRE3.VERSION) {
                                mainLabel.text += "\n" + Localization["screen.open.oldWarning2"]
                            }
                            isLoading = false
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            mainLabel.text = Localization["screen.open.failed", t::class.java.canonicalName]
                            remix?.dispose()
                            remix = null
                            isLoading = false
                        }
                    }
                } else {
                    loadButton.alsoDo = {}
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
        loadButton.alsoDo = {}
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }

    inner class LoadButton(palette: UIPalette, parent: UIElement<OpenRemixScreen>,
                           stage: Stage<OpenRemixScreen>)
        : Button<OpenRemixScreen>(palette, parent, stage) {

        @Volatile
        var alsoDo = {}

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            val remix = remix ?: return
            editor.remix = remix
            alsoDo()
            editor.remix.recomputeCachedData()
            (this@OpenRemixScreen.stage as GenericStage).onBackButtonClick()
            Gdx.app.postRunnable {
                System.gc()
            }
        }
    }
}
