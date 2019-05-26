package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.graphics.OrthographicCamera


fun OrthographicCamera.setRotation(angleDeg: Float) {
    up.set(0f, 1f, 0f)
    direction.set(0f, 0f, -1f)
    rotate(angleDeg)
}

fun OrthographicCamera.setRotationYDown(angleDeg: Float) {
    up.set(0f, -1f, 0f)
    direction.set(0f, 0f, 1f)
    rotate(angleDeg)
}
