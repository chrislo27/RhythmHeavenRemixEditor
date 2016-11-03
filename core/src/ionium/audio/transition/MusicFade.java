package ionium.audio.transition;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import ionium.util.MathHelper;

public class MusicFade extends MusicTransition {

	private float init, end, time;

	private float elapsed = 0;
	private boolean shouldStopMusic;

	public MusicFade(Music m, float init, float end, float time) {
		this(m, init, end, time, true);
	}

	public MusicFade(Music m, float init, float end, float time, boolean shouldStopMusic) {
		super(m);

		if (init < 0 || end < 0) throw new IllegalArgumentException(String
				.format("Initial or end volume cannot be less than zero ({0}, {1})", init, end));

		this.init = init;
		this.end = end;
		this.time = time;
		this.shouldStopMusic = shouldStopMusic;
	}

	@Override
	public void update(float delta) {
		elapsed += delta;

		music.setVolume(MathHelper.lerp(init, end,
				MathUtils.clamp(elapsed / (time <= 0 ? 1 : time), 0f, 1f)));

		if (music.getVolume() <= 0 && shouldStopMusic) {
			music.stop();
			if (init >= 1) music.setVolume(1);
		}
	}

	@Override
	public boolean isFinished() {
		return elapsed >= time;
	}

}
