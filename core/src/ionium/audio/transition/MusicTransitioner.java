package ionium.audio.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;

public class MusicTransitioner {

	private static MusicTransitioner instance;

	private MusicTransitioner() {
	}

	public static MusicTransitioner instance() {
		if (instance == null) {
			instance = new MusicTransitioner();
			instance.loadResources();
		}
		return instance;
	}

	private Array<MusicTransition> transitions = new Array<>();

	private void loadResources() {

	}

	public void update() {
		for (int i = transitions.size - 1; i >= 0; i--) {
			MusicTransition mt = transitions.get(i);

			mt.update(Gdx.graphics.getDeltaTime());

			if (mt.isFinished()) transitions.removeIndex(i);
		}
	}

	public static void addTransition(MusicTransition t) {
		instance().transitions.add(t);
	}

	public static void fade(Music music, float initialVol, float endVol, float duration) {
		fade(music, initialVol, endVol, duration, true);
	}

	public static void fade(Music music, float initialVol, float endVol, float duration,
			boolean shouldStop) {
		addTransition(new MusicFade(music, initialVol, endVol, duration, shouldStop));
	}

	public static void pitchGradual(Music music, float initial, float end, float duration) {
		addTransition(new MusicPitchGradual(music, initial, end, duration));
	}

}
