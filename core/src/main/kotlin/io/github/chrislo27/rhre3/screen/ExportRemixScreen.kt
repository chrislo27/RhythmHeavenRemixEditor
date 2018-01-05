package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import de.sciss.jump3r.Main
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsMusic
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.attemptRememberDirectory
import io.github.chrislo27.rhre3.util.getDefaultDirectory
import io.github.chrislo27.rhre3.util.persistDirectory
import io.github.chrislo27.toolboks.Toolboks
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
import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.Clock
import net.beadsproject.beads.ugens.DelayTrigger
import net.beadsproject.beads.ugens.RangeLimiter
import net.beadsproject.beads.ugens.RecordToFile
import java.io.File
import java.util.*
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

    private enum class ExportFileType(val extension: String) {
        WAV("wav"), MP3("mp3");

        companion object {
            val VALUES: List<ExportFileType> by lazy { values().toList() }
            val EXTENSIONS: List<String> by lazy { VALUES.map(ExportFileType::extension) }
        }
    }

    private fun createFileChooser() =
            FileChooser().apply {
                this.initialDirectory = attemptRememberDirectory(main, PreferenceKeys.FILE_CHOOSER_EXPORT)
                        ?: getDefaultDirectory()
                val key = "screen.export.fileFilter"
                val extensions = arrayOf("*.wav", "*.mp3")

                this.extensionFilters.clear()
                val filter = FileChooser.ExtensionFilter(Localization[key], *extensions)

                this.extensionFilters += filter
                this.selectedExtensionFilter = this.extensionFilters.first()

                this.title = Localization["screen.export.fileChooserTitle"]
            }

    init {
        stage as GenericStage
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_export_big"))
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
    private fun export(file: File, fileType: ExportFileType) {
        if (isExporting || !isCapableOfExporting)
            return
        isExporting = true
        BeadsSoundSystem.isRealtime = false

        fun addBead(function: () -> Unit): Bead {
            return object : Bead() {
                override fun messageReceived(message: Bead?) {
                    function()
                }
            }
        }

        val maxProgressStages: Int = when (fileType) {
            ExportRemixScreen.ExportFileType.WAV -> 1
            ExportRemixScreen.ExportFileType.MP3 -> 2
        }

        fun updateProgress(localization: String, localPercent: Int, stage: Int) {
            mainLabel.text = Localization["screen.export.progress",
                    Localization["screen.export.progress.$localization"],
                    "$localPercent",
                    "$stage",
                    "$maxProgressStages",
                    "${Math.round((localPercent + (stage - 1) * 100f) / (maxProgressStages * 100f) * 100)}"]
        }

        // prepare
        val context = BeadsSoundSystem.audioContext
        BeadsSoundSystem.stop()
        context.stop()
        context.out.pause(true)
        context.out.clearDependents()

        // prep recorder
        val recorderFile = if (fileType != ExportFileType.WAV)
            File.createTempFile("rhre3-export-tmp-${System.currentTimeMillis()}", ".wav").apply {
                this.deleteOnExit()
            } else file
        val recorder = RecordToFile(context, 2, recorderFile, AudioFileFormat.Type.WAVE)
        val limiter = RangeLimiter(context, 2)
        recorder.addInput(limiter)
        limiter.addInput(context.out)
        context.out.addDependent(recorder)

        val oldStart = remix.playbackStart

        @Synchronized
        fun finalize(success: Boolean) {
            recorder.kill()
            limiter.kill()
            BeadsSoundSystem.stop()
            synchronized(context.out) {
                context.out.clearInputConnections()
                context.out.clearDependents()
                context.out.gain = 1f
                context.out.removeDependent(limiter)
            }
            remix.playbackStart = oldStart
            remix.playState = PlayState.STOPPED

            if (success) {
                when (fileType) {
                    ExportFileType.WAV -> {
                        // nothing
                    }
                    ExportFileType.MP3 -> {
                        val args = arrayOf("--ignore-tag-errors",
                                           "--tc", "Made with Rhythm Heaven Remix Editor ${RHRE3.VERSION}",
                                           recorderFile.path, file.path)
                        val main = Main()
                        main.support.addPropertyChangeListener("progress") { event ->
                            val percent: Int = (event.newValue as? Int) ?: 0
                            updateProgress("mp3", percent, 2)
                        }
                        main.run(args)
                    }
                }
            }

            BeadsSoundSystem.isRealtime = true
            BeadsSoundSystem.resume()

            if (success) {
                (GameRegistry.data.objectMap["mrUpbeatWii/applause"] as? Cue)
            } else {
                (GameRegistry.data.objectMap["mountainManeuver/toot"] as? Cue)
            }?.sound?.sound?.play(loop = false, pitch = 1f, rate = 1f, volume = 1f)
                    ?: Toolboks.LOGGER.warn("Export SFX (success=$success) not found")

            isExporting = false
        }

        try {
            // prep triggers
            val startMs = Math.min(remix.musicStartSec.toDouble(),
                                   remix.tempos.beatsToSeconds(
                                           remix.entities.minBy { it.bounds.x }?.bounds?.x ?: 0.0f).toDouble()) * 1000.0
            val endMs = remix.tempos.beatsToSeconds(remix.duration) * 1000.0
            val durationMs = endMs - startMs

            fun Float.beatToMsRelative(): Double = (remix.tempos.beatsToSeconds(this) * 1000.0) - startMs

            // reset things
            remix.playbackStart = remix.tempos.secondsToBeats(startMs.toFloat() / 1000f)
            remix.playState = PlayState.PLAYING
            remix.playState = PlayState.STOPPED

            // music trigger
            if (remix.music != null) {
                val music = BeadsMusic((remix.music!!.music as BeadsMusic).audio)
                music.stop()

                val musicStartMs = (remix.musicStartSec * 1000) - startMs

                context.out.addDependent(DelayTrigger(context, musicStartMs, addBead {
                    music.also(BeadsMusic::play)
                }))

                // volumes
                context.out.addDependent(Clock(context, 10f).apply {
                    addMessageListener(addBead {
                        music.setVolume(remix.musicVolumes.volumeAt(remix.beat))
                    })
                })
            }

            // SFX
            context.out.addDependent(Clock(context, (1f / 60f) * 1000).apply {
                addMessageListener(addBead {
                    remix.seconds = (context.time + startMs).toFloat() / 1000.0f
                    remix.entities.forEach {
                        remix.entityUpdate(it)
                    }

                    val percent = Math.round(context.time / (endMs - startMs) * 100).coerceIn(0, 100).toInt()
                    updateProgress("pcm", percent, 1)
                })
            })

            // scale gain
            context.out.gain = 0.5f

            // run!
            context.out.pause(false)
            context.runForNMillisecondsNonRealTime(durationMs + 500) // padding
        } catch (e: Exception) {
            e.printStackTrace()
            finalize(false)
            throw e
        }

        try {
            finalize(true)
        } catch (e: Exception) {
            e.printStackTrace()
            finalize(false)
            throw e
        }
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen && !isExporting && isCapableOfExporting) {
            Platform.runLater {
                isChooserOpen = true
                val fileChooser = createFileChooser()
                val file: File? = fileChooser.showSaveDialog(null)
                isChooserOpen = false
                if (file != null && main.screen == this) {
                    fileChooser.initialDirectory = if (!file.isDirectory) file.parentFile else file
                    persistDirectory(main, PreferenceKeys.FILE_CHOOSER_EXPORT, fileChooser.initialDirectory)
                    launch(CommonPool) {
                        try {
                            val correctFile = if (file.extension.toLowerCase(Locale.ROOT) !in ExportFileType.EXTENSIONS)
                                file.parentFile.resolve("${file.name}.wav")
                            else
                                file
                            val fileType = ExportFileType.VALUES.firstOrNull {
                                it.extension == file.extension.toLowerCase(Locale.ROOT)
                            } ?: ExportFileType.WAV

                            export(correctFile, fileType)

                            mainLabel.text = Localization["screen.export.success"]
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            updateLabels(t)
                            isExporting = false
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

    override fun hide() {
        super.hide()
        BeadsSoundSystem.stop()
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}