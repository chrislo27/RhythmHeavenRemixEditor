package io.github.chrislo27.rhre3.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.desktop.ToolboksDesktopLauncher
import io.github.chrislo27.toolboks.logging.Logger
import kotlin.concurrent.thread

object DesktopLauncher {

	@JvmStatic fun main(args: Array<String>) {
        // TODO console commands
        thread(isDaemon = true) {
            while (true) {
                val input: String = readLine() ?: break
                val arguments: List<String> = input.split("\\s+".toRegex())

                try {
//                    if (ConsoleCommands.handle(main, arguments.first(), arguments.drop(1)))
//                        break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val logger = Logger()
        val logToFile = true
        val app = RHRE3Application(logger, logToFile)
		ToolboksDesktopLauncher(app)
				.editConfig {
                    this.width = app.emulatedSize.first
                    this.height = app.emulatedSize.second
                    this.title = app.getTitle()
                    this.fullscreen = false
                    this.foregroundFPS = 60
                    this.backgroundFPS = 60
                    this.resizable = true
                    this.vSyncEnabled = true
                    this.initialBackgroundColor = Color(0f, 0f, 0f, 1f)
                    this.allowSoftwareMode = false
                    this.audioDeviceSimultaneousSources = 256

                    this.addIcon("images/icon/icon128.png", Files.FileType.Internal)
                    this.addIcon("images/icon/icon64.png", Files.FileType.Internal)
                    this.addIcon("images/icon/icon32.png", Files.FileType.Internal)
                    this.addIcon("images/icon/icon16.png", Files.FileType.Internal)
                }
                .launch()
	}

}
