package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.entity.model.special.PlayalongEntity
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix


class Playalong(val remix: Remix) {

    companion object {
        val MAX_OFFSET_SEC: Float = 0.15f // Around 9 frames
        val ACE_OFFSET: Float = 0.0417f // Around 2.5 frames
        val GOOD_OFFSET: Float = 0.085f // Around 5 frames
        val BARELY_OFFSET: Float = 0.125f // Around 7.5 frames
    }

    /**
     * A guaranteed-sorted list of [InputActions](InputAction).
     */
    val inputActions: List<InputAction> = toInputActionList()
    /**
     * Inputs that are fully done.
     */
    val inputted: MutableMap<InputAction, InputResults> = mutableMapOf()
    /**
     * For two-stage inputs. Value represents the result of the first part
     */
    val inputsInProgress: MutableMap<InputAction, Pair<Int, InputResult>> = mutableMapOf()

    private val inputMap: Map<PlayalongInput, Set<Int>> = remix.main.playalongControls.toInputMap()
    private val keycodeTriggers: Map<Int, Set<PlayalongInput>> = inputMap.flatMap { (k, v) -> v.map { it to k } }.groupBy { it.first }.mapValues { it.value.map { p -> p.second }.toSet() }

    /**
     * Returns a *sorted* list of [InputActions](InputAction). May be empty.
     */
    private fun toInputActionList(): List<InputAction> {
        return remix.entities.filterIsInstance<PlayalongEntity>().map(PlayalongEntity::getInputAction).sorted()
    }

    fun searchForInputAction(aroundSec: Float, threshold: Float, predicate: ((InputAction) -> Boolean)?): InputAction? {
        if (inputActions.isEmpty()) return null

        return inputActions.filter { predicate == null || predicate(it) }
                .firstOrNull { MathUtils.isEqual(aroundSec, remix.tempos.beatsToSeconds(it.beat), threshold) }
    }

    fun frameUpdate() {
        // Figure out if long press actions are over
        // or if two-stage inputs have expired at their ends
        val seconds = remix.seconds

        // Check in progress inputs
        inputsInProgress.entries.forEach { (input, firstResult) ->
            if ((input.method == PlayalongMethod.LONG_PRESS && seconds > remix.tempos.beatsToSeconds(input.beat + input.duration)) ||
                    (input.method != PlayalongMethod.PRESS && seconds > remix.tempos.beatsToSeconds(input.beat + input.duration) + MAX_OFFSET_SEC)) {
                inputsInProgress.remove(input)
                inputted[input] = InputResults(input, listOf(firstResult.second, createInputResult(input, end = true)))
            }
        }
    }

    private fun createInputResult(target: InputAction, end: Boolean, atSeconds: Float = remix.seconds): InputResult {
        var offset = atSeconds - remix.tempos.beatsToSeconds(target.beat + if (end) target.duration else 0f)
        if (target.method == PlayalongMethod.LONG_PRESS && offset > 0f)
            offset = 0f
        return InputResult(offset)
    }

    private fun handleInput(down: Boolean, keycode: Int): Boolean {
        if (remix.playState != PlayState.PLAYING) return false
        val playalongInput = keycodeTriggers[keycode] ?: return false
        val seconds = remix.seconds
        val searched = searchForInputAction(seconds, MAX_OFFSET_SEC) {
            it !in inputted.keys && it.input in playalongInput
                    && (if (it.method == PlayalongMethod.RELEASE_AND_HOLD) !down else down)
                    && it !in inputsInProgress.keys
        }

        if (searched != null) {
            if (down) {
                when (searched.method) {
                    PlayalongMethod.PRESS -> {
                        val result = createInputResult(searched, false)
                        inputted[searched] = InputResults(searched, listOf(result))
                        onInput(searched, result)
                    }
                    PlayalongMethod.LONG_PRESS, PlayalongMethod.PRESS_AND_HOLD -> {
                        val result = createInputResult(searched, false)
                        inputsInProgress[searched] = keycode to result
                        onInput(searched, result)
                    }
                    else -> {
                    }
                }
            } else {
                if (searched.method == PlayalongMethod.RELEASE_AND_HOLD) {
                    val result = createInputResult(searched, false)
                    inputsInProgress[searched] = keycode to result
                    onInput(searched, result)
                }
            }
        }

        // Check in progress inputs
        inputsInProgress.entries.forEach { (input, firstResult) ->
            if ((!down && keycode == firstResult.first && (input.method == PlayalongMethod.LONG_PRESS || input.method == PlayalongMethod.PRESS_AND_HOLD)) ||
                    (down && keycode == firstResult.first && input.method == PlayalongMethod.RELEASE_AND_HOLD)) {
                inputsInProgress.remove(input)
                val result = createInputResult(input, end = true)
                inputted[input] = InputResults(input, listOf(firstResult.second, result))
                onInput(input, result)
            }
        }

        return false
    }

    fun onInput(inputAction: InputAction, inputResult: InputResult) {
        println("Action at beat ${inputAction.beat} ${inputAction.method} ${inputAction.input.id} hit with offset ${inputResult.offset} - ${inputResult.timing}")
    }

    fun onKeyDown(keycode: Int): Boolean {
        return handleInput(true, keycode)
    }

    fun onKeyUp(keycode: Int): Boolean {
        return handleInput(false, keycode)
    }

}