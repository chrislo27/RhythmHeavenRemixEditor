package io.github.chrislo27.toolboks

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import io.github.chrislo27.toolboks.ui.Stage

/**
 * The main "screen" type class. The [SELF] type parameter should be the same class.
 */
@Suppress("UNCHECKED_CAST")
public abstract class ToolboksScreen<G : ToolboksGame, SELF : ToolboksScreen<G, SELF>>(public val main: G) : Screen, InputProcessor {

    /**
     * The UI stage. By default it is null.
     */
    open val stage: Stage<SELF>? = null

    override fun render(delta: Float) {
        run {
            val stage = this.stage ?: return@run
            val batch = main.batch

            batch.begin()
            stage.render(this as SELF, batch, main.shapeRenderer)
            if (Toolboks.stageOutlines != Toolboks.StageOutlineMode.NONE) {
                val old = batch.packedColor
                batch.setColor(0f, 1f, 0f, 1f)
                stage.drawOutline(batch, stage.camera, 1f, Toolboks.stageOutlines == Toolboks.StageOutlineMode.ONLY_VISIBLE)
                batch.packedColor = old
            }
            batch.end()
        }
    }

    open fun renderUpdate() {
        stage?.frameUpdate(this as SELF)
    }

    abstract fun tickUpdate()

    open fun getDebugString(): String? {
        return null
    }

    protected open fun resizeStage() {
        stage?.updatePositions()
    }

    override fun resize(width: Int, height: Int) {
        resizeStage()
    }

    override fun show() {
        stage?.updatePositions()
        main.inputMultiplexer.removeProcessor(this)
        main.inputMultiplexer.addProcessor(this)
    }

    override fun hide() {
        main.inputMultiplexer.removeProcessor(this)
    }

    open fun showTransition() {

    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage?.touchUp(screenX, screenY, pointer, button)?: false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return stage?.mouseMoved(screenX, screenY)?: false
    }

    override fun keyTyped(character: Char): Boolean {
        return stage?.keyTyped(character)?: false
    }

    override fun scrolled(amount: Int): Boolean {
        return stage?.scrolled(amount)?: false
    }

    override fun keyUp(keycode: Int): Boolean {
        return stage?.keyUp(keycode) ?: false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return stage?.touchDragged(screenX, screenY, pointer) ?: false
    }

    override fun keyDown(keycode: Int): Boolean {
        return stage?.keyDown(keycode) ?: false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return stage?.touchDown(screenX, screenY, pointer, button) ?: false
    }
}