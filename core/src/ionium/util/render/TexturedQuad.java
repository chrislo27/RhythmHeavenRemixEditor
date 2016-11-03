package ionium.util.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TexturedQuad {

	private static float[] vertices = new float[25];
	private static float WHITE_FLOAT_BITS = new Color(1, 1, 1, 1).toFloatBits();

	/**
	 * Renders a quad, with vertices in order from bottom left, bottom right, top right, top left.
	 * @param batch
	 * @param tex
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param x4
	 * @param y4
	 * @param u1
	 * @param v1
	 * @param u2
	 * @param v2
	 */
	public static void renderQuad(Batch batch, Texture tex, float x1, float y1, float x2, float y2,
			float x3, float y3, float x4, float y4, float u1, float v1, float u2, float v2) {
		int id = 0;

		// x, y (1)
		vertices[id++] = x1;
		vertices[id++] = y1;
		vertices[id++] = WHITE_FLOAT_BITS;
		vertices[id++] = u1;
		vertices[id++] = v2;

		// x + w, y (2)
		vertices[id++] = x2;
		vertices[id++] = y2;
		vertices[id++] = WHITE_FLOAT_BITS;
		vertices[id++] = u2;
		vertices[id++] = v2;

		// x + w, y + h (3)
		vertices[id++] = x3;
		vertices[id++] = y3;
		vertices[id++] = WHITE_FLOAT_BITS;
		vertices[id++] = u2;
		vertices[id++] = v1;

		// x, y + h (4)
		vertices[id++] = x4;
		vertices[id++] = y4;
		vertices[id++] = WHITE_FLOAT_BITS;
		vertices[id++] = u1;
		vertices[id++] = v1;

		// x, y (1)
		vertices[id++] = x1;
		vertices[id++] = y1;
		vertices[id++] = WHITE_FLOAT_BITS;
		vertices[id++] = u1;
		vertices[id++] = v2;

		batch.draw(tex, vertices, 0, vertices.length);
		batch.flush();
	}

	public static void renderQuad(Batch batch, TextureRegion reg, float x1, float y1, float x2,
			float y2, float x3, float y3, float x4, float y4) {
		renderQuad(batch, reg.getTexture(), x1, y1, x2, y2, x3, y3, x4, y4, reg.getU(), reg.getV(),
				reg.getU2(), reg.getV2());
	}

}
