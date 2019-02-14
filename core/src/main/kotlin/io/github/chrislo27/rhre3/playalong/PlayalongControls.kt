package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.Input


data class PlayalongControls(var buttonA: Int = Input.Keys.J,
                             var buttonB: Int = Input.Keys.K,
                             var buttonLeft: Int = Input.Keys.A,
                             var buttonRight: Int = Input.Keys.D,
                             var buttonUp: Int = Input.Keys.W,
                             var buttonDown: Int = Input.Keys.S) {

    companion object {
        val QWERTY_D_PAD_LEFT: PlayalongControls = PlayalongControls()

    }

}