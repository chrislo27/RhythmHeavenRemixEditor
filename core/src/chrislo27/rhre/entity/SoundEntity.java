package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.inspections.InspectionFunction;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.registry.Game;
import chrislo27.rhre.registry.GameRegistry;
import chrislo27.rhre.registry.SoundCue;
import chrislo27.rhre.track.Remix;
import chrislo27.rhre.track.Semitones;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import ionium.registry.AssetRegistry;
import ionium.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SoundEntity extends Entity implements HasGame, SoundCueActionProvider {

	public final SoundCue cue;
	private final Game game;
	public volatile int semitone;
	private volatile long soundId;
	private volatile long introSoundId;

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, float duration, int semitone) {
		super(remix);
		this.cue = cue;
		this.semitone = semitone;

		this.bounds.set(beat, level, duration, 1);

		game = GameRegistry.instance().get(cue.getId().substring(0, cue.getId().indexOf('/')));
	}

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, int semitone) {
		this(remix, cue, beat, level, cue.getDuration(), semitone);
	}

	@Override
	public boolean isStretchable() {
		return cue.getCanAlterDuration();
	}

	@Override
	public boolean isRepitchable() {
		return cue.getCanAlterPitch();
	}

	@Override
	public String getID() {
		return cue.getId();
	}

	@Override
	public int getSemitone() {
		return semitone;
	}

	@Override
	public void adjustPitch(int semitoneChange, int min, int max) {
		semitone = MathUtils.clamp(semitone + semitoneChange, min, max);
	}

	@Override
	public String getName() {
		return cue.getName();
	}

	@Override
	public List<InspectionFunction> getInspectionFunctions() {
		return cue.getInspectionFunctions();
	}

	@Override
	public SoundEntity copy() {
		return new SoundEntity(remix, cue, bounds.x, (int) bounds.y, bounds.width, semitone);
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

		main.getFont().getData().setScale(0.5f);
		main.getFont().setColor(0, 0, 0, 1);
		String name = cue.getName();
		float targetWidth = bounds.getWidth() * PX_WIDTH - 8;
		float height = Utils.getHeightWithWrapping(main.getFont(), name, targetWidth);
		main.getFont().draw(batch, name, bounds.getX() * PX_WIDTH,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) + height * 0.5f, targetWidth,
				Align.right, true);

		if (cue.getCanAlterPitch()) {
			main.getFont().draw(batch, Semitones.getSemitoneName(semitone), bounds.getX() * PX_WIDTH + 4,
					bounds.getY() * PX_HEIGHT + main.getFont().getCapHeight() + 4);
		}
		main.getFont().getData().setScale(1);
	}

	@Override
	public void onStart(float delta) {
		super.onStart(delta);

		final float bpm = remix.getTempoChanges().getTempoAt(remix.getBeat());

		if (cue.getIntroSoundObj() != null) {
			introSoundId = cue.getIntroSoundObj().play(1, cue.getPitch(semitone, bpm), 0);
		}
		if (cue.shouldBeLooped()) {
			soundId = cue.getSoundObj().loop(1, cue.getPitch(semitone, bpm), 0);
		} else {
			soundId = cue.getSoundObj().play(1, cue.getPitch(semitone, bpm), 0);
		}
	}

	@Override
	public void onEnd(float delta) {
		super.onEnd(delta);

		if (cue.shouldBeStopped()) {
			if (cue.getIntroSoundObj() != null)
				cue.getIntroSoundObj().stop(introSoundId);
			cue.getSoundObj().stop(soundId);
		}

	}

	@Override
	public Game getGame() {
		return game;
	}

	@NotNull
	@Override
	public List<SoundCueAction> provide() {
		List<SoundCueAction> list = new ArrayList<>();

		float startTime = remix.getTempoChanges().beatsToSeconds(this.bounds.x);
		list.add(new SoundCueAction(cue, startTime,
				remix.getTempoChanges().beatsToSeconds(this.bounds.x + this.bounds.width) - startTime));

		return list;
	}
}
