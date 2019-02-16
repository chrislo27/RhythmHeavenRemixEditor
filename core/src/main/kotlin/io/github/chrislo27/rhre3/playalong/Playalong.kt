package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.editor.stage.playalong.PlayalongStage
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.entity.model.special.PlayalongEntity
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import kotlin.math.absoluteValue
import kotlin.properties.Delegates


class Playalong(val remix: Remix) {

    companion object {
        val MAX_OFFSET_SEC: Float = 0.133333f // Around 8 frames
        val ACE_OFFSET: Float = 0.025f // Around 1.5 frames
        val GOOD_OFFSET: Float = 0.06666667f // Around 4 frames
        val BARELY_OFFSET: Float = 0.116667f // Around 7 frames
    }

    private val stage: PlayalongStage get() = remix.editor.stage.playalongStage

    /**
     * A guaranteed-sorted list of [InputActions](InputAction).
     */
    val inputActions: List<InputAction> = toInputActionList()
    /**
     * The number of input results expected. Instantaneous actions have one, two otherwise.
     */
    val numResultsExpected: Int = inputActions.sumBy { if (it.isInstantaneous) 1 else 2 }
    /**
     * Inputs that are fully done.
     */
    val inputted: MutableMap<InputAction, InputResults> = mutableMapOf()
    /**
     * For two-stage inputs. Value represents the result of the first part
     */
    val inputsInProgress: MutableMap<InputAction, Pair<Int, InputResult>> = mutableMapOf()

    val skillStarEntity: CueEntity? = remix.entities.firstOrNull { it is CueEntity && it.isSkillStar } as CueEntity?

    private val inputMap: Map<PlayalongInput, Set<Int>> = remix.main.playalongControls.toInputMap()
    private val keycodeTriggers: Map<Int, Set<PlayalongInput>> = inputMap.flatMap { (k, v) -> v.map { it to k } }.groupBy { it.first }.mapValues { it.value.map { p -> p.second }.toSet() }

    /**
     * The computed score from 0.0 to 100.0.
     */
    var score: Float = 0f
        private set
    var gotSkillStar: Boolean = false
        private set
    var perfectSoFar: Boolean = true
        private set
    /**
     * If true, counts score acording to [InputTiming.scoreWeight]. Otherwise, by raw offset values.
     */
    var countScoreByTiming: Boolean by Delegates.observable(true) { _, _, _ -> updateScore() }

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

        // Check in progress inputs' trailing inputs
        inputsInProgress.entries.forEach { (input, firstResult) ->
            if ((input.method == PlayalongMethod.LONG_PRESS && seconds > remix.tempos.beatsToSeconds(input.beat + input.duration)) ||
                    (input.method != PlayalongMethod.PRESS && seconds > remix.tempos.beatsToSeconds(input.beat + input.duration) + MAX_OFFSET_SEC)) {
                inputsInProgress.remove(input)
                val result = createInputResult(input, end = true, atSeconds = seconds, timing = InputTiming.MISS)
                inputted[input] = InputResults(input, listOf(firstResult.second, result))
                onInput(input, result, false)
            }
        }
        // Check entirely missed inputs' starting inputs
        inputActions.forEach { input ->
            if (input !in inputsInProgress.keys && input !in inputted.keys && seconds > remix.tempos.beatsToSeconds(input.beat) + MAX_OFFSET_SEC) {
                val result = createInputResult(input, end = false, atSeconds = remix.tempos.beatsToSeconds(input.beat), timing = InputTiming.MISS)
                val list = mutableListOf(result)
                if (!input.isInstantaneous) {
                    list += createInputResult(input, true, remix.tempos.beatsToSeconds(input.beat + input.duration), InputTiming.MISS)
                }
                inputted[input] = InputResults(input, list)
                onInput(input, result, true)
                if (list.size > 1) {
                    onInput(input, list[1], false)
                }
            }
        }
    }

    private fun createInputResult(target: InputAction, end: Boolean, atSeconds: Float = remix.seconds, timing: InputTiming? = null): InputResult {
        var offset = atSeconds - remix.tempos.beatsToSeconds(target.beat + if (end) target.duration else 0f)
        if (target.method == PlayalongMethod.LONG_PRESS && offset > 0f)
            offset = 0f
        return if (timing == null) InputResult(offset) else InputResult(offset, timing)
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
                        onInput(searched, result, true)
                    }
                    PlayalongMethod.LONG_PRESS, PlayalongMethod.PRESS_AND_HOLD -> {
                        val result = createInputResult(searched, false)
                        inputsInProgress[searched] = keycode to result
                        onInput(searched, result, true)
                    }
                    else -> {
                    }
                }
            } else {
                if (searched.method == PlayalongMethod.RELEASE_AND_HOLD) {
                    val result = createInputResult(searched, false)
                    inputsInProgress[searched] = keycode to result
                    onInput(searched, result, true)
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
                onInput(input, result, false)
            }
        }

        return false
    }

    fun onInput(inputAction: InputAction, inputResult: InputResult, start: Boolean) {
        println("Action at beat ${inputAction.beat} ${inputAction.method} ${inputAction.input.id} hit with offset ${inputResult.offset} - ${inputResult.timing}")
        if (!gotSkillStar && skillStarEntity != null && remix.seconds <= remix.tempos.beatsToSeconds(skillStarEntity.bounds.x) + MAX_OFFSET_SEC) {
            if (inputResult.timing == InputTiming.ACE
                    && ((start && MathUtils.isEqual(inputAction.beat, skillStarEntity.bounds.x))
                            || (!start && !inputAction.isInstantaneous && MathUtils.isEqual(inputAction.beat + inputAction.duration, skillStarEntity.bounds.x)))) {
                skillStarEntity.play()
                gotSkillStar = true
                stage.onSkillStarGet()
            }
        }

        updateScore()
        if (perfectSoFar) {
            if (inputResult.timing == InputTiming.MISS) {
                perfectSoFar = false
                stage.onPerfectFail()
            } else {
                stage.onPerfectHit()
            }
        }
    }

    fun updateScore(): Float {
        if (numResultsExpected <= 0) {
            score = 0f
            return score
        }

        score = inputted.values.flatMap { it.results }
                .sumByDouble {
                    (if (countScoreByTiming)
                        it.timing.scoreWeight
                    else (1f - (it.offset.absoluteValue / MAX_OFFSET_SEC).coerceIn(0f, 1f))) * 100.0
                }.toFloat() / numResultsExpected
        score = score.coerceIn(0f, 100f)

        stage.updateScoreLabel()

        return score
    }

    fun onKeyDown(keycode: Int): Boolean {
        return handleInput(true, keycode)
    }

    fun onKeyUp(keycode: Int): Boolean {
        return handleInput(false, keycode)
    }

}