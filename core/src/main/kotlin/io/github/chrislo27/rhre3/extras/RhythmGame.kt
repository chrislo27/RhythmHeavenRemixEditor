package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.playalong.PlayalongControls
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChanges
import io.github.chrislo27.rhre3.util.scaleFont
import io.github.chrislo27.rhre3.util.unscaleFont
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import java.util.*


abstract class RhythmGame {
    
    enum class InputButtons {
        A, B, UP, DOWN, LEFT, RIGHT;
        
        companion object {
            val VALUES = values().toList()
        }
        
        fun getInputKey(controls: PlayalongControls): Int = when (this) {
            A -> controls.buttonA
            B -> controls.buttonB
            UP -> controls.buttonUp
            DOWN -> controls.buttonDown
            LEFT -> controls.buttonLeft
            RIGHT -> controls.buttonRight
        }
    }

    val tempos: TempoChanges = TempoChanges(120f)
    var seconds: Float = 0f
        set(value) {
            field = value
            beat = tempos.secondsToBeats(value)
        }
    var beat: Float = 0f
        private set
    
    val pressedInputs: EnumSet<InputButtons> = EnumSet.noneOf(InputButtons::class.java)
    var playState: PlayState = PlayState.PLAYING
    var currentTextBox: TextBox? = null
    
    val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    private val tmpMatrix: Matrix4 = Matrix4()

    protected abstract fun _render(main: RHRE3Application, batch: SpriteBatch)
    
    open fun render(main: RHRE3Application, batch: SpriteBatch) {
        camera.update()
        tmpMatrix.set(batch.projectionMatrix)
        batch.projectionMatrix = camera.combined
        batch.begin()
        
        _render(main, batch)
        
        val textBox = currentTextBox
        if (textBox != null) {
            val font = main.defaultFontLarge
            font.scaleFont(camera)
            font.scaleMul(0.5f)
            font.setColor(0f, 0f, 0f, 1f)
            // Render text box
            val backing = AssetRegistry.get<Texture>("ui_textbox")
            val texW = backing.width
            val texH = backing.height
            val sectionX = texW / 3
            val sectionY = texH / 3
            val screenW = camera.viewportWidth
            val screenH = camera.viewportHeight
            val x = screenW * 0.1f
            val y = screenH * 0.75f
            val w = screenW * 0.8f
            val h = screenH / 5f
            // Corners
            batch.draw(backing, x, y, sectionX * 1f, sectionY * 1f, 0f, 1f, 1 / 3f, 2 / 3f)
            batch.draw(backing, x, y + h - sectionY, sectionX * 1f, sectionY * 1f, 0f, 2 / 3f, 1 / 3f, 1f)
            batch.draw(backing, x + w - sectionX, y, sectionX * 1f, sectionY * 1f, 2 / 3f, 1f, 1f, 2 / 3f)
            batch.draw(backing, x + w - sectionX, y + h - sectionY, sectionX * 1f, sectionY * 1f, 2 / 3f, 2 / 3f, 1f, 1f)

            // Sides
            batch.draw(backing, x, y + sectionY, sectionX * 1f, h - sectionY * 2, 0f, 2 / 3f, 1 / 3f, 1 / 3f)
            batch.draw(backing, x + w - sectionX, y + sectionY, sectionX * 1f, h - sectionY * 2, 2 / 3f, 2 / 3f, 1f, 1 / 3f)
            batch.draw(backing, x + sectionX, y, w - sectionX * 2, sectionY * 1f, 1 / 3f, 0f, 2 / 3f, 1 / 3f)
            batch.draw(backing, x + sectionX, y + h - sectionY, w - sectionX * 2, sectionY * 1f, 1 / 3f, 2 / 3f, 2 / 3f, 1f)

            // Centre
            batch.draw(backing, x + sectionX, y + sectionY, w - sectionX * 2, h - sectionY * 2, 1 / 3f, 1 / 3f, 2 / 3f, 2 / 3f)

            // Render text
            val textWidth = font.getTextWidth(textBox.text, w - sectionX * 2, false)
            val textHeight = font.getTextHeight(textBox.text)
            font.drawCompressed(batch, textBox.text, (x + w / 2f - textWidth / 2f).coerceAtLeast(x + sectionX), y + h / 2f + textHeight / 2,
                                w - sectionX * 2, Align.left)

            if (textBox.requiresInput) {
                if (textBox.secsBeforeCanInput <= 0f) {
                    val bordered = MathHelper.getSawtoothWave(1.25f) >= 0.25f && InputButtons.A !in pressedInputs
                    font.draw(batch, if (bordered) "\uE0A0" else "\uE0E0", x + w - sectionX * 0.75f, y + font.capHeight + sectionY * 0.35f, 0f, Align.center, false)
                }
            }
            font.scaleMul(1f / 0.5f)
            font.unscaleFont()
        }

        batch.end()
        batch.projectionMatrix = tmpMatrix
    }
    
    open fun renderUpdate() {
        
    }
    
    open fun update(delta: Float) {
        val textBox = currentTextBox
        if (textBox != null && textBox.requiresInput) {
            if (textBox.secsBeforeCanInput > 0f) {
                textBox.secsBeforeCanInput -= Gdx.graphics.deltaTime // Don't use delta
            }
        }
        
        if (playState != PlayState.PLAYING)
            return
        
        // Timing
        seconds += delta


        // FIXME non-endless not yet supported
//        if (playState != PlayState.STOPPED && beat >= duration) {
//            playState = PlayState.STOPPED
//        }
    }

    /**
     * True if consumed
     */
    open fun onInput(button: InputButtons, release: Boolean): Boolean {
        return false
    }
    
    open fun dispose() {
        
    }

    open fun getDebugString(): String {
        return """Pos.: ♩${Editor.THREE_DECIMAL_PLACES_FORMATTER.format(beat)} / ${Editor.THREE_DECIMAL_PLACES_FORMATTER.format(seconds)}
Tempo: ♩=${tempos.tempoAtSeconds(seconds)}
"""
    }
    
}