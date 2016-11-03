package ionium.stage.ui;

import ionium.util.i18n.Localization;

/**
 * Text based UI objects use a LocalizationStrategy to parse their text.
 * The default implementation uses Localization.get(String, Object...) and
 * can be overwritten with a different way to get the string.
 *
 */
public class LocalizationStrategy {

	public String get(String key, Object... params) {
		if (key == null) return "null";

		return Localization.get(key, params);
	}

}
