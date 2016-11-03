package ionium.util.i18n;

import java.util.Locale;

public class NamedLocale {

	private Locale locale;
	private String name;

	public NamedLocale(String name, Locale locale) {
		this.name = name;
		this.locale = locale;

		if (name == null || locale == null)
			throw new IllegalArgumentException("Name and/or locale cannot be null!");
	}

	public Locale getLocale() {
		return locale;
	}

	public NamedLocale setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	public String getName() {
		return name;
	}

	public NamedLocale setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NamedLocale) {
			NamedLocale other = (NamedLocale) obj;

			if (other.locale.equals(this.locale)) {
				if (other.name.equalsIgnoreCase(this.name)) {
					return true;
				}
			}
		}

		return false;
	}

}
