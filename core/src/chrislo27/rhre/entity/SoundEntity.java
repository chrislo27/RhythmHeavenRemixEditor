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
import com.badlogic.gdx.backends.lwjgl.audio.OpenALSound;
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
	private final boolean isFillbotsFill;
	public volatile int semitone;
	private volatile long soundId = -1;
	private volatile long introSoundId = -1;
	private volatile float startTime = 0f;

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, float duration, int semitone) {
		super(remix);
		this.cue = cue;
		this.semitone = semitone;

		this.bounds.set(beat, level, duration, 1);

		game = GameRegistry.instance().get(cue.getId().substring(0, cue.getId().indexOf('/')));
		isFillbotsFill = cue.getId().equals("fillbots/water");
	}

	public SoundEntity(Remix remix, SoundCue cue, float beat, int level, int semitone) {
		this(remix, cue, beat, level, cue.getDuration(), semitone);
	}

	@Override
	public boolean attemptLoadSounds() {
		return cue.attemptLoadSounds();
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
		main.getFont().setColor(palette.getCueText());
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
	public void onStart(float delta, float intendedStart) {
		super.onStart(delta, intendedStart);

		startTime = intendedStart;

		final float bpm = remix.getTempoChanges().getTempoAt(remix.getBeat());

		if (cue.getIntroSoundObj() != null) {
			OpenALSound s = (OpenALSound) cue.getIntroSoundObj();
			introSoundId = s.play(1, cue.getPitch(semitone, bpm), 0);
		}

		OpenALSound s = (OpenALSound) cue.getSoundObj();
		float realPos = remix.getTempoChanges().beatsToSeconds(remix.getBeat() - intendedStart) % s.duration();
		if (cue.shouldBeLooped()) {
			soundId = s.loop(1, cue.getPitch(semitone, bpm), 0);
		} else {
			soundId = s.play(1, cue.getPitch(semitone, bpm), 0);
		}

	}

	@Override
	public void onWhile(float delta) {
		super.onWhile(delta);

		if (isFillbotsFill && soundId != -1) {
			OpenALSound s = (OpenALSound) cue.getSoundObj();
			float remainder = MathUtils
					.clamp(1f - ((startTime + bounds.width) - remix.getBeat()) / bounds.width, 0f, 1f);
			float from = (bounds.width <= 3
					? MathUtils.lerp(1f, 0.6f, (bounds.width - 1) / 2f)
					: MathUtils.lerp(0.6f, 0.4f, (bounds.width - 3) / 4f));

			s.setPitch(soundId, MathUtils.lerp(from, from + 0.6f, remainder));
			// medium: 0.6f - 1.2f
			// big:    0.5f - 1.1f
			// small:  1.0f - 1.6f
		}
	}

	@Override
	public void onEnd(float delta, float intendedEnd) {
		super.onEnd(delta, intendedEnd);

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
