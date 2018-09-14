package io.github.chrislo27.toolboks.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglInput
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.util.CloseListener


/**
 * The launcher to use for desktop applications.
 * The system property `file.encoding` is set to `UTF-8`. The [LwjglInput.keyRepeatTime] is set to `0.05`.
 */
class ToolboksDesktopLauncher(val game: ToolboksGame) {

    val config = LwjglApplicationConfiguration()

    init {
        System.setProperty("file.encoding", "UTF-8")
        LwjglInput.keyRepeatTime = 0.05f
    }

    inline fun editConfig(func: LwjglApplicationConfiguration.() -> Unit): ToolboksDesktopLauncher {
        config.func()
        return this
    }

    fun launch(): LwjglApplication {
        val app = object : LwjglApplication(game, config) {
            override fun exit() {
                if ((game as? CloseListener)?.attemptClose() != false) {
                    super.exit()
                }
            }
        }
        return app
    }

}