package chrislo27.rhre.desktop;

import chrislo27.rhre.Main;
import chrislo27.rhre.WindowListener;
import chrislo27.rhre.lazysound.LazySound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import desktop.GameLwjglApp;
import ionium.registry.GlobalVariables;
import ionium.util.Logger;

import java.util.Arrays;

public class DesktopLauncher {
	private static Logger logger;

	public static void main(String[] args) {
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");

		logger = new Logger("", com.badlogic.gdx.utils.Logger.DEBUG);
		final Main main = new Main(logger);

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Rhythm Heaven Remix Editor");
		config.setWindowedMode(GlobalVariables.defaultWidth, GlobalVariables.defaultHeight);
		config.setIdleFPS(GlobalVariables.maxFps);
		config.setResizable(true);
		config.useVsync(true);
		config.setInitialBackgroundColor(Color.BLACK);

		config.setWindowListener(new WindowListener(main));

		config.setWindowIcon("images/icon/icon128.png", "images/icon/icon64.png", "images/icon/icon32.png", "images/icon/icon16.png");

		LazySound.Companion.setForceLoadNow(Arrays.stream(args).anyMatch(s -> s.equalsIgnoreCase("--force-load-lazy-sounds")));
		new GameLwjglApp(main, config, logger);
	}
}
