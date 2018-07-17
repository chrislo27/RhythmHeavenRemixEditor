package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.ToolboksScreen

/**
 * Absorbs all input.
 */
class InputSponge<S : ToolboksScreen<*, *>>(parent: UIElement<S>, parameterStage: Stage<S>)
    : UIElement<S>(parent, parameterStage) {

    var shouldAbsorbInput: Boolean = true

    override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        // invisible
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return shouldAbsorbInput
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return shouldAbsorbInput
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return shouldAbsorbInput
    }

    override fun keyTyped(character: Char): Boolean {
        return shouldAbsorbInput
    }

    override fun scrolled(amount: Int): Boolean {
        return shouldAbsorbInput
    }

    override fun keyUp(keycode: Int): Boolean {
        return shouldAbsorbInput
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return shouldAbsorbInput
    }

    override fun keyDown(keycode: Int): Boolean {
        return shouldAbsorbInput
    }
}