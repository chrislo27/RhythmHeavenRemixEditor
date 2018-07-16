package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation


class CameraPan(val startX: Float, val endX: Float,
                val duration: Float,
                val interpolationX: Interpolation) {

    private var timeElapsed: Float = 0f

    val progress: Float
        get() = (timeElapsed / duration).coerceIn(0f, 1f)

    val done: Boolean
        get() = progress >= 1f

    fun update(delta: Float, camera: OrthographicCamera) {
        timeElapsed += delta

        if (startX != endX) {
            camera.position.x = interpolationX.apply(startX, endX, progress)
        }
    }

}
