package ionium.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class OneTimeAnimation extends Animation {

	private long startTime = -1;
	private boolean looping = true;

	public OneTimeAnimation(float delay, int count, String path, boolean usesRegion) {
		super(delay, count, path, usesRegion);
	}

	@Override
	public TextureRegion getCurrentFrame() {
		if (!isPlaying()) {
			return frames[0];
		} else {
			if (System.currentTimeMillis() - startTime > (this.framedelay * 1000f)
					* this.framecount
					&& !looping) {
				stop();
				return frames[0];
			}
		}

		long i = (long) ((System.currentTimeMillis() - startTime) / (framedelay * 1000d));
		return frames[(frames.length - 1) - ((int) ((frames.length - 1) - (i % frames.length)))];
	}

	public OneTimeAnimation start() {
		stop();
		startTime = System.currentTimeMillis();

		return this;
	}

	public OneTimeAnimation stop() {
		startTime = -1;

		return this;
	}

	public boolean isPlaying() {
		return startTime != -1;
	}

	public OneTimeAnimation setLooping(boolean l) {
		looping = l;

		return this;
	}

	public boolean isLooping() {
		return looping;
	}
}
