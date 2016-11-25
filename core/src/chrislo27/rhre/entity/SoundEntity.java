package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.registry.SoundCue;
import chrislo27.rhre.track.Remix;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import ionium.util.Utils;

public class SoundEntity extends Entity {

	public final SoundCue cue;

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, float duration) {
		super(remix);
		this.cue = cue;

		this.bounds.set(beat, level, duration, 1);
	}

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level) {
		this(remix, cue, beat, level, cue.getDuration());
	}

	@Override
	public void render(Main main, AbstractPalette palette, SpriteBatch batch, boolean selected) {
		renderRect(batch, !cue.getCanAlterDuration() ? palette.getSoundCue() : palette.getStretchableSoundCue(),
				palette.getSelectionTint(), selected);

		main.font.getData().setScale(0.5f);
		main.font.setColor(0, 0, 0, 1);
		String name = cue.getName();
		float height = Utils.getHeight(main.font, name);
		main.font.draw(batch, name, bounds.getX() * PX_WIDTH,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) + height * 0.5f,
				bounds.getWidth() * PX_WIDTH - 8, Align.right, true);
		main.font.getData().setScale(1);
	}
}
