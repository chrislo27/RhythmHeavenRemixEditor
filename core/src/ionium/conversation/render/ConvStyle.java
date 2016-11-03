package ionium.conversation.render;

public class ConvStyle {

	public boolean shouldFaceBeShown = true;
	public boolean shouldFaceBeRightAligned = false;
	public boolean shouldPlayMumbling = true;
	public float mumblingPitchOffset = 0.1f;
	public float textPaddingX = 32f / 1920f;
	public float textPaddingY = 18f / 1080f;
	public float percentageOfScreenToOccupy = 0.25f;
	public boolean shouldRenderNametag = false;

	/**
	 * Retain default values
	 */
	public ConvStyle() {

	}

	public ConvStyle(boolean shouldFaceBeShown, boolean shouldFaceBeRightAligned,
			boolean shouldPlayMumbling, float mumblingPitchOffset, float textPaddingX,
			float textPaddingY, float percentageOfScreenToOccupy, boolean shouldRenderNametag) {
		this.shouldFaceBeShown = shouldFaceBeShown;
		this.shouldFaceBeRightAligned = shouldFaceBeRightAligned;
		this.shouldPlayMumbling = shouldPlayMumbling;
		this.mumblingPitchOffset = mumblingPitchOffset;
		this.textPaddingX = textPaddingX;
		this.textPaddingY = textPaddingY;
		this.percentageOfScreenToOccupy = percentageOfScreenToOccupy;
		this.shouldRenderNametag = shouldRenderNametag;
	}

}
