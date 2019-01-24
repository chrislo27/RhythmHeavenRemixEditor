package io.github.chrislo27.rhre3.modding

import io.github.chrislo27.rhre3.RHRE3Application


object ModdingUtils {

    val moddingToolsEnabled: Boolean get() = RHRE3Application.instance.advancedOptions
    var currentGame: ModdingGame = ModdingGame.DEFAULT_GAME

}
