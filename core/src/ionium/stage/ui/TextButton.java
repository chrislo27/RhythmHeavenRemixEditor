package ionium.stage.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import ionium.stage.Stage;
import ionium.stage.ui.skin.Palette;

public class TextButton extends Button {

	private LocalizationStrategy i10nStrategy = new LocalizationStrategy();

	private String localizationKey = null;

	public TextButton(Stage s, Palette p, String textKey) {
		super(s, p);

		localizationKey = textKey;
	}

	@Override
	public void render(SpriteBatch batch, float alpha) {
		super.render(batch, alpha);

		Palette palette = getPalette();
		boolean isMouseOver = stage.isMouseOver(this);

		Color textColor = palette.textColor;
		if (!isEnabled()) {
			textColor = palette.disabledTextColor;
		} else if (isPressed()) {
			textColor = palette.clickedTextColor;
		} else if (isMouseOver) {
			textColor = palette.mouseoverTextColor;
		}

		palette.textFont.setColor(palette.textColor.r, palette.textColor.b, palette.textColor.b,
				palette.textColor.a * alpha);
		palette.textFont.draw(batch, getText(), getX() + getWidth() * 0.5f,
				getY() + getHeight() * 0.5f + palette.textFont.getCapHeight() * 0.5f, 0,
				Align.center, false);
		palette.textFont.setColor(1, 1, 1, 1);
	}

	public String getLocalizationKey() {
		return localizationKey;
	}

	public TextButton setLocalizationKey(String key) {
		localizationKey = key;

		return this;
	}

	public String getText() {
		return i10nStrategy.get(getLocalizationKey());
	}

	public TextButton setI10NStrategy(LocalizationStrategy strat) {
		i10nStrategy = strat;

		return this;
	}

	public LocalizationStrategy getI10NStrategy() {
		return i10nStrategy;
	}

}
