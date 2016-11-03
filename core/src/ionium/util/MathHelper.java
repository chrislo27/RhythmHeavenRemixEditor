package ionium.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class MathHelper {

	private MathHelper() {
	}

	private static final Rectangle rect1 = new Rectangle();
	private static final Rectangle rect2 = new Rectangle();

	public static final double rootTwo = Math.sqrt(2f);

	public static float lerp(float x, float y, float alpha) {
		return (float) lerp((double) x, y, alpha);
	}

	public static double lerp(double x, double y, double alpha) {
		return x + alpha * (y - x);
	}

	public static float snapToNearest(float number, float interval) {
		interval = Math.abs(interval);

		if (interval == 0) return number;

		return Math.round(number / interval) * interval;
	}

	public static float lockAtIntervals(float number, float interval) {
		interval = Math.abs(interval);

		if (interval == 0) return number;

		return ((int) (number / interval)) * interval;
	}

	/**
	 * Normalizes a rectangle to have positive width and height
	 */
	public static void normalizeRectangle(Rectangle r) {
		if (r.width < 0) {
			r.width = Math.abs(r.width);
			r.x -= r.width;
		}

		if (r.height < 0) {
			r.height = Math.abs(r.height);
			r.y -= r.height;
		}
	}

	public static double getScaleFactor(float iMasterSize, float iTargetSize) {
		double dScale = 1;
		if (iMasterSize > iTargetSize) {
			dScale = (double) iTargetSize / (double) iMasterSize;
		} else {
			dScale = (double) iTargetSize / (double) iMasterSize;
		}
		return dScale;
	}

	public static float calcRotationAngleInDegrees(float x, float y, float tx, float ty) {
		float theta = MathUtils.atan2(tx - x, ty - y);

		float angle = theta * MathUtils.radiansToDegrees;

		if (angle < 0) {
			angle += 360;
		}
		angle += 180;

		return angle;
	}

	public static float calcRotationAngleInRadians(float x, float y, float tx, float ty) {
		return calcRotationAngleInDegrees(x, y, tx, ty) * MathUtils.degreesToRadians;
	}

	public static double calcRadiansDiff(float x, float y, float tx, float ty) {
		double d = calcRotationAngleInDegrees(x, y, tx, ty);
		d -= 90;
		d %= 360;
		return Math.toRadians(d);
	}

	public static float timePulse(float num) {
		return ((num > 0.5f ? (0.5f - (num - 0.5f)) : num))
				- MathUtils.clamp(0.50000001f, 1f, 0.5f);
	}

	public static boolean isEven(int num) {
		return (num & 1) == 0;
	}

	public static boolean isOdd(int num) {
		return !isEven(num);
	}

	public static double calcDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	public static double clamp(double val, double min, double max) {
		return Math.max(min, Math.min(max, val));
	}

	/**
	 * get a number from 0, 1 based on time
	 * 
	 * @return
	 */
	public static float getSawtoothWave() {
		return getSawtoothWave(System.currentTimeMillis(), 1);
	}

	public static float getSawtoothWave(float seconds) {
		return getSawtoothWave(System.currentTimeMillis(), seconds);
	}

	public static float getSawtoothWave(long time, float seconds) {
		if (seconds == 0) throw new IllegalArgumentException("Seconds cannot be zero!");
		return ((time % Math.round((seconds * 1000))) / (seconds * 1000f));
	}

	public static float getTriangleWave(long ms, float seconds) {
		float f = getSawtoothWave(ms, seconds);
		if (f >= 0.5f) {
			return (1f - f) * 2;
		} else return (f) * 2;
	}

	public static float getTriangleWave(float sec) {
		return getTriangleWave(System.currentTimeMillis(), sec);
	}

	public static float getTriangleWave() {
		return getTriangleWave(1f);
	}

	public static int getNthDigit(int number, int n) {
		int base = 10;
		return (int) ((number / Math.pow(base, n - 1)) % base);
	}

	public static int getNthDigit(long number, int n) {
		int base = 10;
		return (int) ((number / Math.pow(base, n - 1)) % base);
	}

	// original x, y, new x, y
	public static double getScaleFactorToFit(float ox, float oy, float nx, float ny) {
		double dScale = 1d;
		double dScaleWidth = getScaleFactor(ox, nx);
		double dScaleHeight = getScaleFactor(oy, ny);

		dScale = Math.min(dScaleHeight, dScaleWidth);

		return dScale;
	}

	public static boolean checkPowerOfTwo(int number) {
		if (number <= 0) {
			throw new IllegalArgumentException("Number is less than zero: " + number);
		}
		return ((number & (number - 1)) == 0);
	}

	//	public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
	//		if (val.compareTo(min) < 0) return min;
	//		else if (val.compareTo(max) > 0) return max;
	//		else return val;
	//	}

	public static float distanceSquared(float x, float y, float x2, float y2) {
		return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
	}

	public static float lightingCalc(int l) {
		return 1.0f - ((float) (logOfBase(15, l)));
	}

	public static double logOfBase(int base, int num) {
		return Math.log(num) / Math.log(base);
	}

	public static boolean isOneOfThem(int check, int[] these) {
		for (int i : these) {
			if (check == i) return true;
		}
		return false;
	}

	public static boolean isOneOfThem(int check, int com) {
		return check == com;
	}

	public static float getJumpVelo(double gravity, double distance) {
		return (float) (Math.sqrt(2 * distance * gravity));
	}

	public static boolean intersects(float x, float y, float width, float height, float x2,
			float y2, float width2, float height2) {
		return intersects(x, y, width, height, x2, y2, width2, height2, false);
	}

	public static boolean intersects(float x, float y, float width, float height, float x2,
			float y2, float width2, float height2, boolean touchingIsIntersecting) {
		makeRectangleValuesPositive(rect1.set(x, y, width, height));
		makeRectangleValuesPositive(rect2.set(x2, y2, width2, height2));

		if (touchingIsIntersecting) {
			if (rect1.x + rect1.width == rect2.x) return true;
			if (rect1.x == rect2.x + rect2.width) return true;
			if (rect1.y + rect1.height == rect2.y) return true;
			if (rect1.y == rect2.y + rect2.height) return true;
		}

		// true if: rect1 overlaps 2, or either contains
		return rect1.overlaps(rect2);
	}

	public static boolean isPointInRectangle(float rectX, float rectY, float rectW, float rectH,
			float x, float y) {
		if (x >= rectX && x <= rectX + rectW) {
			if (y >= rectY && y <= rectY + rectH) {
				return true;
			}
		}

		return false;
	}

	public static Rectangle makeRectangleValuesPositive(Rectangle rect) {
		MathHelper.normalizeRectangle(rect);

		return rect;
	}

}
