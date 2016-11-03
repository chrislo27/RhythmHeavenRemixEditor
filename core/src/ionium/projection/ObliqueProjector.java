package ionium.projection;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import ionium.projection.CoordinateF.CoordFPool;
import ionium.util.render.TexturedQuad;

public class ObliqueProjector {

	/**
	 * Flat orthographic view (no depth at all).
	 */
	public static final Vector3 DEPTH_FLAT = new Vector3(0, 0, 0);

	/**
	 * Cabinet view (depth is half length at a 45 degree angle).
	 */
	public static final Vector3 DEPTH_CABINET = new Vector3(0.5f, 0, 0.5f);

	/**
	 * Cavalier view (depth is full length at a 45 degree angle).
	 */
	public static final Vector3 DEPTH_CAVALIER = new Vector3(1f, 0, 1f);

	public Vector3 depth = DEPTH_CABINET.cpy();

	private final Batch batch;

	public ObliqueProjector(Batch b) {
		batch = b;
	}

	// render order
	/*
	 * left to right
	 * bottom to top
	 * back to front
	 */

	/**
	 * Parameters passed in act like an orthographic view.
	 * @param tex
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 */
	public void render(RenderFace face, Texture tex, float x, float y, float z, float width,
			float height) {
		render(face, tex, x, y, z, width, height, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, false);
	}

	/**
	 * Parameters passed in act like an orthographic view. Includes arguments to offset the positions.
	 * @param face
	 * @param tex
	 * @param x
	 * @param y
	 * @param z
	 * @param width
	 * @param height
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param x4
	 * @param y4
	 */
	public void render(RenderFace face, Texture tex, float x, float y, float z, float width,
			float height, float x1, float y1, float z1, float x2, float y2, float z2, float x3,
			float y3, float z3, float x4, float y4, float z4, boolean flipHor, boolean flipVert) {
		CoordinateF coord1 = convertToProjected(x, y, z);
		CoordinateF coord2 = convertToProjected(x + width, y, z);
		CoordinateF coord3 = convertToProjected(x + width, y + height, z);
		CoordinateF coord4 = convertToProjected(x, y + height, z);

		CoordinateF[] offsets = { convertToProjected(x1, y1, z1), convertToProjected(x2, y2, z2),
				convertToProjected(x3, y3, z3), convertToProjected(x4, y4, z4) };

		if (face == RenderFace.SIDE) {
			coord2.set(coord1.x, coord1.y);
			coord3.set(coord4.x, coord4.y);
			coord2.translateToDepth(width, height, depth);
			coord3.translateToDepth(width, height, depth);
		} else if (face == RenderFace.TOP) {
			coord3.set(coord2.x, coord2.y).translateToDepth(width, height, depth);
			coord4.set(coord1.x, coord1.y).translateToDepth(width, height, depth);
		}

		TexturedQuad.renderQuad(batch, tex, coord1.x + offsets[0].x, coord1.y + offsets[0].y,
				coord2.x + offsets[1].x, coord2.y + offsets[1].y, coord3.x + offsets[2].x,
				coord3.y + offsets[2].y, coord4.x + offsets[3].x, coord4.y + offsets[3].y,
				(flipHor ? 1 : 0), (flipVert ? 1 : 0), (flipHor ? 0 : 1), (flipVert ? 0 : 1));

		CoordFPool.pool.free(coord1);
		CoordFPool.pool.free(coord2);
		CoordFPool.pool.free(coord3);
		CoordFPool.pool.free(coord4);
		for (int i = offsets.length - 1; i >= 0; i--) {
			CoordFPool.pool.free(offsets[i]);
		}
	}

	/**
	 * Converts from world to screen
	 * @param coord
	 * @return
	 */
	public CoordinateF convertToProjected(CoordinateF coord, float x, float y, float z) {
		coord.setX(x);
		coord.setY(y + (x * depth.y));

		coord.set(coord.x + (z * depth.x), coord.y + (z * depth.z));

		return coord;
	}

	public CoordinateF convertToProjected(float x, float y, float z) {
		return convertToProjected(CoordFPool.pool.obtain(), x, y, z);
	}

	public static enum RenderFace {
		FRONT, SIDE, TOP;
	}

}
