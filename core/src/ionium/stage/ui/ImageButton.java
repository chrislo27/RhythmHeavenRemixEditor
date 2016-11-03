package ionium.stage.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ionium.stage.Stage;
import ionium.stage.ui.skin.Palette;

public class ImageButton extends Button {

	private TextureRegion textureRegion;
	private Color imageTint = new Color(1, 1, 1, 1);

	public ImageButton(Stage stage, Palette palette, TextureRegion region) {
		super(stage, palette);

		textureRegion = region;
	}

	public TextureRegion getTextureRegion() {
		return textureRegion;
	}

	public Button setTextureRegion(TextureRegion region) {
		textureRegion = region;

		return this;
	}

	public Color getColor() {
		return imageTint;
	}

	@Override
	public void render(SpriteBatch batch, float alpha) {
		super.render(batch, alpha);

		if (textureRegion != null) {
			boolean isMouseOver = stage.isMouseOver(this);
			float texWidth, texHeight;

			if (getWidth() <= getHeight()) {
				texWidth = getWidth() - getBorderThickness(isMouseOver) * 2;
				texHeight = texWidth
						* (textureRegion.getRegionWidth() * 1f / textureRegion.getRegionHeight());
			} else {
				texHeight = getHeight() - getBorderThickness(isMouseOver) * 2;
				texWidth = texHeight
						* (textureRegion.getRegionHeight() * 1f / textureRegion.getRegionWidth());
			}

			batch.setColor(imageTint.r, imageTint.g, imageTint.b, imageTint.a * alpha);

			batch.draw(textureRegion, (getX() + getWidth() * 0.5f) - texWidth * 0.5f,
					(getY() + getHeight() * 0.5f) - texHeight * 0.5f, texWidth, texHeight);

			batch.setColor(1, 1, 1, 1);
		}
	}

}
