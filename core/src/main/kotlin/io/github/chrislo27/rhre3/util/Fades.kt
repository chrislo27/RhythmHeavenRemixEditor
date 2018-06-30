package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.toolboks.transition.Transition
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


/**
 * Fades TO the specified colour to opaque.
 */
class FadeOut(duration: Float, val color: Color) : Transition(duration) {

    override fun render(transitionScreen: TransitionScreen<*>, screenRender: () -> Unit) {
        screenRender()

        val batch = transitionScreen.main.batch
        batch.begin()
        batch.setColor(color.r, color.g, color.b, color.a * transitionScreen.percentageCurrent)
        batch.fillRect(0f, 0f, transitionScreen.main.defaultCamera.viewportWidth * 1f, transitionScreen.main.defaultCamera.viewportHeight * 1f)
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    override fun dispose() {
    }

}

/**
 * Fades AWAY from the specified colour to transparent
 */
class FadeIn(duration: Float, val color: Color) : Transition(duration) {

    override fun render(transitionScreen: TransitionScreen<*>, screenRender: () -> Unit) {
        screenRender()

        val batch = transitionScreen.main.batch
        batch.begin()
        batch.setColor(color.r, color.g, color.b, color.a * (1f - transitionScreen.percentageCurrent))
        batch.fillRect(0f, 0f, transitionScreen.main.defaultCamera.viewportWidth * 1f, transitionScreen.main.defaultCamera.viewportHeight * 1f)
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
    }

    override fun dispose() {
    }

}