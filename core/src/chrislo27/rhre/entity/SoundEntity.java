package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.registry.SoundCue;
import chrislo27.rhre.track.Remix;
import chrislo27.rhre.track.Semitones;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import ionium.registry.AssetRegistry;
import ionium.util.Utils;

public class SoundEntity extends Entity {

	public final SoundCue cue;
	public volatile int semitone;

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, float duration, int semitone) {
		super(remix);
		this.cue = cue;
		this.semitone = semitone;

		this.bounds.set(beat, level, duration, 1);
	}
	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, int semitone) {
		this(remix, cue, beat, level, cue.getDuration(), semitone);
	}

	@Override
	public boolean isStretchable() {
		return cue.getCanAlterDuration();
	}

	@Override
	public void render(Main main, AbstractPalette palette, SpriteBatch batch, boolean selected) {
		renderRect(batch, !cue.getCanAlterDuration() ? palette.getSoundCue() : palette.getStretchableSoundCue(),
				palette.getSelectionTint(), selected, bounds);

		batch.setColor(1, 1, 1, 0.25f);
		batch.draw(AssetRegistry.getTexture("gameIcon_" + cue.getId().substring(0, cue.getId().indexOf('/'))),
				bounds.getX() * PX_WIDTH + Editor.GAME_ICON_PADDING,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) - Editor.GAME_ICON_SIZE * 0.5f,
				Editor.GAME_ICON_SIZE, Editor.GAME_ICON_SIZE);
		batch.setColor(1, 1, 1, 1);

		main.font.getData().setScale(0.5f);
		main.font.setColor(0, 0, 0, 1);
		String name = cue.getName();
		float height = Utils.getHeight(main.font, name);
		main.font.draw(batch, name, bounds.getX() * PX_WIDTH,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) + height * 0.5f,
				bounds.getWidth() * PX_WIDTH - 8, Align.right, true);
		main.font.getData().setScale(1);
	}

	@Override
	public void onStart(float delta) {
		super.onStart(delta);

		if (cue.getIntroSoundObj() != null) {
			cue.getIntroSoundObj().play(1, cue.getCanAlterPitch() ? Semitones.INSTANCE.getALPitch(semitone) : 1, 0);
		}
		if (cue.getCanAlterDuration() || cue.getLoops()) {
			cue.getSoundObj().loop(1, cue.getCanAlterPitch() ? Semitones.INSTANCE.getALPitch(semitone) : 1, 0);
		} else {
			cue.getSoundObj().play(1, cue.getCanAlterPitch() ? Semitones.INSTANCE.getALPitch(semitone) : 1, 0);
		}
	}

	@Override
	public void onEnd(float delta) {
		super.onEnd(delta);

		if (cue.getCanAlterDuration() || cue.getLoops()) {
			if (cue.getIntroSoundObj() != null)
				cue.getIntroSoundObj().stop();
			cue.getSoundObj().stop();
		}

	}
}
