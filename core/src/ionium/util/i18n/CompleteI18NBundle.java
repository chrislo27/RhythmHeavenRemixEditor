package ionium.util.i18n;

import com.badlogic.gdx.utils.I18NBundle;

public class CompleteI18NBundle {

	protected NamedLocale locale;
	protected I18NBundle bundle;

	public CompleteI18NBundle(NamedLocale locale, I18NBundle bundle) {
		this.locale = locale;
		this.bundle = bundle;
	}

	public NamedLocale getLocale() {
		return locale;
	}

	public CompleteI18NBundle setLocale(NamedLocale locale) {
		this.locale = locale;
		return this;
	}

	public I18NBundle getBundle() {
		return bundle;
	}

	public CompleteI18NBundle setBundle(I18NBundle bundle) {
		this.bundle = bundle;
		return this;
	}

}
