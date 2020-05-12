package io.github.chrislo27.toolboks.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.util.CloseListener


/**
 * The launcher to use for desktop applications.
 * The system property `file.encoding` is set to `UTF-8`.
 */
class ToolboksDesktopLauncher3(val game: ToolboksGame) {

    val config = Lwjgl3ApplicationConfiguration()

    init {
        System.setProperty("file.encoding", "UTF-8")
    }

    inline fun editConfig(func: Lwjgl3ApplicationConfiguration.() -> Unit): ToolboksDesktopLauncher3 {
        config.func()
        return this
    }

    fun launch(): Lwjgl3Application {
        val app = object : Lwjgl3Application(game, config) {
            override fun exit() {
                if ((game as? CloseListener)?.attemptClose() != false) {
                    super.exit()
                }
            }
        }
        return app
    }

}
