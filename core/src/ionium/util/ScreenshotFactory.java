package ionium.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotFactory {

	private ScreenshotFactory() {
	}

	public static void saveScreenshot() {
		try {
			FileHandle fh;
			String date = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date());
			date.trim();
			do {
				fh = new FileHandle("screenshots/screenshot_" + date + ".png");
			} while (fh.exists());
			Pixmap pixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
					true);
			PixmapIO.writePNG(fh, pixmap);
			pixmap.dispose();
		} catch (Exception e) {
		}
	}

	public static Pixmap getScreenshot(int x, int y, int w, int h, boolean yDown) {
		byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),
				Gdx.graphics.getBackBufferHeight(), true);

		Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(),
				Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
		BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
		return pixmap;
	}
}
