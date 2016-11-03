package ionium.util.resolution;

import com.badlogic.gdx.math.MathUtils;

public class AspectRatio {

	public final int width;
	public final int height;

	public AspectRatio(int w, int h) {
		width = w;
		height = h;
	}

	public double getAspectRatio() {
		return (((double) width) / ((double) height));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AspectRatio) {
			AspectRatio ar = (AspectRatio) obj;

			if (MathUtils.isEqual((float) getAspectRatio(), (float) ar.getAspectRatio()))
				return true;
		}

		return super.equals(obj);
	}

}
