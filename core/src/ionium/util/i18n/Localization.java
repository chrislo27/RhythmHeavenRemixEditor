package ionium.util.i18n;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import ionium.templates.Main;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;

public class Localization {

	public static final String SETTINGS_KEY = "language";

	private static Localization instance;
	private FileHandle baseFileHandle;
	private HashMap<String, Boolean> caughtMissing = new HashMap<>();
	private Array<CompleteI18NBundle> bundles = new Array<>();
	private CompleteI18NBundle selectedBundle = null;

	private Localization() {
	}

	public static synchronized Localization instance() {
		if (instance == null) {
			instance = new Localization();
			instance.loadResources();
		}
		return instance;
	}

	public static String get(String key, Object... params) {

		if (instance().caughtMissing.get(key) != null) {
			return key;
		}

		try {
			if (params == null || params.length == 0) {
				return instance().getCurrentBundle().getBundle().get(key);
			} else {
				return instance().getCurrentBundle().getBundle().format(key, params);
			}
		} catch (MissingResourceException e) {
			instance().caughtMissing.put(key, true);
			Main.logger.warn("WARNING: the bundle \"" + instance().getBaseFileHandle().nameWithoutExtension() + "_" +
					instance().getCurrentBundle().getLocale().getLocale().toString() + "\" has no key \"" + key +
					"\"");
		}

		return key;
	}

	private void loadResources() {
		setBaseFileHandle(Gdx.files.internal("localization/default"));

		addBundle(new NamedLocale("English", new Locale("")));

		selectedBundle = bundles.get(0);
	}

	public void nextLanguage(int advance) {
		if (advance == 0) {
			return;
		}

		int currentIndex = 0;

		for (int i = 0; i < bundles.size; i++) {
			if (bundles.get(i) == selectedBundle) {
				currentIndex = i;
				break;
			}
		}

		currentIndex += (int) Math.signum(advance);

		if (currentIndex < 0) {
			currentIndex = bundles.size - 1;
		} else if (currentIndex >= bundles.size) {
			currentIndex = 0;
		}

		selectedBundle = bundles.get(currentIndex);
	}

	public void setLanguage(int index) {
		if (index < 0 || index >= bundles.size)
			throw new IllegalArgumentException(
					"Index for setting language cannot be out of bounds! (got " + index + ")");

		selectedBundle = bundles.get(index);
	}

	public void setLanguage(NamedLocale locale) {
		for (int i = 0; i < bundles.size; i++) {
			if (locale.equals(bundles.get(i).locale)) {
				selectedBundle = bundles.get(i);
				return;
			}
		}

		selectedBundle = bundles.get(0);
	}

	public void reloadFromFile() {
		CompleteI18NBundle bundle = null;

		for (int i = 0; i < bundles.size; i++) {
			bundle = bundles.get(i);

			bundle.setBundle(I18NBundle.createBundle(getBaseFileHandle(), bundle.locale.getLocale()));
		}
	}

	public void addCustom(String key, String value) {
		for (int i = 0; i < bundles.size; i++) {
			addCustom(key, value, bundles.get(i));
		}
	}

	public void addCustom(String key, String value, NamedLocale locale) {
		if (locale == null)
			return;

		CompleteI18NBundle bundle = null;
		for (CompleteI18NBundle b : bundles) {
			if (b.getLocale().equals(locale)) {
				bundle = b;
				break;
			}
		}

		if (bundle == null)
			return;

		addCustom(key, value, bundle);
	}

	public void addCustom(String key, String value, CompleteI18NBundle bundle) {
		try {
			Field f = bundle.getBundle().getClass().getDeclaredField("properties");

			f.setAccessible(true);

			ObjectMap<String, String> props = (ObjectMap<String, String>) f.get(bundle.getBundle());

			props.put(key, value);
		} catch (Exception e) {
			Main.logger.warn("Failed to add custom key/value to " + bundle.toString(), e);
		}
	}

	public CompleteI18NBundle getCurrentBundle() {
		return selectedBundle;
	}

	public Array<CompleteI18NBundle> getAllBundles() {
		return bundles;
	}

	public void addBundle(NamedLocale locale) {
		bundles.add(new CompleteI18NBundle(locale, I18NBundle.createBundle(getBaseFileHandle(), locale.getLocale())));

		Main.logger.info("Loaded language " + locale.getName() + " (" + locale.getLocale().toString() + ")");
	}

	public void loadFromSettings(Preferences settings) {
		CompleteI18NBundle bundle = null;
		String savedSetting = settings.getString(SETTINGS_KEY, "");

		for (int i = 0; i < bundles.size; i++) {
			bundle = bundles.get(i);

			if (savedSetting.equalsIgnoreCase(bundle.getLocale().getLocale().toString())) {
				selectedBundle = bundle;

				return;
			}
		}

		selectedBundle = bundles.get(0);
	}

	public void saveToSettings(Preferences settings) {
		settings.putString(SETTINGS_KEY, selectedBundle.locale.getLocale().toString());

		settings.flush();
	}

	public FileHandle getBaseFileHandle() {
		return baseFileHandle;
	}

	public void setBaseFileHandle(FileHandle handle) {
		baseFileHandle = handle;
	}

}
