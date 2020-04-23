package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.toolboks.transition.Transition
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.math.absoluteValue


open class WipeTo(val color: Color, duration: Float, val slope: Float = 4f, val interpolation: Interpolation = Interpolation.linear)
    : Transition(duration) {
    override fun render(transitionScreen: TransitionScreen<*>, screenRender: () -> Unit) {
        screenRender()

        val batch = transitionScreen.main.batch
        batch.begin()
        batch.setColor(color.r, color.g, color.b, color.a)

        val camera = transitionScreen.main.defaultCamera
        val slopePart = (if (slope == 0f) 0f else (camera.viewportWidth / slope.absoluteValue))
        val xOffset = interpolation.apply(camera.viewportWidth, -slopePart, transitionScreen.percentageCurrent)

        // Slope
        if (slope > 0f) {
            batch.drawQuad(xOffset, 0f, color, xOffset + slopePart, 0f, color, xOffset + slopePart, camera.viewportHeight, color, xOffset + slopePart, camera.viewportHeight, color)
        } else if (slope < 0f) {
            batch.drawQuad(xOffset + slopePart, 0f, color, xOffset + slopePart, 0f, color, xOffset + slopePart, camera.viewportHeight, color, xOffset, camera.viewportHeight, color)
        }

        // Block
        batch.fillRect(xOffset + slopePart, 0f, camera.viewportWidth, camera.viewportHeight)

        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    override fun dispose() {
    }
}

open class WipeFrom(val color: Color, duration: Float, val slope: Float = 4f, val interpolation: Interpolation = Interpolation.linear)
    : Transition(duration) {
    override fun render(transitionScreen: TransitionScreen<*>, screenRender: () -> Unit) {
        screenRender()

        val batch = transitionScreen.main.batch
        batch.begin()
        batch.setColor(color.r, color.g, color.b, color.a)

        val camera = transitionScreen.main.defaultCamera
        val slopePart = (if (slope == 0f) 0f else (camera.viewportWidth / slope.absoluteValue))
        val xOffset = interpolation.apply(0f, -(camera.viewportWidth + slopePart), transitionScreen.percentageCurrent)

        // Slope
        if (slope > 0f) {
            batch.drawQuad(xOffset + camera.viewportWidth, 0f, color, xOffset + camera.viewportWidth, 0f, color, xOffset + slopePart + camera.viewportWidth, camera.viewportHeight, color, xOffset + camera.viewportWidth, camera.viewportHeight, color)
        } else if (slope < 0f) {
            batch.drawQuad(xOffset + camera.viewportWidth, 0f, color, xOffset + slopePart + camera.viewportWidth, 0f, color, xOffset + camera.viewportWidth, camera.viewportHeight, color, xOffset + camera.viewportWidth, camera.viewportHeight, color)
        }

        // Block
        batch.fillRect(xOffset, 0f, camera.viewportWidth, camera.viewportHeight)

        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    override fun dispose() {
    }
}
