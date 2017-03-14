package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.inspections.InspectionFunction;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.registry.Game;
import chrislo27.rhre.registry.GameRegistry;
import chrislo27.rhre.registry.Pattern;
import chrislo27.rhre.registry.SoundCue;
import chrislo27.rhre.track.PlaybackCompletion;
import chrislo27.rhre.track.Remix;
import chrislo27.rhre.track.Semitones;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import ionium.registry.AssetRegistry;
import ionium.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PatternEntity extends Entity implements HasGame, SoundCueActionProvider {

	public final List<SoundEntity> internal;
	public final Pattern pattern;
	private final List<Vector2> originalBounds;
	private final List<Integer> originalSemitones;
	private final float originalWidth;
	private final boolean repitchable;
	private final Game game;
	private volatile int semitone;

	public PatternEntity(Remix remix, Pattern p) {
		super(remix);
		this.internal = new ArrayList<>();
		this.pattern = p;

		pattern.getCues().forEach(pc -> {
			SoundCue sc = GameRegistry.instance().getCueRaw(pc.getId());

//			Main.logger.debug("Initializing pattern - loading " + pc.getId() + " " + sc);

			internal.add(new SoundEntity(remix, sc, pc.getBeat(), pc.getTrack(),
					sc.getCanAlterDuration() && pc.getDuration() > 0 ? pc.getDuration() : sc.getDuration(),
					pc.getSemitone()));
		});

		if (internal.size() == 0)
			throw new IllegalStateException("Pattern must have content!");

		SoundEntity highest = null;
		SoundEntity furthest = null;
		for (SoundEntity se : internal) {
			if (highest == null || se.bounds.y > highest.bounds.y)
				highest = se;

			if (furthest == null || se.bounds.x + se.bounds.width > furthest.bounds.x + furthest.bounds.width)
				furthest = se;

		}
		try {
			this.bounds.height = highest.bounds.y + highest.bounds.height;
			this.bounds.width = furthest.bounds.x + furthest.bounds.width;
		} catch (NullPointerException e) {
			throw new RuntimeException(
					"Couldn't create pattern entity - highest or furthest was null (h: " + highest + ", f: " +
							furthest + ")");
		}
		this.originalWidth = this.bounds.width;

		this.originalBounds = new ArrayList<>();
		internal.forEach(se -> originalBounds.add(new Vector2(se.bounds.getX(), se.bounds.getWidth())));

		this.originalSemitones = new ArrayList<>();
		internal.forEach(se -> originalSemitones.add(se.semitone));

		repitchable = internal.stream().anyMatch(se -> se.cue.getCanAlterPitch());
		String id = pattern.getCues().get(0).getId();
		game = GameRegistry.instance().get(id.substring(0, id.indexOf('/')));
	}

	@Override
	public boolean attemptLoadSounds() {
		boolean b = false;
		for (SoundEntity se : internal) {
			b |= se.attemptLoadSounds();
		}
		return b;
	}

	@Override
	public void onLengthChange(float old) {
		super.onLengthChange(old);

		for (int i = 0; i < internal.size(); i++) {
			SoundEntity se = internal.get(i);
			Vector2 originalData = originalBounds.get(i);

			final float ratioX = originalData.x / originalWidth;
			se.bounds.x = ratioX * this.bounds.width;

			final float ratioW = originalData.y / originalWidth;
			se.bounds.width = ratioW * this.bounds.width;
		}
	}

	@Override
	public void adjustPitch(int semitoneChange, int min, int max) {
		int[] original = new int[internal.size()];
		int originalSt = semitone;
		for (int i = 0; i < internal.size(); i++) {
			original[i] = internal.get(i).semitone;
		}

		semitone += semitoneChange;

		for (SoundEntity se : internal) {
			se.semitone += semitoneChange;

			if (se.semitone < min || se.semitone > max) {
				for (int i = 0; i < internal.size(); i++) {
					internal.get(i).semitone = original[i];
				}
				semitone = originalSt;

				break;
			}
		}
	}

	@Override
	public String getName() {
		return pattern.getName();
	}

	@Override
	public List<InspectionFunction> getInspectionFunctions() {
		return pattern.getInspectionFunctions();
	}

	@Override
	public PatternEntity copy() {
		PatternEntity pe = new PatternEntity(this.remix, pattern);

		pe.bounds.set(this.bounds);
		pe.semitone = semitone;
		pe.internal.forEach(se -> se.semitone += pe.semitone);
		pe.onLengthChange(pe.bounds.width);

		return pe;
	}

	@Override
	public boolean isStretchable() {
		return pattern.isStretchable();
	}

	@Override
	public boolean isRepitchable() {
		return repitchable;
	}

	@Override
	public String getID() {
		return pattern.getId();
	}

	@Override
	public int getSemitone() {
		return semitone;
	}

	@Override
	public void render(Main main, AbstractPalette palette, SpriteBatch batch, boolean selected) {
		renderRect(batch, !pattern.isStretchable() ? palette.getPattern() : palette.getStretchablePattern(),
				palette.getSelectionTint(), selected, this.bounds);

		batch.setColor(0, 0, 0, 0.1f);
		for (SoundEntity se : internal) {
			Main.fillRect(batch, bounds.getX() * PX_WIDTH + se.bounds.x * PX_WIDTH,
					bounds.getY() * PX_HEIGHT + se.bounds.y * PX_HEIGHT, se.bounds.width * PX_WIDTH,
					se.bounds.height * PX_HEIGHT);
			Main.drawRect(batch, bounds.getX() * PX_WIDTH + se.bounds.x * PX_WIDTH,
					bounds.getY() * PX_HEIGHT + se.bounds.y * PX_HEIGHT, se.bounds.width * PX_WIDTH,
					se.bounds.height * PX_HEIGHT, 4);
			Main.drawRect(batch, bounds.getX() * PX_WIDTH + se.bounds.x * PX_WIDTH,
					bounds.getY() * PX_HEIGHT + se.bounds.y * PX_HEIGHT, se.bounds.width * PX_WIDTH,
					se.bounds.height * PX_HEIGHT, 1);
		}

		batch.setColor(1, 1, 1, 0.25f);
		batch.draw(AssetRegistry.getTexture(
				"gameIcon_" + internal.get(0).cue.getId().substring(0, internal.get(0).cue.getId().indexOf('/'))),
				bounds.getX() * PX_WIDTH + Editor.GAME_ICON_PADDING,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) - Editor.GAME_ICON_SIZE * 0.5f,
				Editor.GAME_ICON_SIZE, Editor.GAME_ICON_SIZE);
		batch.setColor(1, 1, 1, 1);

		main.getFont().getData().setScale(0.5f);
		main.getFont().setColor(palette.getCueText());
		String name = pattern.getName();
		float targetWidth = bounds.getWidth() * PX_WIDTH - 8;
		float height = Utils.getHeightWithWrapping(main.getFont(), name, targetWidth);
		main.getFont().draw(batch, name, bounds.getX() * PX_WIDTH,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) + height * 0.5f, targetWidth,
				Align.right, true);
		if (isRepitchable()) {
			main.getFont().draw(batch, Semitones.getSemitoneName(semitone), bounds.getX() * PX_WIDTH + 4,
					bounds.getY() * PX_HEIGHT + main.getFont().getCapHeight() + 4);
		}
		main.getFont().getData().setScale(1);
	}

	@Override
	public void reset() {
		super.reset();
		internal.forEach(Entity::reset);
	}

	@Override
	public void onStart(float delta, float intendedStart) {
		super.onStart(delta, intendedStart);
	}

	@Override
	public void onEnd(float delta, float intendedStart) {
		super.onEnd(delta, intendedStart);

		internal.forEach(se -> {
			se.onEnd(delta, this.bounds.x + se.bounds.x + se.bounds.width);
			se.playbackCompletion = PlaybackCompletion.FINISHED;
		});
	}

	@Override
	public void onWhile(float delta) {
		super.onWhile(delta);

		for (Entity e : internal) {
			if (e.playbackCompletion == PlaybackCompletion.FINISHED)
				continue;

			if (remix.getBeat() >= this.bounds.x + e.bounds.x) {
				if (e.playbackCompletion == PlaybackCompletion.WAITING) {
					e.onStart(delta, this.bounds.x + e.bounds.x);
					e.playbackCompletion = PlaybackCompletion.STARTED;
				}

				if (e.playbackCompletion == PlaybackCompletion.STARTED) {
					e.onWhile(delta);

					if (remix.getBeat() >= this.bounds.x + e.bounds.x + e.bounds.width) {
						e.onEnd(delta, this.bounds.x + e.bounds.x + e.bounds.width);
						e.playbackCompletion = PlaybackCompletion.FINISHED;
					}
				}
			}
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

		internal.forEach(se -> {
			float startTime = remix.getTempoChanges().beatsToSeconds(this.bounds.x + se.bounds.x);
			list.add(new SoundCueAction(se.cue, startTime,
					remix.getTempoChanges().beatsToSeconds(this.bounds.x + se.bounds.x + se.bounds.width) -
							startTime));
		});

		return list;
	}
}
