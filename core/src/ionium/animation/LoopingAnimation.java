package ionium.animation;


public class LoopingAnimation extends OneTimeAnimation {

	public LoopingAnimation(float delay, int count, String path, boolean usesRegion) {
		super(delay, count, path, usesRegion);
		start();
	}

}
