package ionium.tests;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import ionium.screen.Updateable;
import ionium.templates.Main;
import ionium.util.quadtree.QuadRectangleable;
import ionium.util.quadtree.QuadTree;

public class QuadTreeTestScreen extends Updateable<Main> {

	private class Rect extends Rectangle implements QuadRectangleable {

		public Rect(float x, float y, float w, float h) {
			super(x, y, w, h);
		}

	}

	QuadTree<Rect> quadtree = new QuadTree(1280, 720);
	Array<Rect> rects = new Array<>();
	Array<Rect> temp = new Array<>();

	public QuadTreeTestScreen(Main m) {
		super(m);

		for (int i = 0; i < 128; i++) {
			Rect r = new Rect(MathUtils.random(0, 1280), MathUtils.random(0, 720),
					MathUtils.random(32, 64), MathUtils.random(32, 64));

			rects.add(r);
		}

		Rect player = new Rect(MathUtils.random(0, 1280), MathUtils.random(0, 720),
				MathUtils.random(32, 64), MathUtils.random(32, 64));

		rects.add(player);
	}

	@Override
	public void render(float delta) {
		main.batch.begin();
		
		quadtree.clear();

		for (int i = 0; i < rects.size; i++) {
			Rect r = rects.get(i);

			quadtree.insert(r);
		}

		for (int i = 0; i < rects.size; i++) {
			Rect r = rects.get(i);

			if (i == rects.size - 1) {
				main.batch.setColor(0, 1, 0, 1);
			} else {
				main.batch.setColor(0.5f, 0.5f, 0.5f, 1);
			}

			Main.fillRect(main.batch, r.x, r.y, r.width, r.height);
			main.batch.setColor(1, 1, 1, 1);
			Main.drawRect(main.batch, r.x, r.y, r.width, r.height, 2);
		}

		temp.clear();
		quadtree.retrieve(temp, rects.get(rects.size - 1));

		for (int i = 0; i < temp.size; i++) {
			Rect r = temp.get(i);

			main.batch.setColor(0, 0, 1, 1);

			Main.fillRect(main.batch, r.x, r.y, r.width, r.height);
			main.batch.setColor(1, 1, 1, 1);
			Main.drawRect(main.batch, r.x, r.y, r.width, r.height, 2);
		}

		Rect r = rects.get(rects.size - 1);

		main.batch.setColor(0, 1, 0, 1);

		Main.fillRect(main.batch, r.x, r.y, r.width, r.height);
		main.batch.setColor(1, 1, 1, 1);
		Main.drawRect(main.batch, r.x, r.y, r.width, r.height, 2);

		quadtree.renderDebug(main.batch, 1);
		
		main.batch.end();
	}

	@Override
	public void renderUpdate() {
	}

	@Override
	public void tickUpdate() {
	}

	@Override
	public void getDebugStrings(Array<String> array) {
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

}
