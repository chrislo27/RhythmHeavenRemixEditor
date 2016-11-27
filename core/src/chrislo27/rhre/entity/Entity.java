package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.palette.EntityColors;
import chrislo27.rhre.track.Remix;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

	public static final int PX_HEIGHT = 48;
	public static final int PX_WIDTH = PX_HEIGHT * 4;
	private static final Color tmp = new Color();
	private static final Color tmp2 = new Color();
	public final Rectangle bounds = new Rectangle();
	public final Remix remix;

	public Entity(Remix remix) {
		this.remix = remix;
	}

	public abstract void render(Main main, AbstractPalette palette, SpriteBatch batch, boolean selected);

	protected final void renderRect(SpriteBatch batch, EntityColors palette, Color selectionTint, boolean selected) {
		renderRect(batch, palette.getBg(), palette.getOutline(), selectionTint, selected);
	}

	protected final void renderRect(SpriteBatch batch, Color bg, Color outline, Color selectionTint, boolean
			selected) {
		batch.setColor(selected
				? tmp
				.set(bg.r * (1 + selectionTint.r), bg.g * (1 + selectionTint.g), bg.b * (1 + selectionTint.b), bg.a)
				: bg);

		Main.fillRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
				bounds.getHeight() * PX_HEIGHT);
		batch.setColor(selected ? tmp.set(outline.r * (1 + selectionTint.r), outline.g * (1 + selectionTint.g),
				outline.b * (1 + selectionTint.b), outline.a) : outline);
		Main.drawRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
				bounds.getHeight() * PX_HEIGHT, 4);

		batch.setColor(1, 1, 1, 1);
	}

	public void onStart(float delta) {

	}

	public void onEnd(float delta) {

	}

	public void onWhile(float delta) {

	}

}
