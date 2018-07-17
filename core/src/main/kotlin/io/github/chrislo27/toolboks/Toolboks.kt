package io.github.chrislo27.toolboks

import com.badlogic.gdx.Input
import io.github.chrislo27.toolboks.logging.Logger

/**
 * Holds constants and some info about Toolboks.
 */
object Toolboks {

//    val TOOLBOKS_VERSION: Version = Version(1, 0, 0)

    const val TOOLBOKS_ASSET_PREFIX: String = "toolboks_"

    @Volatile lateinit var LOGGER: Logger

    const val DEBUG_KEY: Int = Input.Keys.F8
    val DEBUG_KEY_NAME: String = Input.Keys.toString(DEBUG_KEY)
    var debugMode: Boolean = false
    var stageOutlines: StageOutlineMode = StageOutlineMode.NONE

    enum class StageOutlineMode {
        NONE, ALL, ONLY_VISIBLE
    }

}