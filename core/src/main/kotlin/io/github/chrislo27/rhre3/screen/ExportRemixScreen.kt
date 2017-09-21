package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsMusic
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
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
import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.Clock
import net.beadsproject.beads.ugens.DelayTrigger
import net.beadsproject.beads.ugens.RecordToFile
import java.io.File
import javax.sound.sampled.AudioFileFormat


class ExportRemixScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, ExportRemixScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: Stage<ExportRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val remix: Remix
        get() = editor.remix

    @Volatile private var isChooserOpen = false
        set(value) {
            field = value
            stage as GenericStage
            stage.backButton.enabled = !isChooserOpen
        }
    @Volatile private var isExporting = false
    private var isCapableOfExporting = false
    private val mainLabel: TextLabel<ExportRemixScreen>

    private val fileChooser: FileChooser = FileChooser().apply {
        this.initialDirectory = attemptRememberDirectory(main, PreferenceKeys.FILE_CHOOSER_EXPORT)
                ?: getDefaultDirectory()
        val key = "screen.export.fileFilter"
        val extensions = arrayOf("*.wav")

        fun applyLocalizationChanges() {
            this.extensionFilters.clear()
            val filter = FileChooser.ExtensionFilter(Localization[key], *extensions)

            this.extensionFilters += filter
            this.selectedExtensionFilter = this.extensionFilters.first()

            this.title = Localization["screen.export.fileChooserTitle"]
        }

        applyLocalizationChanges()

        Localization.listeners += { old ->
            applyLocalizationChanges()
        }
    }

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_saveremix"))
        stage.titleLabel.text = "screen.export.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen && !isExporting) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette
        stage.centreStage.elements += object : TextLabel<ExportRemixScreen>(palette, stage.centreStage,
                                                                            stage.centreStage) {
            override fun frameUpdate(screen: ExportRemixScreen) {
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
            (stage as GenericStage).onBackButtonClick()
        }
    }

    @Synchronized
    private fun export(file: File) {
        if (isExporting || !isCapableOfExporting)
            return
        isExporting = true

        fun addBead(function: () -> Unit): Bead {
            return object : Bead() {
                override fun messageReceived(message: Bead?) {
                    function()
                }
            }
        }

        // prepare
        val context = BeadsSoundSystem.audioContext
        context.stop()
        context.out.clearInputConnections()
        context.out.clearDependents()

        // prep recorder
        val recorder = RecordToFile(context, 2, file, AudioFileFormat.Type.WAVE)
        recorder.addInput(context.out)
        context.out.addDependent(recorder)

        val oldStart = remix.playbackStart

        fun finalize() {
            recorder.kill()
            context.stop()
            context.out.clearInputConnections()
            context.out.clearDependents()
            context.out.gain = 1f
            context.start()
            remix.playbackStart = oldStart
            remix.playState = PlayState.STOPPED
            isExporting = false
        }

        try {
            // prep triggers
            val startMs = Math.min(remix.musicStartSec.toDouble(),
                                   remix.tempos.beatsToSeconds(remix.entities.minBy { it.bounds.x }?.bounds?.x ?: 0.0f).toDouble()) * 1000.0
            val endMs = remix.tempos.beatsToSeconds(remix.duration) * 1000.0
            val durationMs = endMs - startMs

            fun Float.beatToMsRelative(): Double = (remix.tempos.beatsToSeconds(this) * 1000.0) - startMs

            // reset things
            remix.playbackStart = remix.tempos.secondsToBeats(startMs.toFloat() / 1000f)
            remix.playState = PlayState.PLAYING
            remix.playState = PlayState.STOPPED

            // music trigger
            if (remix.music != null) {
                val music = (remix.music!!.music as BeadsMusic)
                music.stop()

                context.out.addDependent(DelayTrigger(context, (remix.musicStartSec * 1000) - startMs, addBead {
                    music.also(BeadsMusic::play)
                }))
                // volumes
                remix.musicVolumes.getBackingMap().values.forEach {
                    context.out.addDependent(DelayTrigger(context, it.beat.beatToMsRelative(), addBead {
                        music.setVolume(it.volume / 100f)
                    }))
                }
            }

            // SFX
            context.out.addDependent(Clock(context, (1f / 60f) * 1000).apply {
                addMessageListener(addBead {
                    remix.seconds = (context.time + startMs).toFloat() / 1000.0f
                    remix.entities.forEach(remix::entityUpdate)

                    val percent = Math.round(context.time / (endMs - startMs) * 100).coerceIn(0, 100)
                    mainLabel.text = Localization["screen.export.progress", "$percent"]
                })
            })

            // scale gain
            context.out.gain = 0.5f

            // run!
            context.out.start()
            context.runForNMillisecondsNonRealTime(durationMs + 500) // padding
        } catch (e: Exception) {
            e.printStackTrace()
            finalize()
            throw e
        }

        finalize()
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen && !isExporting && isCapableOfExporting) {
            Platform.runLater {
                isChooserOpen = true
                val file: File? = fileChooser.showSaveDialog(JavafxStub.application.primaryStage)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_EXPORT, fileChooser.initialDirectory)
                    try {
                        val correctFile = if (file.extension != "wav")
                            file.parentFile.resolve("${file.name}.wav")
                        else
                            file

                        export(correctFile)

                        mainLabel.text = Localization["screen.export.success", correctFile.length() / (1024)]
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        updateLabels(t)
                        isExporting = false
                    }
                } else {
                    (stage as GenericStage).onBackButtonClick()
                }
            }
        }
    }

    private fun updateLabels(throwable: Throwable? = null) {
        val label = mainLabel
        val hasEndRemix = remix.duration < Float.POSITIVE_INFINITY
        val isBeads = SoundSystem.system == BeadsSoundSystem
        isCapableOfExporting = isBeads && hasEndRemix
        if (!isCapableOfExporting) {
            if (!isBeads) {
                label.text = Localization["screen.export.cannot", Localization["screen.export.needsBeadsSound"]]
            } else if (!hasEndRemix) {
                label.text = Localization["screen.export.cannot", Localization["screen.export.needsEndRemix"]]
            }
        } else {
            if (throwable == null) {
                label.text = ""
            } else {
                label.text = Localization["screen.export.failed", throwable::class.java.canonicalName]
            }
        }
    }

    override fun show() {
        super.show()
        updateLabels()
        openPicker()
        updateLabels()
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}