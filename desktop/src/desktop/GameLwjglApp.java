package desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ionium.util.Logger;

import java.text.SimpleDateFormat;

public class GameLwjglApp extends Lwjgl3Application {

	public GameLwjglApp(ApplicationListener listener, Lwjgl3ApplicationConfiguration config,
			Logger log) {
		super(listener, config);
		logger = log;
	}

	private SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
	private Logger logger;

	public Logger getLogger() {
		return logger;
	}

}
