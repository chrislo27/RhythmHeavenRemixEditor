package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.inspections.InspectionFunction;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.palette.EntityColors;
import chrislo27.rhre.track.PlaybackCompletion;
import chrislo27.rhre.track.Remix;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public abstract class Entity {

	public static final int PX_HEIGHT = 48;
	public static final int PX_WIDTH = PX_HEIGHT * 4;
	private static final Color tmp = new Color();
	private static final Color tmp2 = new Color();
	public final Rectangle bounds = new Rectangle();
	public final Remix remix;

	public PlaybackCompletion playbackCompletion = PlaybackCompletion.WAITING;

	public Entity(Remix remix) {
		this.remix = remix;
	}

	public void reset() {
		playbackCompletion = PlaybackCompletion.WAITING;
	}

	public void onLengthChange(float old) {

	}

	public void adjustPitch(int semitoneChange, int min, int max){

	}

	public abstract String getName();

	public abstract List<InspectionFunction> getInspectionFunctions();

	public abstract Entity copy();

	public abstract boolean isStretchable();

	public abstract boolean isRepitchable();

	public abstract String getID();

	public abstract int getSemitone();

	public abstract void render(Main main, AbstractPalette palette, SpriteBatch batch, boolean selected);

	public final void setBatchColorFromState(SpriteBatch batch, Color c, Color selectionTint,
											 boolean selected) {
		batch.setColor(selected
				? tmp
				.set(c.r * (1 + selectionTint.r), c.g * (1 + selectionTint.g), c.b * (1 + selectionTint.b), c.a)
				: c);
	}

	protected final void renderRect(SpriteBatch batch, EntityColors palette, Color selectionTint, boolean selected,
									Rectangle bounds) {
		renderRect(batch, palette.getBg(), palette.getOutline(), selectionTint, selected, bounds);
	}

	protected final void renderRect(SpriteBatch batch, Color bg, Color outline, Color selectionTint, boolean selected,
									Rectangle bounds) {
		setBatchColorFromState(batch, bg, selectionTint, selected);

		Main.fillRect(batch, bounds.getX() * PX_WIDTH, bounds.getY() * PX_HEIGHT, bounds.getWidth() * PX_WIDTH,
				bounds.getHeight() * PX_HEIGHT);
		setBatchColorFromState(batch, outline, selectionTint, selected);
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
