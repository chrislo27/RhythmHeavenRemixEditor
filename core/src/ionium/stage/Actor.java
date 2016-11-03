package ionium.stage;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntArray;
import ionium.templates.Main;

public abstract class Actor {

	private Rectangle screenOffset = new Rectangle();
	private Rectangle pixelOffset = new Rectangle();
	private Rectangle actualPosition = new Rectangle();

	protected final Stage stage;

	private int align = Align.center;
	private Rectangle viewport = new Rectangle();

	private boolean isPressed = false;
	private boolean enabled = true;
	private boolean visible = true;
	private float alpha = 1;
	private IntArray keybinds = new IntArray();

	public Actor(Stage s) {
		stage = s;
		viewport.set(s.getCamera().position.x, s.getCamera().position.y,
				s.getCamera().viewportWidth, s.getCamera().viewportHeight);
	}

	public abstract void render(SpriteBatch batch, float alpha);

	public void renderDebug(SpriteBatch batch) {
		batch.setColor(0, 0, 1, visible ? 1 : 0);
		Main.drawRect(batch, getX(), getY(), getWidth(), getHeight(), 1);
		batch.setColor(1, 1, 1, 1);
	}

	public void updateActualPosition() {
		float x = viewport.x;
		float y = viewport.y;
		float viewportWidth = viewport.width;
		float viewportHeight = viewport.height;

		float originX = viewportWidth * screenOffset.x + pixelOffset.x;
		float originY = viewportHeight * screenOffset.y + pixelOffset.y;
		float originWidth = viewportWidth * screenOffset.width + pixelOffset.width;
		float originHeight = viewportHeight * screenOffset.height + pixelOffset.height;

		if ((align & Align.right) == Align.right) {
			actualPosition.x = viewportWidth - originX - originWidth + x;
		} else if ((align & Align.left) == Align.left) {
			actualPosition.x = originX + x;
		} else if ((align & Align.center) == Align.center) {
			actualPosition.x = viewportWidth * 0.5f - originWidth * 0.5f + originX + x;
		}

		if ((align & Align.top) == Align.top) {
			actualPosition.y = viewportHeight - originY - originHeight + y;
		} else if ((align & Align.bottom) == Align.bottom) {
			actualPosition.y = originY + y;
		} else if ((align & Align.center) == Align.center) {
			actualPosition.y = viewportHeight * 0.5f - originHeight * 0.5f + originY + y;
		}

		actualPosition.setSize(originWidth, originHeight);
	}

	/**
	 * Fired when the mouse is clicked
	 * @param x position of mouse in actor
	 * @param y position of mouse in actor
	 */
	public void onClicked(float x, float y) {
		if (x >= 0 && x <= 1 && y >= 0 && y <= 1) isPressed = true;
	}

	/**
	 * Fired when the mouse is released
	 * @param x position of mouse in actor
	 * @param y position of mouse in actor
	 */
	public void onClickRelease(float x, float y) {
		isPressed = false;

		if (x >= 0 && y >= 0 && x <= 1 && y <= 1) {
			onClickAction(x, y);
		}
	}

	public void onClickAction(float x, float y) {

	}

	public void onMouseDrag(float x, float y) {

	}

	public void onMouseMove(float x, float y) {

	}

	public boolean onKeyAction(int key) {
		for (int keycode : getKeybinds().items) {
			if (key == keycode) {
				onClickAction(0, 0);

				return true;
			}
		}

		return false;
	}

	public Rectangle getViewport() {
		return viewport;
	}

	public Actor setViewportToStageCamera() {
		viewport.set(0, 0, stage.getCamera().viewportWidth, stage.getCamera().viewportHeight);

		return this;
	}

	public Actor align(int align) {
		this.align = align;
		updateActualPosition();

		return this;
	}

	public int getAlign() {
		return align;
	}

	public float getAlpha() {
		return alpha;
	}

	public Actor setAlpha(float alpha) {
		this.alpha = alpha;

		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public Actor setVisible(boolean visible) {
		this.visible = visible;

		return this;
	}

	public Actor setEnabled(boolean enabled) {
		this.enabled = enabled;

		return this;
	}

	public boolean isPressed() {
		return isPressed;
	}

	public Actor setPressed(boolean pressed) {
		isPressed = pressed;

		return this;
	}

	public float getScreenOffsetX() {
		return screenOffset.x;
	}

	public float getScreenOffsetY() {
		return screenOffset.y;
	}

	public float getScreenOffsetWidth() {
		return screenOffset.width;
	}

	public float getScreenOffsetHeight() {
		return screenOffset.height;
	}

	public float getPixelOffsetX() {
		return pixelOffset.x;
	}

	public float getPixelOffsetY() {
		return pixelOffset.y;
	}

	public float getPixelOffsetWidth() {
		return pixelOffset.width;
	}

	public float getPixelOffsetHeight() {
		return pixelOffset.height;
	}

	public Actor setScreenOffset(float x, float y, float w, float h) {
		screenOffset.set(x, y, w, h);
		updateActualPosition();

		return this;
	}

	public Actor setScreenOffset(float x, float y) {
		return setScreenOffset(x, y, screenOffset.width, screenOffset.height);
	}

	public Actor setScreenOffsetSize(float w, float h) {
		return setScreenOffset(screenOffset.x, screenOffset.y, w, h);
	}

	public Actor setPixelOffset(float x, float y, float w, float h) {
		pixelOffset.set(x, y, w, h);
		updateActualPosition();

		return this;
	}

	public Actor setPixelOffset(float x, float y) {
		return setPixelOffset(x, y, pixelOffset.width, pixelOffset.height);
	}

	public Actor setPixelOffsetSize(float w, float h) {
		return setPixelOffset(pixelOffset.x, pixelOffset.y, w, h);
	}

	public float getX() {
		return actualPosition.x;
	}

	public float getY() {
		return actualPosition.y;
	}

	public float getWidth() {
		return actualPosition.width;
	}

	public float getHeight() {
		return actualPosition.height;
	}

	public void onResize(int width, int height) {
		setViewportToStageCamera();
		updateActualPosition();
	}

	public IntArray getKeybinds() {
		return keybinds;
	}

}
