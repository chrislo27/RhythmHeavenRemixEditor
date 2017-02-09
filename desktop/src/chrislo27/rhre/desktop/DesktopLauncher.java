package chrislo27.rhre.desktop;

import chrislo27.rhre.Main;
import com.badlogic.gdx.Files;
import desktop.ArgumentInferredLwjglAppConfig;
import desktop.GameLwjglApp;
import ionium.registry.GlobalVariables;
import ionium.util.Logger;

public class DesktopLauncher {
	private static Logger logger;

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");

		ArgumentInferredLwjglAppConfig config = new ArgumentInferredLwjglAppConfig(args);
		config.title = "";
		config.width = GlobalVariables.defaultWidth;
		config.height = GlobalVariables.defaultHeight;
		config.fullscreen = false;
		config.foregroundFPS = GlobalVariables.maxFps;
		config.backgroundFPS = GlobalVariables.maxFps;
		config.resizable = false;
		config.vSyncEnabled = true;
		config.samples = 0;

		config.inferFromArguments();

		config.addIcon("images/icon/icon128.png", Files.FileType.Internal);
		config.addIcon("images/icon/icon64.png", Files.FileType.Internal);
		config.addIcon("images/icon/icon32.png", Files.FileType.Internal);
		config.addIcon("images/icon/icon16.png", Files.FileType.Internal);

		logger = new Logger("", com.badlogic.gdx.utils.Logger.DEBUG);
		new GameLwjglApp(new Main(logger), config, logger);
	}
}
