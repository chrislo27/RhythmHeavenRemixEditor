package ionium.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ionium.util.Logger;

import java.text.SimpleDateFormat;

public class GameLwjglApp extends LwjglApplication {

	public GameLwjglApp(ApplicationListener listener, LwjglApplicationConfiguration config,
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
