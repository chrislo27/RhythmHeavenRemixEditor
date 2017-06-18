package chrislo27.rhre.desktop

import chrislo27.rhre.Main
import chrislo27.rhre.util.console.ConsoleCommands
import com.badlogic.gdx.Files
import com.badlogic.gdx.graphics.Color
import ionium.desktop.ArgumentInferredLwjglAppConfig
import ionium.desktop.GameLwjglApp
import ionium.registry.GlobalVariables
import ionium.registry.lazysound.LazySound
import ionium.util.Logger
import kotlin.concurrent.thread

object DesktopLauncher {
	private var logger: Logger? = null

	@JvmStatic fun main(args: Array<String>) {
		logger = Logger("", com.badlogic.gdx.utils.Logger.DEBUG)
		val main = Main(logger!!)

		val config = ArgumentInferredLwjglAppConfig(args)
		config.title = "Rhythm Heaven Remix Editor"
		config.width = GlobalVariables.defaultWidth
		config.height = GlobalVariables.defaultHeight
		config.fullscreen = false
		config.foregroundFPS = GlobalVariables.maxFps
		config.backgroundFPS = GlobalVariables.maxFps
		config.resizable = true
		config.vSyncEnabled = true
		config.samples = 0
		config.initialBackgroundColor = Color.BLACK
		config.allowSoftwareMode = false
		config.audioDeviceSimultaneousSources = 256

		config.inferFromArguments()

		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "${config.allowSoftwareMode}")

		config.addIcon("images/icon/icon128.png", Files.FileType.Internal)
		config.addIcon("images/icon/icon64.png", Files.FileType.Internal)
		config.addIcon("images/icon/icon32.png", Files.FileType.Internal)
		config.addIcon("images/icon/icon16.png", Files.FileType.Internal)

		LazySound.forceLoadNow = args.any { s ->
			s.equals("--force-load-lazy-sounds", ignoreCase = true)
		}

		thread(isDaemon = true) {
			while (true) {
				val input: String = readLine() ?: break
				val arguments: List<String> = input.split("\\s+".toRegex()).drop(1)

				if (ConsoleCommands.handle(input, arguments))
					break
			}
		}

		GameLwjglApp(main, config, logger)
	}
}
