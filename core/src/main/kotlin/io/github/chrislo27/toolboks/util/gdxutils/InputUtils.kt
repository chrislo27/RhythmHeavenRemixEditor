package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.SharedLibraryLoader


private val inputMap: MutableMap<Int, Boolean> = mutableMapOf()
private val buttonMap: MutableMap<Int, Boolean> = mutableMapOf()

fun Input.isKeyJustReleased(key: Int): Boolean {
    if (inputMap[key] == null)
        inputMap[key] = false

    val old = inputMap[key]
    val state = Gdx.input.isKeyPressed(key)

    inputMap[key] = state

    return !state && old == true
}

fun Input.isButtonJustReleased(button: Int): Boolean {
    if (buttonMap[button] == null)
        buttonMap[button] = false

    val old = buttonMap[button]
    val state = Gdx.input.isButtonPressed(button)

    inputMap[button] = state

    return state && old == false
}

fun Input.isButtonJustPressed(button: Int): Boolean {
    if (buttonMap[button] == null)
        buttonMap[button] = false

    val old = buttonMap[button]
    val state = Gdx.input.isButtonPressed(button)

    inputMap[button] = state

    return !state && old == true
}

fun Input.isControlDown(): Boolean {
    return isKeyPressed(Input.Keys.CONTROL_LEFT) || isKeyPressed(Input.Keys.CONTROL_RIGHT) || (SharedLibraryLoader.isMac && isKeyPressed(Input.Keys.SYM))
}

fun Input.isAltDown(): Boolean {
    return isKeyPressed(Input.Keys.ALT_LEFT) || isKeyPressed(Input.Keys.ALT_RIGHT)
}

fun Input.isShiftDown(): Boolean {
    return isKeyPressed(Input.Keys.SHIFT_LEFT) || isKeyPressed(Input.Keys.SHIFT_RIGHT)
}
