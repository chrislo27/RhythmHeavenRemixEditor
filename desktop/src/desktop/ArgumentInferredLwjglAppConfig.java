package desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ionium.util.Utils;
import ionium.util.resolution.Resolutable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ArgumentInferredLwjglAppConfig extends LwjglApplicationConfiguration implements Resolutable {

	private final String[] rawArgs;
	private HashMap<String, String> arguments = new HashMap<>();

	public ArgumentInferredLwjglAppConfig(String[] args) {
		super();

		this.rawArgs = args;
	}

	/**
	 * Turns rawArgs into the arguments HashMap and then infers
	 */
	public void inferFromArguments() {
		arguments.clear();

		if (rawArgs == null || rawArgs.length <= 1) return;

		for (int i = 0; i < rawArgs.length - (rawArgs.length % 2 == 1 ? -1 : 0); i += 2) {
			arguments.put(rawArgs[i], rawArgs[i + 1]);
		}

		Set<String> keys = arguments.keySet();
		Iterator<String> it = keys.iterator();

		String key;
		String value;
		while (it.hasNext()) {
			key = it.next();
			value = arguments.get(key);

			checkKeyAndValue(key, value);
		}

	}

	private void inferenceFail(String key, String value){
		System.err.println("Failed to infer argument " + value + " for key " + key);
	}

	private void checkKeyAndValue(String key, String value) {
		if (!key.startsWith("-")) return;

		key = key.substring(1);
		value = value.toLowerCase();

		switch (key.toLowerCase()) {
			case "width":
				try {
					int i = Integer.parseInt(value);

					this.width = i;

					Utils.setArgumentsOverrideSettings(true);
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
			case "height":
				try {
					int i = Integer.parseInt(value);

					this.height = i;

					Utils.setArgumentsOverrideSettings(true);
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
			case "fullscreen":
				try {
					int i = Integer.parseInt(value);

					if (i == 0) {
						this.fullscreen = false;

						Utils.setArgumentsOverrideSettings(true);
					} else if (i == 1) {
						this.fullscreen = true;

						Utils.setArgumentsOverrideSettings(true);
					}
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
			case "vsync":
				try {
					int i = Integer.parseInt(value);

					if (i == 0) {
						this.vSyncEnabled = false;

						Utils.setArgumentsOverrideSettings(true);
					} else if (i == 1) {
						this.vSyncEnabled = true;

						Utils.setArgumentsOverrideSettings(true);
					}
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
			case "fpslock":
				try {
					int i = Integer.parseInt(value);

					this.foregroundFPS = i;
					this.backgroundFPS = i;

					Utils.setArgumentsOverrideSettings(true);
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
			case "msaasamples":
				try {
					int i = Integer.parseInt(value);

					this.samples = i;

					Utils.setArgumentsOverrideSettings(true);
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
			case "resizeable":
				try {
					int i = Integer.parseInt(value);

					if (i == 0) {
						this.resizable = false;

						Utils.setArgumentsOverrideSettings(true);
					} else if (i == 1) {
						this.resizable = true;

						Utils.setArgumentsOverrideSettings(true);
					}
				} catch (NumberFormatException ex) {
					inferenceFail(key.toLowerCase(), value);
				}
				break;
		}
	}

	@Override
	public void setWidth(int w) {
		this.width = w;
	}

	@Override
	public void setHeight(int h) {
		this.height = h;
	}

	@Override
	public void setFullscreen(boolean fs) {
		this.fullscreen = fs;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public boolean isFullscreen() {
		return this.fullscreen;
	}

}