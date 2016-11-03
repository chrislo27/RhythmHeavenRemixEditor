package ionium.stage.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import ionium.stage.Stage;
import ionium.stage.ui.skin.Palette;

public class TextLabel extends Label {

	private LocalizationStrategy i10nStrategy = new LocalizationStrategy();
	private Color textColor = new Color(1, 1, 1, 1);
	private String localizationKey = null;
	private int textAlign = Align.center;
	private boolean wrapText = false;

	public TextLabel(Stage s, Palette palette, String textKey) {
		super(s, palette);

		localizationKey = textKey;
	}

	@Override
	public void render(SpriteBatch batch, float alpha) {
		Palette palette = getPalette();

		float textY = 0;

		if ((getTextAlign() & Align.top) == Align.top) {
			textY = getHeight() - palette.labelFont.getLineHeight();
		} else if ((getTextAlign() & Align.bottom) == Align.bottom) {
			textY = 0;
		} else if ((getTextAlign() & Align.center) == Align.center) {
			textY = getHeight() * 0.5f - palette.labelFont.getCapHeight() * 0.5f;
		}

		palette.labelFont.setColor(textColor.r, textColor.g, textColor.b, textColor.a * alpha);
		palette.labelFont.draw(batch, getText(), getX(),
				getY() + textY + palette.labelFont.getLineHeight() * 0.5f, getWidth(), textAlign,
				wrapText);
		palette.labelFont.setColor(1, 1, 1, 1);
	}

	public boolean isTextWrapped() {
		return wrapText;
	}

	public TextLabel setTextWrap(boolean wrap) {
		wrapText = wrap;

		return this;
	}

	public int getTextAlign() {
		return textAlign;
	}

	public TextLabel setTextAlign(int align) {
		textAlign = align;

		return this;
	}

	public Color getColor() {
		return textColor;
	}

	public String getLocalizationKey() {
		return localizationKey;
	}

	public TextLabel setLocalizationKey(String key) {
		localizationKey = key;

		return this;
	}

	public String getText() {
		return i10nStrategy.get(getLocalizationKey());
	}

	public TextLabel setI10NStrategy(LocalizationStrategy strat) {
		i10nStrategy = strat;

		return this;
	}

	public LocalizationStrategy getI10NStrategy() {
		return i10nStrategy;
	}

}
