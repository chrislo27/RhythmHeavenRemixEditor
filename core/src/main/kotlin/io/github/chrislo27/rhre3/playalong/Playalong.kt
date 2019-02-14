package io.github.chrislo27.rhre3.playalong

import io.github.chrislo27.rhre3.entity.model.special.PlayalongEntity
import io.github.chrislo27.rhre3.track.Remix


class Playalong(val remix: Remix) {

    /**
     * A guaranteed-sorted list of [InputActions](InputAction).
     */
    val inputActions: List<InputAction> = toInputActionList()

    private val inputMap: Map<PlayalongInput, Int> = remix.main.playalongControls.toInputMap()

    /**
     * Returns a *sorted* list of [InputActions](InputAction). May be empty.
     */
    private fun toInputActionList(): List<InputAction> {
        return remix.entities.filterIsInstance<PlayalongEntity>().map(PlayalongEntity::getInputAction).sorted()
    }

    fun onKeyDown(keycode: Int): Boolean {
        return false
    }

    fun onKeyUp(keycode: Int): Boolean {
        return false
    }

}