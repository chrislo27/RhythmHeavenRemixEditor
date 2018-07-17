package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3

private val vector: Vector3 = Vector3()

private fun assignVector() {
    vector.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
}

fun OrthographicCamera.getInputX(): Float {
    assignVector()
    return this.unproject(vector).x
}

fun OrthographicCamera.getInputY(): Float {
    assignVector()
    return this.unproject(vector).y
}
