package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.playalong.ControllerInput
import io.github.chrislo27.rhre3.playalong.ControllerMapping
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.screen.HidesVersionText
import io.github.chrislo27.rhre3.screen.PlayalongSettingsScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.util.WipeFrom
import io.github.chrislo27.rhre3.util.WipeTo
import io.github.chrislo27.rhre3.util.scaleFont
import io.github.chrislo27.rhre3.util.unscaleFont
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import org.eclipse.jgit.errors.NotSupportedException
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class RhythmGameScreen(main: RHRE3Application, val game: RhythmGame)
    : ToolboksScreen<RHRE3Application, RhythmGameScreen>(main), HidesVersionText, ControllerListener {

    data class PauseState(val lastPlayState: PlayState)

    companion object {
        private val PAUSED_TITLE_MATRIX: Matrix4 = Matrix4().rotate(0f, 0f, 1f, 20.556f).translate(220f, 475f, 0f)
        private val TMP_CAMERA_MATRIX: Matrix4 = Matrix4()
    }

    override val stage: Stage<RhythmGameScreen> = Stage(null, main.defaultCamera, 1280f, 720f)
    private val playStage: Stage<RhythmGameScreen> = Stage(stage, stage.camera, stage.pixelsWidth, stage.pixelsHeight)
    private val pauseStage: Stage<RhythmGameScreen> = Stage(stage, stage.camera, stage.pixelsWidth, stage.pixelsHeight)
    private val resumeButton: Button<RhythmGameScreen>
    private val settingsButton: Button<RhythmGameScreen>
    private val quitButton: Button<RhythmGameScreen>
    private val controlsLabel: TextLabel<RhythmGameScreen>

    private var paused: PauseState? = null

    private var isCursorInvisible = false
    private var goingToSettings = false

    private val pauseCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }

    init {
        val palette = main.uiPalette
        stage.tooltipElement = TextLabel(palette.copy(backColor = Color(0f, 0f, 0f, 0.75f), fontScale = 0.75f), stage, stage).apply {
            this.textAlign = Align.center
            this.background = true
        }

        stage.elements += playStage

        pauseStage.visible = false
        resumeButton = Button(palette, pauseStage, pauseStage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 275f, pixelHeight = 64f, screenX = 0.025f, screenWidth = 0.25f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "extras.playing.pauseMenu.resume"
                this.location.set(pixelX = 4f, pixelWidth = -8f)
            })
            this.leftClickAction = { _, _ ->
                pauseUnpause()
            }
        }
        pauseStage.elements += resumeButton
        settingsButton = Button(palette, pauseStage, pauseStage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 200f, pixelHeight = 64f, screenX = 0.025f, screenWidth = 0.25f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.playalongSettings.title"
                this.location.set(pixelX = 4f, pixelWidth = -8f)
            })
            this.leftClickAction = { _, _ ->
                goingToSettings = true
                main.screen = PlayalongSettingsScreen(main, this@RhythmGameScreen)
            }
        }
        pauseStage.elements += settingsButton
        quitButton = Button(palette, pauseStage, pauseStage).apply {
            this.location.set(screenY = 0f, screenHeight = 0f, pixelY = 125f, pixelHeight = 64f, screenX = 0.025f, screenWidth = 0.25f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "extras.playing.pauseMenu.quit"
                this.location.set(pixelX = 4f, pixelWidth = -8f)
            })
            this.leftClickAction = { _, _ ->
                game.playState = PlayState.STOPPED
                game.playState = PlayState.PAUSED
                playSelectSfx()
                onQuit()
            }
        }
        pauseStage.elements += quitButton
        controlsLabel = TextLabel(palette.copy(ftfont = main.defaultBorderedFontFTF), pauseStage, pauseStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = ""
            this.textAlign = Align.right
            this.location.set(screenX = 0f, screenY = 0f, screenWidth = 0f, screenHeight = 0f,
                              pixelY = 1f, pixelHeight = 32f, pixelX = 410f, pixelWidth = 870f - 4f)
        }
        pauseStage.elements += controlsLabel

        stage.elements += pauseStage
        stage.updatePositions()
    }

    override fun render(delta: Float) {
        val batch = main.batch

        game.render(main, batch)
        batch.packedColor = Color.WHITE_FLOAT_BITS

        pauseStage.visible = paused != null
        playStage.visible = paused == null

        if (paused != null) {
            val shapeRenderer = main.shapeRenderer
            pauseCamera.update()
            batch.projectionMatrix = pauseCamera.combined
            shapeRenderer.projectionMatrix = pauseCamera.combined
            batch.begin()

            batch.setColor(1f, 1f, 1f, 0.5f)
            batch.fillRect(0f, 0f, 1280f, 720f)
            batch.setColor(1f, 1f, 1f, 1f)

            shapeRenderer.prepareStencilMask(batch) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                shapeRenderer.triangle(0f, 720f, 1280f * 0.75f, 720f, 0f, 720f * 0.5f)
                shapeRenderer.triangle(1280f * 0.25f, 0f, 1280f, 0f, 1280f, 720f * 0.5f)
                shapeRenderer.end()
            }.useStencilMask {
                batch.setColor(1f, 1f, 1f, 1f)
                val tex: Texture = AssetRegistry["bg_tile"]
                val period = 5f
                val start: Float = MathHelper.getSawtoothWave(period)
                val speed = 1f

                val w = tex.width
                val h = tex.height
                for (x in (start * w * speed - (w * speed.absoluteValue)).toInt()..pauseCamera.viewportWidth.roundToInt() step w) {
                    for (y in (start * h * speed - (h * speed.absoluteValue)).toInt()..pauseCamera.viewportHeight.roundToInt() step h) {
                        batch.draw(tex, x.toFloat(), y.toFloat(), w * 1f, h * 1f)
                    }
                }
            }

            val titleFont = main.defaultBorderedFontLarge
            titleFont.scaleFont(pauseCamera)
            titleFont.scaleMul(0.9f)
            batch.projectionMatrix = TMP_CAMERA_MATRIX.set(pauseCamera.combined).mul(PAUSED_TITLE_MATRIX)
            titleFont.setColor(0.9f, 0.85f, 0.25f, 1f)
            titleFont.drawCompressed(batch, Localization["extras.playing.pauseMenu.title"], 0f, 0f, 475f, Align.center)
            titleFont.setColor(1f, 1f, 1f, 1f)
            batch.projectionMatrix = pauseCamera.combined
            titleFont.unscaleFont()

            batch.end()
            shapeRenderer.projectionMatrix = main.defaultCamera.combined
            batch.projectionMatrix = main.defaultCamera.combined
        }

        super.render(delta)
    }

    fun pauseUnpause() {
        val paused = this.paused
        if (paused == null) {
            if (!game.onPauseTriggered()) {
                this.paused = PauseState(game.playState)
                game.playState = PlayState.PAUSED
                game.onPauseMenuStateChange(true)
                AssetRegistry.get<Sound>("sfx_pause_enter").play()
            }
        } else {
            this.paused = null
            game.playState = paused.lastPlayState
            game.onPauseMenuStateChange(false)
            AssetRegistry.get<Sound>("sfx_pause_exit").play()
        }
    }

    private fun playSelectSfx() {
        AssetRegistry.get<Sound>("sfx_select").play()
    }

    private fun onQuit() {
        main.screen = TransitionScreen(main, main.screen, ScreenRegistry["info"], WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
    }

    private fun onEnd() {
        throw NotSupportedException("Non-endless games are not supported yet")
        // Transition away to proper results
//        val score = engine.computeScore()
//        if (robotEnabled) {
//            main.screen = TransitionScreen(main, main.screen,
//                                           GameSelectScreen(main),
//                                           FadeOut(0.5f, Color.BLACK), WipeFrom(Color.BLACK, 0.35f))
//        } else {
//            main.screen = TransitionScreen(main, main.screen,
//                                           ResultsScreen(main, score),
//                                           FadeOut(1f, Color.BLACK), null)
//        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        val delta = Gdx.graphics.deltaTime

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseUnpause()
        }

        if (paused == null) {
            // Update inputs
            val controls = Playalong.playalongControls
            val inputSet = game.pressedInputs
            RhythmGame.InputButtons.VALUES.forEach { inp ->
                val inSet = inp in inputSet
                val keyPressed = Gdx.input.isKeyPressed(inp.getInputKey(controls))
                if (inSet != keyPressed) {
                    if (inSet) {
                        inputSet.remove(inp)
                    } else {
                        inputSet.add(inp)
                    }
                    game.onInput(inp, !keyPressed)
                }
            }

            game.update(delta)
            if (game.playState == PlayState.STOPPED) {
                onEnd()
            }
        }

        if (paused == null && main.screen == this) {
            if (!isCursorInvisible) {
                isCursorInvisible = true
                Gdx.graphics.setCursor(AssetRegistry["cursor_invisible"])
            }
        } else {
            if (isCursorInvisible) {
                isCursorInvisible = false
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
            }
        }
    }

    override fun show() {
        super.show()
        controlsLabel.text = Playalong.playalongControls.toInputString()
        Controllers.addListener(this)
        Playalong.loadActiveMappings()
        DiscordHelper.updatePresence(PresenceState.PlayingEndlessGame(game.gameName))
    }

    override fun hide() {
        super.hide()
        Controllers.removeListener(this)
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        if (!goingToSettings) {
            dispose()
        } else {
            goingToSettings = false
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        game.dispose()
    }

    override fun getDebugString(): String? {
        return "game: ${game::class.java.canonicalName}\n${game.getDebugString()}"
    }

    private fun getMapping(controller: Controller): ControllerMapping? = Playalong.activeControllerMappings[controller]

    override fun buttonDown(controller: Controller, buttonCode: Int): Boolean {
        val mapping = getMapping(controller) ?: return false
        val buttonA = mapping.buttonA
        val buttonB = mapping.buttonB
        val buttonLeft = mapping.buttonLeft
        val buttonRight = mapping.buttonRight
        val buttonUp = mapping.buttonUp
        val buttonDown = mapping.buttonDown

        val inputSet = game.pressedInputs
        fun trigger(inp: RhythmGame.InputButtons) {
            val inSet = inp in inputSet
            if (!inSet) {
                inputSet.add(inp)
                game.onInput(inp, false)
            }
        }

        var any = false
        if (buttonA is ControllerInput.Button && buttonA.code == buttonCode) {
            trigger(RhythmGame.InputButtons.A)
            any = true
        }
        if (buttonB is ControllerInput.Button && buttonB.code == buttonCode) {
            trigger(RhythmGame.InputButtons.B)
            any = true
        }
        if (buttonLeft is ControllerInput.Button && buttonLeft.code == buttonCode) {
            trigger(RhythmGame.InputButtons.LEFT)
            any = true
        }
        if (buttonRight is ControllerInput.Button && buttonRight.code == buttonCode) {
            trigger(RhythmGame.InputButtons.RIGHT)
            any = true
        }
        if (buttonUp is ControllerInput.Button && buttonUp.code == buttonCode) {
            trigger(RhythmGame.InputButtons.UP)
            any = true
        }
        if (buttonDown is ControllerInput.Button && buttonDown.code == buttonCode) {
            trigger(RhythmGame.InputButtons.DOWN)
            any = true
        }
        return any && game.playState == PlayState.PLAYING
    }

    override fun buttonUp(controller: Controller, buttonCode: Int): Boolean {
        val mapping = getMapping(controller) ?: return false
        val buttonA = mapping.buttonA
        val buttonB = mapping.buttonB
        val buttonLeft = mapping.buttonLeft
        val buttonRight = mapping.buttonRight
        val buttonUp = mapping.buttonUp
        val buttonDown = mapping.buttonDown
        val inputSet = game.pressedInputs
        fun trigger(inp: RhythmGame.InputButtons) {
            val inSet = inp in inputSet
            if (inSet) {
                inputSet.remove(inp)
                game.onInput(inp, true)
            }
        }

        var any = false
        if (buttonA is ControllerInput.Button && buttonA.code == buttonCode) {
            trigger(RhythmGame.InputButtons.A)
            any = true
        }
        if (buttonB is ControllerInput.Button && buttonB.code == buttonCode) {
            trigger(RhythmGame.InputButtons.B)
            any = true
        }
        if (buttonLeft is ControllerInput.Button && buttonLeft.code == buttonCode) {
            trigger(RhythmGame.InputButtons.LEFT)
            any = true
        }
        if (buttonRight is ControllerInput.Button && buttonRight.code == buttonCode) {
            trigger(RhythmGame.InputButtons.RIGHT)
            any = true
        }
        if (buttonUp is ControllerInput.Button && buttonUp.code == buttonCode) {
            trigger(RhythmGame.InputButtons.UP)
            any = true
        }
        if (buttonDown is ControllerInput.Button && buttonDown.code == buttonCode) {
            trigger(RhythmGame.InputButtons.DOWN)
            any = true
        }
        return any && game.playState == PlayState.PLAYING
    }

    override fun povMoved(controller: Controller, povCode: Int, value: PovDirection): Boolean {
        val mapping = getMapping(controller) ?: return false
        val buttonA = mapping.buttonA
        val buttonB = mapping.buttonB
        val buttonLeft = mapping.buttonLeft
        val buttonRight = mapping.buttonRight
        val buttonUp = mapping.buttonUp
        val buttonDown = mapping.buttonDown
        var any = false
        val release = value == PovDirection.center
        val inputSet = game.pressedInputs
        fun trigger(inp: RhythmGame.InputButtons) {
            val inSet = inp in inputSet
            if (release && inSet) {
                if (inputSet.remove(inp)) {
                    game.onInput(inp, true)
                }
            } else if (!release && !inSet) {
                if (inputSet.add(inp)) {
                    game.onInput(inp, false)
                }
            }
        }
        if (buttonA is ControllerInput.Pov && buttonA.povCode == povCode && (buttonA.direction == value || release)) {
            trigger(RhythmGame.InputButtons.A)
            any = true
        }
        if (buttonB is ControllerInput.Pov && buttonB.povCode == povCode && (buttonB.direction == value || release)) {
            trigger(RhythmGame.InputButtons.B)
            any = true
        }
        if (buttonLeft is ControllerInput.Pov && buttonLeft.povCode == povCode && (buttonLeft.direction == value || release)) {
            trigger(RhythmGame.InputButtons.LEFT)
            any = true
        }
        if (buttonRight is ControllerInput.Pov && buttonRight.povCode == povCode && (buttonRight.direction == value || release)) {
            trigger(RhythmGame.InputButtons.RIGHT)
            any = true
        }
        if (buttonUp is ControllerInput.Pov && buttonUp.povCode == povCode && (buttonUp.direction == value || release)) {
            trigger(RhythmGame.InputButtons.UP)
            any = true
        }
        if (buttonDown is ControllerInput.Pov && buttonDown.povCode == povCode && (buttonDown.direction == value || release)) {
            trigger(RhythmGame.InputButtons.DOWN)
            any = true
        }
        return any && game.playState == PlayState.PLAYING
    }

    // Below not implemented
    override fun axisMoved(controller: Controller, axisCode: Int, value: Float): Boolean = false

    override fun accelerometerMoved(controller: Controller, accelerometerCode: Int, value: Vector3): Boolean = false

    override fun xSliderMoved(controller: Controller, sliderCode: Int, value: Boolean): Boolean = false

    override fun ySliderMoved(controller: Controller, sliderCode: Int, value: Boolean): Boolean = false

    override fun connected(controller: Controller) {
        Toolboks.LOGGER.info("[RhythmGameScreen] Controller ${controller.name} connected")
    }

    override fun disconnected(controller: Controller) {
        Toolboks.LOGGER.info("[RhythmGameScreen] Controller ${controller.name} disconnected")
    }

}