package desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic;
import ionium.runnables.AudioChangePitch;
import ionium.util.Logger;
import org.lwjgl.openal.AL10;

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

	@Override
	public boolean executeRunnables() {
		synchronized (runnables) {
			for (int i = runnables.size - 1; i >= 0; i--) {
				executedRunnables.add(runnables.get(i));
			}
			runnables.clear();
		}

		if (executedRunnables.size == 0) return false;

		do {
			Runnable r = executedRunnables.pop();

			checkSpecialRunnables(r);
		} while (executedRunnables.size > 0);

		return true;
	}

	protected boolean checkSpecialRunnables(Runnable r) {
		r.run();

		if (r instanceof AudioChangePitch) {
			AudioChangePitch apc = (AudioChangePitch) r;

			if (!apc.mus.isPlaying()) return true;

			AL10.alSourcef(((OpenALMusic) apc.mus).getSourceId(), AL10.AL_PITCH, apc.pitch);

			return true;
		}

		return false;
	}

	@Override
	public void debug(String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			logger.debug((tag != null ? tag : "") + message);
		}

	}

	@Override
	public void debug(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			logger.debug((tag != null ? tag : "") + message);
			exception.printStackTrace(System.out);
		}

	}

	@Override
	public void log(String tag, String message) {
		if (logLevel >= LOG_INFO) {
			logger.info((tag != null ? tag : "") + message);
		}

	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) {
			logger.info((tag != null ? tag : "") + message);
			exception.printStackTrace(System.out);
		}

	}

	@Override
	public void error(String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			logger.debug((tag != null ? tag : "") + message);
		}

	}

	@Override
	public void error(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			logger.error((tag != null ? tag : "") + message, exception);
		}

	}

}
