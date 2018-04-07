package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ILoadsSounds
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import java.time.LocalDate
import kotlin.math.roundToInt


class EventScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, EventScreen>(main), HidesVersionText {

    enum class EventType {
        NONE, ANNIVERSARY
    }

    companion object {
        private val NOW: LocalDate = LocalDate.now()

        fun getPossibleEvent(main: RHRE3Application, nextScreen: ToolboksScreen<*, *>?): EventScreen? {
            val anniversaryButNow: LocalDate = RHRE3.RHRE_ANNIVERSARY.withYear(NOW.year)

            // Event check
            val today: LocalDate = when {
                RHRE3.immediateAnniversary -> anniversaryButNow
                else -> LocalDate.now()
            }

            return when {
                // RHRE anniversary:
                // Occurs from day of to 3 days after (exclusive)
                today == anniversaryButNow || (today.isAfter(anniversaryButNow)
                        && today.isBefore(anniversaryButNow.plusDays(3))) -> {
                    EventScreen(main).takeIf {
                        it.loadEventJson(EventType.ANNIVERSARY,
                                         Gdx.files.internal("event/anniversary.json"), nextScreen)
                    }
                }
                else -> null
            }
        }
    }

    private var nextScreen: ToolboksScreen<*, *>? = null
    private var eventType = EventType.NONE
    private val editor = Editor(main, main.defaultCamera)
    private val remix: Remix
        get() = editor.remix
    private var canUpdate = 0
    private var canContinue = -1f
    private val camera = OrthographicCamera().apply {
        viewportWidth = RHRE3.WIDTH.toFloat()
        viewportHeight = RHRE3.HEIGHT.toFloat()
        position.x = viewportWidth / 2f
        position.y = viewportHeight / 2f
        update()
    }

    fun loadEventJson(eventType: EventType, file: FileHandle, nextScreen: ToolboksScreen<*, *>?): Boolean {
        return try {
            val loadInfo = Remix.fromJson(JsonHandler.OBJECT_MAPPER.readTree(file.readString("UTF-8")) as ObjectNode,
                                          editor.createRemix())
            editor.remix = loadInfo.remix
            remix.playbackStart = -1f
            remix.playState = PlayState.STOPPED

            this.nextScreen = nextScreen
            this.eventType = eventType
            this.canContinue = if (main.preferences.getInteger(PreferenceKeys.EVENT_PREFIX + eventType.name,
                                                               0) == NOW.year) 0f else -1f
            main.preferences.putInteger(PreferenceKeys.EVENT_PREFIX + eventType.name, NOW.year).flush()
            this.canUpdate = 0
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun render(delta: Float) {
        super.render(delta)

        val batch = main.batch
        val oldProj = batch.projectionMatrix
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)

        if (remix.midiInstruments > 0) {
            // each cell is 88x136 with a 1 px black border
            val texture = AssetRegistry.get<Texture>("glee_club")
            val width = 86
            val cellHeight = 134
            val height = cellHeight - 15
            val renderWidth = width * 0.75f

            val startX = (camera.viewportWidth / 2f - renderWidth * remix.midiInstruments / 2f - renderWidth / 4f).coerceAtLeast(
                    0f)
            val startY = camera.viewportHeight / 2 - height / 2

            for (i in 0 until remix.midiInstruments) {
                val playingEntity: Entity? = remix.entities.find {
                    it.playbackCompletion == PlaybackCompletion.PLAYING && it is CueEntity && it.instrument == i + 1
                }
                val isPlaying = remix.playState != PlayState.STOPPED && playingEntity != null
                val animation = if (isPlaying) {
                    MathUtils.lerp(0f, 3f, MathHelper.getSawtoothWave(0.2f)).roundToInt().coerceAtMost(2)
                } else {
                    MathHelper.getSawtoothWave(0.2f).roundToInt()
                }

                batch.draw(texture, startX + i * (if (startX <= 0f)
                    camera.viewportWidth / remix.midiInstruments
                else renderWidth), startY + (i * -height * 0.1f), width.toFloat(),
                           height.toFloat(),
                           1 + (animation * (width + 2)),
                           1 + (if (isPlaying) ((cellHeight + 2) * (if (isPlaying && playingEntity != null && playingEntity is IRepitchable && playingEntity.semitone > IRepitchable.DEFAULT_RANGE.endInclusive) 4 else 3)) else 0),
                           width, height, false, false)
            }
        }

        if (canContinue > 0f) {
            val font = main.defaultBorderedFont
            val fontAlpha = (canContinue * 2).coerceIn(0f, 1f)
            font.setColor(1f, 1f, 1f, fontAlpha)
            with(editor) {
                font.scaleFont(this@EventScreen.camera)
            }

            // keystroke text
            font.drawCompressed(batch, "[ENTER]", 0f, camera.viewportHeight * 0.15f, camera.viewportWidth, Align.center)

            when (eventType) {
                EventType.ANNIVERSARY -> {
                    val baseY = camera.viewportHeight * 0.8f
                    val years = NOW.year - RHRE3.RHRE_ANNIVERSARY.year
                    val rhre = "RHRE"
                    val layout = font.drawCompressed(batch,
                                                     "Happy $years${getNumberSuffix(years)} [X]$rhre[] Anniversary!",
                                                     0f, baseY, camera.viewportWidth, Align.center)
                    val logo = AssetRegistry.get<Texture>("logo_32")

                    val indexOfR = layout.runs.indexOfFirst { it.color.a == 0.0f }
                    if (indexOfR != -1) {
                        val run = layout.runs[indexOfR]
                        batch.setColor(1f, 1f, 1f, fontAlpha)
                        batch.draw(logo, run.x, run.y + baseY - font.capHeight / 2 - run.width / 2, run.width,
                                   run.width)
                        batch.setColor(1f, 1f, 1f, 1f)
                    }

                    font.drawCompressed(batch, "Thank you for your continued support over the years.", 0f,
                                        baseY - font.lineHeight * 2, camera.viewportWidth, Align.center)
                    font.drawCompressed(batch, "Here's to more RHRE!", 0f,
                                        baseY - font.lineHeight * 3, camera.viewportWidth, Align.center)
                }
                else -> {
                }
            }

            font.setColor(1f, 1f, 1f, 1f)
            with(editor) {
                font.unscaleFont()
            }
        }

        batch.end()
        batch.projectionMatrix = oldProj
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (canUpdate < 2) {
            canUpdate++
        } else {
            remix.timeUpdate(Gdx.graphics.deltaTime * (if (Toolboks.debugMode && Gdx.input.isKeyPressed(Input.Keys.S)) 2f else 1f))
        }

        if (remix.beat >= remix.lastPoint && canContinue < 0f) {
            canContinue = 0f
        }

        if (canContinue in 0f..1f) {
            canContinue += Gdx.graphics.deltaTime
        }

        if (canContinue >= 0f && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(
                        Input.Keys.SPACE))) {
            main.screen = nextScreen
        }

        if (Toolboks.debugMode && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            remix.playState = PlayState.STOPPED
            remix.playState = PlayState.PLAYING
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun show() {
        super.show()
        remix.entities.forEach { entity ->
            if (entity is ILoadsSounds) {
                entity.loadSounds()
            }
        }
        remix.playState = PlayState.PLAYING
        canUpdate = 1
    }

    override fun hide() {
        super.hide()
        remix.playState = PlayState.STOPPED
    }

    override fun getDebugString(): String? {
        return "type: $eventType\ncanContinue: $canContinue\nnext: ${nextScreen?.let { it::class.java.canonicalName }}"
    }

    private fun getNumberSuffix(number: Int): String {
        return when {
            number in 11..19 -> "th"
            number % 10 == 1 -> "st"
            number % 10 == 2 -> "nd"
            number % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}