package ionium.util;

public class AssetLogger extends ionium.util.Logger {

	public AssetLogger(String tag, int level) {
		super(tag, level);
		this.tag = tag;
		this.level = level;
	}

	private final String tag;
	private int level;

	private String lastmsg = "";

	public String getLastMsg() {
		return lastmsg;
	}

	public void debug(String message) {
		if (level >= DEBUG) {
			// Gdx.app.debug(tag, message);
			lastmsg = message;
		}
	}

	public void debug(String message, Exception exception) {
		if (level >= DEBUG) {
			// Gdx.app.debug(tag, message, exception);
			lastmsg = message;
		}
	}

	public void info(String message) {
		if (level >= INFO) {
			// Gdx.app.log(tag, message);
			lastmsg = message;
		}
	}

	public void info(String message, Exception exception) {
		if (level >= INFO) {
			// Gdx.app.log(tag, message, exception);
			lastmsg = message;
		}
	}

	public void error(String message) {
		if (level >= ERROR) {
			// Gdx.app.error(tag, message);
			lastmsg = message;
		}
	}

	public void error(String message, Throwable exception) {
		if (level >= ERROR) {
			// Gdx.app.error(tag, message, exception);
			lastmsg = message;
		}
	}

	/**
	 * Sets the log level. {@link #NONE} will mute all log output.
	 * {@link #ERROR} will only let error messages through. {@link #INFO} will
	 * let all non-debug messages through, and {@link #DEBUG} will let all
	 * messages through.
	 * 
	 * @param level
	 *            {@link #NONE}, {@link #ERROR}, {@link #INFO}, {@link #DEBUG}.
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

}
