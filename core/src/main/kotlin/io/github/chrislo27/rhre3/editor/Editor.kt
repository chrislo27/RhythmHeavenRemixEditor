package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.theme.DarkTheme
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class Editor(val main: RHRE3Application, stageCamera: OrthographicCamera)
    : Disposable {

    companion object {
        const val ENTITY_HEIGHT: Float = 48f
        const val ENTITY_WIDTH: Float = ENTITY_HEIGHT * 4

        const val ICON_SIZE: Float = 32f
        const val ICON_PADDING: Float = 6f
        const val ICON_COUNT_X: Int = 13
        const val ICON_COUNT_Y: Int = 4

        const val TRACK_COUNT: Int = 5
        const val TRACK_LINE: Float = 2f
        const val PATTERN_COUNT: Int = 5

        const val MESSAGE_BAR_HEIGHT: Int = 14
        const val BUTTON_SIZE: Float = 32f
        const val BUTTON_PADDING: Float = 4f
        const val BUTTON_BAR_HEIGHT: Float = BUTTON_SIZE + BUTTON_PADDING * 2

        val TRANSLUCENT_BLACK: Color = Color(0f, 0f, 0f, 0.5f)
        val ARROWS: List<String> = listOf("▲", "▼", "△", "▽", "➡")
        val SELECTED_TINT: Color = Color(0.65f, 1f, 1f, 1f)
        val CUE_PATTERN_COLOR: Color = Color(0.65f, 0.65f, 0.65f, 1f)
    }

    val camera: OrthographicCamera by lazy {
        val c = OrthographicCamera()
        c.setToOrtho(false, RHRE3.WIDTH.toFloat(), RHRE3.HEIGHT.toFloat())
        c.position.x = 0f
        c.update()
        c
    }

    val pickerSelection: PickerSelection = PickerSelection()
    var remix: Remix = Remix(camera)
    val stage: EditorStage = EditorStage(null, stageCamera, main, this)
    val batch: SpriteBatch
        get() = main.batch

    var theme: Theme = DarkTheme()

    fun toScaleX(float: Float): Float =
            (float / RHRE3.WIDTH) * camera.viewportWidth

    fun toScaleY(float: Float): Float =
            (float / RHRE3.HEIGHT) * camera.viewportHeight

    fun getBeatRange(): IntRange =
            Math.round((camera.position.x - camera.viewportWidth / 2 * camera.zoom) / toScaleX(
                    ENTITY_WIDTH)) - 1..(Math.round(
                    (camera.position.x + camera.viewportWidth / 2 * camera.zoom) / toScaleX(ENTITY_WIDTH)) + 1)

    /**
     * Pre-stage render.
     */
    fun render() {
        val bgColour = theme.background
        Gdx.gl.glClearColor(bgColour.r, bgColour.g, bgColour.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.position.y = (camera.viewportHeight / 2) -
                (stage.topOfMinimapBar + (stage.centreAreaStage.location.realHeight / 2) - toScaleY(
                        (TRACK_COUNT + 1) * ENTITY_HEIGHT / 2f))
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()

        val beatRange = getBeatRange()
        val font = main.defaultFont

        // horizontal track lines
        run trackLines@ {
            batch.color = theme.trackLine
            val startX = toScaleX(beatRange.start.toFloat() * ENTITY_WIDTH)
            val width = toScaleX(beatRange.endInclusive.toFloat() * ENTITY_WIDTH) - startX
            for (i in 0..TRACK_COUNT) {
                batch.fillRect(startX, toScaleY(i * ENTITY_HEIGHT), width,
                               toScaleY(TRACK_LINE))
            }
            batch.setColor(1f, 1f, 1f, 1f)
        }

        // beat lines
        run beatLines@ {
            for (i in beatRange) {
                // TODO time signature based
                if (i % 4 == 0) {
                    batch.color = theme.trackLine
                } else {
                    batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.25f)
                }
                batch.fillRect(toScaleX(i * ENTITY_WIDTH), 0f, toScaleX(TRACK_LINE),
                               toScaleY(TRACK_COUNT * ENTITY_HEIGHT + TRACK_LINE))
            }
            batch.setColor(1f, 1f, 1f, 1f)
        }

        // beat numbers
        run beatLines@ {
            for (i in beatRange) {
                font.color = theme.trackLine
                font.drawCompressed(batch, "$i",
                                    toScaleX(i * ENTITY_WIDTH - ENTITY_WIDTH / 4f),
                                    toScaleY(TRACK_COUNT * ENTITY_HEIGHT + TRACK_LINE + TRACK_LINE) + font.capHeight,
                                    toScaleX(ENTITY_WIDTH / 2f), Align.center)

                // TODO time signature based
                if (Math.floorMod(i, 4) == 0) {
                    val measureNum = i / 4 + 1
                    if (measureNum >= 1) {
                        font.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.5f)
                        font.drawCompressed(batch, "$measureNum",
                                            toScaleX(i * ENTITY_WIDTH - ENTITY_WIDTH / 4f),
                                            toScaleY(
                                                    TRACK_COUNT * ENTITY_HEIGHT + TRACK_LINE + TRACK_LINE) + font.capHeight + font.lineHeight,
                                            toScaleX(ENTITY_WIDTH / 2f), Align.center)
                    }
                }
            }
            font.setColor(1f, 1f, 1f, 1f)
        }

        // remix
        remix.render(batch)

        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
        batch.begin()


        batch.end()

    }

    fun postStageRender() {

    }

    fun renderUpdate() {
        val shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
        val control = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(
                Input.Keys.CONTROL_RIGHT)
        val left = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val accelerateCamera = shift || control
        val cameraDelta = toScaleX(ENTITY_WIDTH * 5 * Gdx.graphics.deltaTime * if (accelerateCamera) 5 else 1)

        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            Gdx.input.inputProcessor.scrolled(-1)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            Gdx.input.inputProcessor.scrolled(1)
        }

        run camera@ {
            if (left) {
                camera.position.x -= cameraDelta
            }
            if (right) {
                camera.position.x += cameraDelta
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
                camera.position.x = 0f
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
                // TODO
            }
        }

    }

    override fun dispose() {
    }

    fun resize(width: Int, height: Int) {
        stage.updatePositions()
        camera.setToOrtho(false, main.defaultCamera.viewportWidth, main.defaultCamera.viewportHeight)
        camera.update()
    }
}