package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Input


private val inputMap: MutableMap<Int, Boolean> = mutableMapOf()
private val buttonPressedMap: MutableMap<Int, Boolean> = mutableMapOf()
private val buttonReleasedMap: MutableMap<Int, Boolean> = mutableMapOf()

fun Input.isKeyJustReleased(key: Int): Boolean {
    if (inputMap[key] == null)
        inputMap[key] = false

    val old = inputMap[key]
    val current = isKeyPressed(key)

    inputMap[key] = current

    return !current && old == true
}

fun Input.isButtonJustPressed(button: Int): Boolean {
    if (buttonPressedMap[button] == null)
        buttonPressedMap[button] = false

    val old = buttonPressedMap[button]
    val current = isButtonPressed(button)

    buttonPressedMap[button] = current

    return current && old == false
}

fun Input.isButtonJustReleased(button: Int): Boolean {
    if (buttonReleasedMap[button] == null)
        buttonReleasedMap[button] = false

    val old = buttonReleasedMap[button]
    val current = isButtonPressed(button)

    buttonReleasedMap[button] = current

    return !current && old == true
}

fun Input.isControlDown(): Boolean {
    return isKeyPressed(Input.Keys.CONTROL_LEFT) || isKeyPressed(Input.Keys.CONTROL_RIGHT)
}

fun Input.isAltDown(): Boolean {
    return isKeyPressed(Input.Keys.ALT_LEFT) || isKeyPressed(Input.Keys.ALT_RIGHT)
}

fun Input.isShiftDown(): Boolean {
    return isKeyPressed(Input.Keys.SHIFT_LEFT) || isKeyPressed(Input.Keys.SHIFT_RIGHT)
}
