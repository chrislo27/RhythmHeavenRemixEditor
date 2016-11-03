package ionium.runnables;

import com.badlogic.gdx.audio.Music;

public class AudioChangePitch implements Runnable {

	public final Music mus;
	public float pitch;
	
	public AudioChangePitch(Music mus, float pitch) {
		this.mus = mus;
		this.pitch = pitch;
	}

	@Override
	public void run() {
	}

}
