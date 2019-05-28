package io.github.chrislo27.toolboks.transition

import com.badlogic.gdx.Screen
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.ToolboksScreen
import kotlin.math.absoluteValue


/**
 * A one-use transition screen.
 *
 * During a transition, only the render method is called for the entry and destination screens.
 *
 */
open class TransitionScreen<G : ToolboksGame>(main: G,
                                              val entryScreen: Screen?, val destScreen: Screen?,
                                              entryTransition: Transition?, destTransition: Transition?)
    : ToolboksScreen<G, TransitionScreen<G>>(main) {

    val entryTransition: Transition = entryTransition ?: Transition.EMPTY
    val destTransition: Transition = destTransition ?: Transition.EMPTY

    init {
        if (this.entryTransition.duration < 0f)
            throw IllegalArgumentException("Duration of entry transition is negative: ${this.entryTransition.duration}")
        if (this.destTransition.duration < 0f)
            throw IllegalArgumentException("Duration of dest transition is negative: ${this.destTransition.duration}")
    }

    val duration: Float = (this.entryTransition.duration + this.destTransition.duration).absoluteValue
    var timeElapsed: Float = 0f
        private set
    /**
     * The total percentage from 0.0 to 1.0 of the transition state.
     */
    val percentageTotal: Float
        get() = if (duration == 0f) 1f else (timeElapsed / duration).coerceIn(0f, 1f)
    /**
     * The total percentage from 0.0 to 1.0 of the ENTRY transition.
     */
    val percentageEntry: Float
        get() = if (this.entryTransition.duration == 0f) {
            1f
        } else {
            (timeElapsed / this.entryTransition.duration).coerceIn(0f, 1f)
        }
    /**
     * The total percentage from 0.0 to 1.0 of the DESTINATION transition.
     */
    val percentageDest: Float
        get() = if (this.destTransition.duration == 0f) {
            1f
        } else {
            ((timeElapsed - this.entryTransition.duration) / this.destTransition.duration).coerceIn(0f, 1f)
        }
    val percentageCurrent: Float
        get() = if (doneEntry) percentageDest else percentageEntry
    val done: Boolean
        get() = percentageTotal >= 1.0f
    val doneEntry: Boolean
        get() = percentageEntry >= 1.0f

    private var lastScreen: Screen? = entryScreen

    override fun render(delta: Float) {
        super.render(delta)
        timeElapsed += delta

        // Render transition
        val transition = (if (doneEntry) destTransition else entryTransition)
        val screen = (if (doneEntry) destScreen else entryScreen)
        if (lastScreen != screen) {
            (screen as? ToolboksScreen<*, *>)?.showTransition() ?: (screen?.show())
            lastScreen = screen
        }
        transition.render(this) { screen?.render(delta) }

        if (transition.overrideDone) {
            timeElapsed = if (doneEntry) {
                duration
            } else {
                entryTransition.duration
            }
        }

        if (done) {
            dispose()
            main.screen = destScreen
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        entryTransition.dispose()
        destTransition.dispose()
    }

}