package ionium.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import ionium.templates.Main;
import ionium.util.resolution.AspectRatio;
import ionium.util.resolution.ResolutionDeterminator;

import java.util.concurrent.TimeUnit;

public class Utils {

	private Utils() {
	}

	private static GlyphLayout glyphLayout = new GlyphLayout();
	private static IntMap<Boolean> pressedButtons = new IntMap<>();
	private static IntMap<Boolean> pressedKeys = new IntMap<>();
	private static Cursor cursor = null;

	public static boolean argumentsOverrode = false;

	public static float getWidth(BitmapFont font, String text) {
		glyphLayout.setText(font, text);
		return glyphLayout.width;
	}

	public static float getHeight(BitmapFont font, String text) {
		glyphLayout.setText(font, text);
		return glyphLayout.height;
	}

	public static float getHeightWithWrapping(BitmapFont font, String text, float width) {
		glyphLayout.setText(font, text, Color.WHITE, width, Align.left, true);
		return glyphLayout.height;
	}

	public static float getWidthWithWrapping(BitmapFont font, String text, float width) {
		glyphLayout.setText(font, text, Color.WHITE, width, Align.left, true);
		return glyphLayout.width;
	}


	public static void setArgumentsOverrideSettings(boolean b) {
		argumentsOverrode = b;
	}

	public static void resizeScreenFromSettings(int width, int height, boolean fs,
			AspectRatio[] ratios) {

		// searches in order of this:
		// if it's not fullscreen, set it windowed to the settings
		// otherwise
		// do fullscreen at the equal-or-smaller fullscreen to the settings

		if (!argumentsOverrode) {
			if (!fs) {
				Gdx.graphics.setWindowedMode(width, height);
			} else {
				Gdx.graphics.setFullscreenMode(ResolutionDeterminator.findMostIdealDisplayMode(
						Gdx.graphics.getMonitor(), width, height, ratios));
			}
		}

		Main.logger.info(
				"Set window size to " + (Gdx.graphics.isFullscreen() ? "fullscreen" : "windowed")
						+ " " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight()
						+ (argumentsOverrode ? " (arguments overrode old settings of [w, h, fs]: ["
								+ width + ", " + height + ", " + fs + "])" : ""));
	}

	public static <T> T findFirstInstance(Array array, Class<T> clazz) {
		for (int i = 0; i < array.size; i++) {
			if (array.get(i).getClass() == clazz) {
				return (T) array.get(i);
			}
		}

		return null;
	}

	public static void setCursorVisibility(boolean visible) {
		if (visible) {
			Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
		} else {
			if (cursor == null) {
				cursor = Gdx.graphics.newCursor(Main.clearPixmapCursor, 0, 0);
			}

			Gdx.graphics.setCursor(cursor);
		}
	}

	public static <T> boolean addToArray(T[] array, T toadd) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = toadd;
				return true;
			}
		}

		return false;
	}

	public static int getUnsignedByte(byte b) {
		return (b & 0xFF);
	}

	//	public static float getSoundPan(float xpos, float camerax) {
	//		return MathUtils
	//				.clamp(((xpos - (Math.round((camerax + (Settings.DEFAULT_WIDTH / 2))
	//						/ World.tilesizex))) / (((Settings.DEFAULT_WIDTH / 2f) - World.tilesizex) / World.tilesizex)),
	//						-1f, 1f);
	//	}

	public static void drawRotatedCentered(Batch batch, Texture tex, float x, float y, float width,
			float height, float rotation, boolean clockwise) {
		drawRotated(batch, tex, x - width * 0.5f, y - width * 0.5f, width, height, rotation,
				clockwise);
	}

	public static void drawRotated(Batch batch, Texture tex, float x, float y, float width,
			float height, float rotation, boolean clockwise) {
		drawRotated(batch, tex, x, y, width, height, width * 0.5f, height * 0.5f, rotation,
				clockwise, 0, 0, tex.getWidth(), tex.getHeight());
	}

	public static void drawRotated(Batch batch, Texture tex, float x, float y, float width,
			float height, float centerX, float centerY, float rotation, boolean clockwise) {
		drawRotated(batch, tex, x, y, width, height, centerX, centerY, rotation, clockwise, 0, 0,
				tex.getWidth(), tex.getHeight());
	}

	public static void drawRotated(Batch batch, Texture tex, float x, float y, float width,
			float height, float centerX, float centerY, float rotation, boolean clockwise, int srcX,
			int srcY, int srcWidth, int srcHeight) {
		batch.draw(tex, x, y, centerX, centerY, width, height, 1, 1,
				rotation * (clockwise ? -1 : 1), srcX, srcY, srcWidth, srcHeight, false, false);
	}

	public static void drawRotated(Batch batch, TextureRegion tex, float x, float y, float width,
			float height, float centerX, float centerY, float rotation, boolean clockwise) {
		batch.draw(tex, x, y, centerX, centerY, width, height, 1, 1,
				rotation * (clockwise ? -1 : 1), false);
	}

	public static void drawRotated(Batch batch, TextureRegion tex, float x, float y, float width,
			float height, float rotation, boolean clockwise) {
		drawRotated(batch, tex, x, y, width, height, width * 0.5f, height * 0.5f, rotation,
				clockwise);
	}

	public static void drawRotatedCentered(Batch batch, TextureRegion tex, float x, float y,
			float width, float height, float rotation, boolean clockwise) {
		drawRotated(batch, tex, x - width * 0.5f, y - height * 0.5f, width, height, rotation,
				clockwise);
	}

	public static int HSBtoRGBA8888(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}
		return (r << 24) | (g << 16) | (b << 8) | 0x000000ff;
	}

	public static String formatMs(long millis) {
		return String.format("%02d:%02d:%02d.%03d", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
				TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
				millis % 1000);
	}

	public static final double epsilon = 0.015625;

	public static boolean compareFloat(float x, float y) {
		if (Math.abs(x - y) < epsilon) {
			return true;
		} else return false;
	}

	public static char getRandomLetter() {
		char randomChar = (char) ((int) 'A' + Math.random() * ((int) 'Z' - (int) 'A' + 1));
		return randomChar;
	}

	/**
	 * Draws a baked texture that's masked with the mask texture. Use with the mask shader.
	 * <br>
	 * Only the parts that are visible in the mask texture will be affect the parts in the baked texture.
	 */
	public static void drawMaskedTexture(Main main, Batch batch, Texture theMask, Texture baked,
			float x, float y, float width, float height) {
		Main.useMask(theMask);
		batch.draw(baked, x, y, width, height);
	}

	/**
	 * Does magic number work to set up the noise shader to cover a percentage of sprite at zoom 2.0;
	 */
	public static void setupMaskingNoiseShader(ShaderProgram program, float percentage) {
		program.setUniformf("zoom", 2f);
		program.setUniformf("intensity", (float) (percentage * 2.5f));
	}

	public static String repeat(String s, int times) {
		String r = "";
		for (int i = 0; i < times; i++) {
			r += s;
		}
		return r;
	}

	public static boolean isButtonJustPressed(int button) {
		if (Gdx.input.isButtonPressed(button)) {
			if (pressedButtons.get(button) == null || !pressedButtons.get(button)) {
				pressedButtons.put(button, true);

				return true;
			}
		} else {
			pressedButtons.put(button, false);
		}

		return false;
	}

	public static boolean isButtonJustReleased(int button) {
		if (!Gdx.input.isButtonPressed(button)) {
			if (pressedButtons.get(button) == null) pressedButtons.put(button, false);

			boolean oldState = pressedButtons.get(button);

			if (oldState == true) {
				pressedButtons.put(button, false);

				return true;
			}
		} else {
			pressedButtons.put(button, true);
		}

		return false;
	}

	public static boolean isKeyJustReleased(int key) {
		if (!Gdx.input.isKeyPressed(key)) {
			if (pressedKeys.get(key) == null) pressedKeys.put(key, false);

			if (pressedKeys.get(key) == true) {
				pressedKeys.put(key, false);

				return true;
			}
		} else {
			pressedKeys.put(key, true);
		}

		return false;
	}

	/**
	 * packs 2 ints into a long
	 * @param ms most significant 32 bits
	 * @param ls least significant 32 bits
	 * @return
	 */
	public static long packLong(int ms, int ls) {
		return ((long) ms << 32) | (ls & 0xFFFFFFFFL);
	}

	public static int unpackLongUpper(long l) {
		return (int) (l >> 32);
	}

	public static int unpackLongLower(long l) {
		return (int) l;
	}
}
