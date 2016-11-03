package ionium.util.resolution;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;

/**
 * Helps determine resolution.
 * 
 *
 */
public class ResolutionDeterminator {

	/**
	 * Gets ideal fullscreen display mode from a target width, height, and allowed aspect ratios
	 * @param mon
	 * @param width
	 * @param height
	 * @param ratios
	 * @return
	 */
	public static DisplayMode findMostIdealDisplayMode(Monitor mon, int width, int height,
			AspectRatio[] ratios) {

		int largestSoFar = -1;

		for (int i = 0; i < Gdx.graphics.getDisplayModes(mon).length; i++) {
			DisplayMode dm = Gdx.graphics.getDisplayModes(mon)[i];

			// perfect match
			if (dm.width == width && dm.height == height) {
				return dm;
			}

			if (largestSoFar == -1) {
				largestSoFar = i;
			} else if ((dm.width > Gdx.graphics.getDisplayModes(mon)[largestSoFar].width
					|| dm.height > Gdx.graphics.getDisplayModes(mon)[largestSoFar].height)) {
				// bigger than largest

				// fits in target width/height
				if (dm.width <= width && dm.height <= height) {
					// matchs aspect ratio
					if (matchesAnAspectRatio(dm.width, dm.height, ratios)) {
						largestSoFar = i;
					}
				}
			}
		}

		if (largestSoFar == -1) {
			return Gdx.graphics.getDisplayMode(mon);
		} else {
			return Gdx.graphics.getDisplayModes(mon)[largestSoFar];
		}
	}

	public static DisplayMode findMostIdealDisplayMode(int width, int height,
			AspectRatio[] ratios) {
		return findMostIdealDisplayMode(Gdx.graphics.getMonitor(), width, height, ratios);
	}

	public static boolean matchesAnAspectRatio(int w, int h, AspectRatio[] ratios) {
		AspectRatio temp = new AspectRatio(w, h);

		for (AspectRatio a : ratios) {
			if (temp.equals(a)) return true;
		}

		return false;
	}

	public static boolean doesResolutionFit(Resolution r) {

		if (r.width > Gdx.graphics.getDisplayMode().width
				|| r.height > Gdx.graphics.getDisplayMode().height) {
			return false;
		}

		return true;
	}

}
