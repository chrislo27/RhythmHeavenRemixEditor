package chrislo27.rhre.entity;

import chrislo27.rhre.Main;
import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.palette.AbstractPalette;
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

import java.util.ArrayList;
import java.util.List;

public class PatternEntity extends Entity {

	public final List<SoundEntity> internal;
	public final Pattern pattern;
	private final List<Vector2> originalBounds;
	private final List<Integer> originalSemitones;
	private final float originalWidth;
	private final boolean repitchable;
	private volatile int semitone;

	public PatternEntity(Remix remix, Pattern p) {
		super(remix);
		this.internal = new ArrayList<>();
		this.pattern = p;

		pattern.getCues().forEach(pc -> {
			SoundCue sc = GameRegistry.instance().getCueRaw(pc.getId());

//			Main.logger.debug("Initializing pattern - loading " + pc.getId() + " " + sc);

			internal.add(new SoundEntity(remix, sc, pc.getBeat(), pc.getTrack(), sc.getDuration(), pc.getSemitone()));
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
		this.bounds.height = highest.bounds.y + highest.bounds.height;
		this.bounds.width = furthest.bounds.x + furthest.bounds.width;
		this.originalWidth = this.bounds.width;

		this.originalBounds = new ArrayList<>();
		internal.forEach(se -> originalBounds.add(new Vector2(se.bounds.getX(), se.bounds.getWidth())));

		this.originalSemitones = new ArrayList<>();
		internal.forEach(se -> originalSemitones.add(se.semitone));

		repitchable = internal.stream().anyMatch(se -> se.cue.getCanAlterPitch());
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
	public boolean isStretchable() {
		return pattern.isStretchable();
	}

	@Override
	public boolean isRepitchable() {
		return repitchable;
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

		main.font.getData().setScale(0.5f);
		main.font.setColor(0, 0, 0, 1);
		String name = pattern.getName();
		float targetWidth = bounds.getWidth() * PX_WIDTH - 8;
		float height = Utils.getHeightWithWrapping(main.font, name, targetWidth);
		main.font.draw(batch, name, bounds.getX() * PX_WIDTH,
				bounds.getY() * PX_HEIGHT + (bounds.getHeight() * PX_HEIGHT * 0.5f) + height * 0.5f, targetWidth,
				Align.right, true);
		if (isRepitchable()) {
			main.font.draw(batch, Semitones.getSemitoneName(semitone), bounds.getX() * PX_WIDTH + 4,
					bounds.getY() * PX_HEIGHT + main.font.getCapHeight() + 4);
		}
		main.font.getData().setScale(1);
	}

	@Override
	public void reset() {
		super.reset();
		internal.forEach(Entity::reset);
	}

	@Override
	public void onStart(float delta) {
		super.onStart(delta);
	}

	@Override
	public void onEnd(float delta) {
		super.onEnd(delta);

		internal.forEach(se -> {
			se.onEnd(delta);
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
					e.onStart(delta);
					e.playbackCompletion = PlaybackCompletion.STARTED;
				}

				if (e.playbackCompletion == PlaybackCompletion.STARTED) {
					e.onWhile(delta);

					if (remix.getBeat() >= this.bounds.x + e.bounds.x + e.bounds.width) {
						e.onEnd(delta);
						e.playbackCompletion = PlaybackCompletion.FINISHED;
					}
				}
			}
		}
	}
}
