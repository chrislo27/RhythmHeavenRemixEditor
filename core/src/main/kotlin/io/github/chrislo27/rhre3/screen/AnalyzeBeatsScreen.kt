package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import ddf.minim.analysis.BeatDetect
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsMusic
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.TextLabel


class AnalyzeBeatsScreen(main: RHRE3Application, private val editor: Editor) : ToolboksScreen<RHRE3Application, AnalyzeBeatsScreen>(main) {

    override val stage: GenericStage<AnalyzeBeatsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val remix: Remix get() = editor.remix

    init {
        val palette = stage.palette
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_cuenumber"))
        stage.titleLabel.text = "screen.analyzeBeats.title"
        stage.backButton.enabled = true
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        val music = remix.music?.music as? BeadsMusic
        if (music == null) {
            stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
                this.text = "screen.analyzeBeats.noMusic"
                this.isLocalizationKey = true
            }
        } else {
            val sample = music.audio.sample
            val sampleRate = sample.sampleRate
            val samplesPerAnalysis = 1024
            val sampleArray: FloatArray = FloatArray(sample.numChannels)
            val floatArray: FloatArray = FloatArray(samplesPerAnalysis)
            val beatDetect = BeatDetect(samplesPerAnalysis, sampleRate).apply {
                this.setSensitivity(200)
                this.detectMode(BeatDetect.SOUND_ENERGY)
            }

            println("Starting analysis with ${sample.numFrames} frames")

            val time = System.nanoTime()
            for (frame in 0 until sample.numFrames step samplesPerAnalysis.toLong()) {
//                println("Analyzing frames $frame to ${frame + samplesPerAnalysis} - ${sample.samplesToMs(frame.toDouble())} to ${sample.samplesToMs(frame.toDouble() + samplesPerAnalysis)} ms")
                for (i in 0 until samplesPerAnalysis) {
                    if ((frame + i) >= sample.numFrames) {
                        floatArray[i] = 0f
                    } else {
                        sample.getFrame((frame + i).toInt(), sampleArray)
                        floatArray[i] = sampleArray.average().toFloat()
                    }
                }

                beatDetect.detect(floatArray)

                if (beatDetect.isOnset || beatDetect.isKick) {
                    println("Detected beat at frame $frame, ${sample.samplesToMs(frame.toDouble())} ms")
                    remix.entities += CueEntity(remix, GameRegistry.data.objectMap["countInEn/cowbell"]!! as Cue).apply {
                        this.updateBounds {
                            bounds.x = remix.tempos.secondsToBeats(sample.samplesToMs(frame.toDouble()).toFloat() / 1000f)
                        }
                    }
                }
            }
            val end = System.nanoTime()
            println("Took ${(end - time) / 1000000f} ms to analyze")
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.enabled) {
            stage.backButton.onLeftClick(0f, 0f)
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}