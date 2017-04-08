package chrislo27.rhre.desktop

import chrislo27.rhre.Main
import com.badlogic.gdx.Files
import com.badlogic.gdx.graphics.Color
import ionium.desktop.ArgumentInferredLwjglAppConfig
import ionium.desktop.GameLwjglApp
import ionium.registry.GlobalVariables
import ionium.registry.lazysound.LazySound
import ionium.util.Logger
import java.util.*

object DesktopLauncher {
	private var logger: Logger? = null

	@JvmStatic fun main(args: Array<String>) {
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true")

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
		config.allowSoftwareMode = true
		config.audioDeviceSimultaneousSources = 256

		config.inferFromArguments()

		config.addIcon("images/icon/icon128.png", Files.FileType.Internal)
		config.addIcon("images/icon/icon64.png", Files.FileType.Internal)
		config.addIcon("images/icon/icon32.png", Files.FileType.Internal)
		config.addIcon("images/icon/icon16.png", Files.FileType.Internal)

		LazySound.forceLoadNow = Arrays.stream(args).anyMatch { s ->
			s.equals("--force-load-lazy-sounds", ignoreCase = true)
		}
		GameLwjglApp(main, config, logger)
	}
}
