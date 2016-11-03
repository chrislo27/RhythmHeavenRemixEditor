package ionium.audio.transition;

import com.badlogic.gdx.audio.Music;

public abstract class MusicTransition {

	protected final Music music;

	public MusicTransition(Music m) {
		music = m;
	}

	public abstract void update(float delta);
	
	public abstract boolean isFinished();

}
