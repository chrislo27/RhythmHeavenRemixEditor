package ionium.stage.ui.skin;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Defines some rendering stuff for the default UI actors
 * 
 *
 */
public class Palette {

	public Color backgroundColor = new Color();
	public Color mouseoverBackgroundColor = new Color();
	public Color clickedBackgroundColor = new Color();
	public Color disabledBackgroundColor = new Color();

	public Color borderColor = new Color();
	public Color mouseoverBorderColor = new Color();
	public Color clickedBorderColor = new Color();
	public Color disabledBorderColor = new Color();

	public int borderThickness = 1;
	public int mouseoverBorderThickness = 1;
	public int clickedBorderThickness = 1;
	public int disabledBorderThickness = 1;

	public Color textColor = new Color();
	public Color mouseoverTextColor = new Color();
	public Color clickedTextColor = new Color();
	public Color disabledTextColor = new Color();

	public BitmapFont textFont;
	public BitmapFont labelFont;

	public Palette setTextColor(float r, float g, float b, float a) {
		textColor.set(r, g, b, a);
		mouseoverTextColor.set(r, g, b, a);
		clickedTextColor.set(r, g, b, a);
		disabledTextColor.set(r, g, b, a);

		return this;
	}

	public Palette setBackgroundColor(float r, float g, float b, float a) {
		backgroundColor.set(r, g, b, a);
		mouseoverBackgroundColor.set(r, g, b, a);
		clickedBackgroundColor.set(r, g, b, a);
		disabledBackgroundColor.set(r, g, b, a);

		return this;
	}

	public Palette setBorderColor(float r, float g, float b, float a) {
		borderColor.set(r, g, b, a);
		mouseoverBorderColor.set(r, g, b, a);
		clickedBorderColor.set(r, g, b, a);
		disabledBorderColor.set(r, g, b, a);

		return this;
	}

	public Palette setBorderThickness(int thickness) {
		borderThickness = thickness;
		mouseoverBorderThickness = thickness;
		clickedBorderThickness = thickness;
		disabledBorderThickness = thickness;

		return this;
	}

}
