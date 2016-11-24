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

	public final Rectangle bounds = new Rectangle();
	public final Remix remix;

	public Entity(Remix remix) {
		this.remix = remix;
	}

	public abstract void render(Main main, AbstractPalette palette, SpriteBatch batch, boolean selected);

	protected final void renderRect(SpriteBatch batch, EntityColors palette, EntityColors selPal, boolean selected) {
		renderRect(batch, palette.getBg(), palette.getOutline(), selPal.getBg(), selPal.getOutline(), selected);
	}

	protected final void renderRect(SpriteBatch batch, Color bg, Color outline, Color selBg, Color selOutline,
									boolean selected) {
		batch.setColor(selected ? selBg : bg);

		Main.fillRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
				bounds.getHeight() * PX_HEIGHT);
		batch.setColor(selected ? selOutline : outline);
		Main.drawRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
				bounds.getHeight() * PX_HEIGHT, 4);

		batch.setColor(1, 1, 1, 1);
	}

	public void onStart() {

	}

	public void onEnd() {

	}

	public void onWhile() {

	}

}
