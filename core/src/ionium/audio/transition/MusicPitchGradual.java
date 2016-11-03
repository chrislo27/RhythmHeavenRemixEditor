package ionium.audio.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import ionium.runnables.AudioChangePitch;
import ionium.util.MathHelper;

public class MusicPitchGradual extends MusicTransition {

	private float init, end, time;

	private float elapsed = 0;

	private AudioChangePitch runnable;

	public MusicPitchGradual(Music m, float init, float end, float time) {
		super(m);

		if (init <= 0 || end <= 0) throw new IllegalArgumentException(String.format(
				"Initial or end pitch cannot be less than or equal to zero ({0}, {1})", init, end));

		this.init = init;
		this.end = end;
		this.time = time;

		runnable = new AudioChangePitch(music, init);
	}

	@Override
	public void update(float delta) {
		elapsed += delta;

		runnable.pitch = MathHelper.lerp(init, end,
				MathUtils.clamp(elapsed / (time <= 0 ? 1 : 0), 0f, 1f));
		Gdx.app.postRunnable(runnable);
	}

	@Override
	public boolean isFinished() {
		return elapsed >= time;
	}

}
