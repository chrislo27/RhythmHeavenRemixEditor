package ionium.util.quadtree;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import ionium.templates.Main;

public class QuadTree<E extends QuadRectangleable> {

	private static final int NODE_PARENT = -1;
	private static final int NODE_NW = 0;
	private static final int NODE_NE = 1;
	private static final int NODE_SE = 2;
	private static final int NODE_SW = 3;

	public static int maxObjects = 4;
	public static int maxNodes = 8;

	private int level = 0;
	private Array<E> objects = new Array<>();
	private Rectangle nodeBounds = new Rectangle();
	private QuadTree[] childNodes = new QuadTree[4];
	private boolean[] usedNodes = new boolean[4];

	public QuadTree(float worldWidth, float worldHeight) {
		level = 0;
		nodeBounds.set(0, 0, worldWidth, worldHeight);
	}

	private QuadTree(int level, float x, float y, float width, float height) {
		this.level = level;
		nodeBounds.set(x, y, width, height);
	}

	public void renderDebug(SpriteBatch batch, float thickness) {
		batch.setColor(0, 1, 0, 1);
		Main.drawRect(batch, nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height,
				thickness);
		batch.setColor(1, 1, 1, 1);

		for (int i = 0; i < childNodes.length; i++) {
			if (usedNodes[i]) {
				childNodes[i].renderDebug(batch, thickness);
			}
		}
	}

	public void clear() {
		objects.clear();

		for (int i = 0; i < childNodes.length; i++) {
			if (childNodes[i] != null) childNodes[i].clear();
			usedNodes[i] = false;
		}
	}

	private void split() {
		final int newLevel = level + 1;
		final float halfWidth = nodeBounds.width * 0.5f;
		final float halfHeight = nodeBounds.height * 0.5f;
		final float x = nodeBounds.x;
		final float y = nodeBounds.y;

		if (childNodes[NODE_NW] == null)
			childNodes[NODE_NW] = new QuadTree(newLevel, x, y + halfHeight, halfWidth, halfHeight);
		if (childNodes[NODE_NE] == null) childNodes[NODE_NE] = new QuadTree(newLevel, x + halfWidth,
				y + halfHeight, halfWidth, halfHeight);
		if (childNodes[NODE_SE] == null)
			childNodes[NODE_SE] = new QuadTree(newLevel, x + halfWidth, y, halfWidth, halfHeight);
		if (childNodes[NODE_SW] == null)
			childNodes[NODE_SW] = new QuadTree(newLevel, x, y, halfWidth, halfHeight);

		for (int i = 0; i < usedNodes.length; i++) {
			usedNodes[i] = true;
		}
	}

	/**
	 * Returns index of where an element can be placed. Returns NODE_PARENT if it doesn't fit.
	 * @param element
	 * @return node ID (parent, NW, NE, SE, SW)
	 */
	private int getIndex(E element) {
		int index = NODE_PARENT;

		final float xMidpoint = nodeBounds.x + nodeBounds.width * 0.5f;
		final float yMidpoint = nodeBounds.y + nodeBounds.height * 0.5f;

		boolean topHalf = element.getY() > yMidpoint;
		boolean bottomHalf = element.getY() + element.getHeight() < yMidpoint;
		boolean leftHalf = element.getX() + element.getWidth() < xMidpoint;
		boolean rightHalf = element.getX() > xMidpoint;

		if (leftHalf) {
			if (topHalf) {
				index = NODE_NW;
			} else if (bottomHalf) {
				index = NODE_SW;
			}
		} else if (rightHalf) {
			if (topHalf) {
				index = NODE_NE;
			} else if (bottomHalf) {
				index = NODE_SE;
			}
		}

		return index;
	}

	public void insert(E element) {
		// place in child node first
		int testIndex = getIndex(element);

		if (testIndex != NODE_PARENT && usedNodes[testIndex] != false) {
			childNodes[testIndex].insert(element);

			return;
		}

		// must be placed in this node
		objects.add(element);

		// split because we're too large
		if (level < maxNodes && objects.size > maxObjects) {
			split();

			for (int i = objects.size - 1; i >= 0; i--) {
				int index = getIndex(objects.get(i));

				if (index == NODE_PARENT) {
					continue;
				} else {
					childNodes[index].insert(objects.removeIndex(i));
				}
			}
		}
	}

	public Array<E> retrieve(Array<E> returnList, E reference) {
		if (reference == null) throw new IllegalArgumentException(
				"Reference element while retrieving cannot be null!");

		int index = getIndex(reference);

		if (index != NODE_PARENT && usedNodes[index] != false) {
			childNodes[index].retrieve(returnList, reference);
		}

		returnList.addAll(objects);

		return returnList;
	}

}