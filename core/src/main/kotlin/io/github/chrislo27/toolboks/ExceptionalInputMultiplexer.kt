package io.github.chrislo27.toolboks

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor


open class ExceptionalInputMultiplexer(val exceptionHandler: (Throwable) -> Unit, vararg processors: InputProcessor?)
    : InputMultiplexer(*processors) {

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        try {
            return super.touchUp(screenX, screenY, pointer, button)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        try {
            return super.mouseMoved(screenX, screenY)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        try {
            return super.keyTyped(character)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        try {
            return super.touchDown(screenX, screenY, pointer, button)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        try {
            return super.scrolled(amount)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        try {
            return super.keyUp(keycode)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        try {
            return super.touchDragged(screenX, screenY, pointer)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        try {
            return super.keyDown(keycode)
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
        return false
    }
}