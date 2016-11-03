package ionium.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import ionium.templates.Main;

public class Spiral implements Transition {

	public Spiral() {
		this(4);
	}

	public Spiral(int time) {
		tilespertick = time;
		tilewidth = Gdx.graphics.getWidth() / 16;
		tileheight = Gdx.graphics.getHeight() / 9;

		traversed = new boolean[tilewidth][tileheight];
	}

	int tilewidth;
	int tileheight;
	int tilespertick;
	int tilescovered = 0;

	boolean[][] traversed;

	@Override
	public boolean finished() {
		return tilescovered >= (16 * 9);
	}

	int cx = 0;
	int cy = 0;
	/**
	 * 0 - 3 = up down left right
	 */
	int state = 1;

	Color color = Color.BLACK;

	@Override
	public void render(Main main) {

		main.batch.setColor(color);
		for (int x = 0; x < tilewidth; x++) {
			for (int y = 0; y < tileheight; y++) {
				if (traversed[x][y]) Main.fillRect(main.batch, x * (tilewidth),
						Main.convertY(y * (tileheight) + (tileheight)), (tilewidth), (tileheight));
			}
		}
		main.batch.setColor(Color.WHITE);

	}

	public Spiral setColor(Color c) {
		color = c;
		return this;
	}

	@Override
	public void tickUpdate(Main main) {

		for (int i = 0; i < tilespertick; i++) {
			if (!traversed[cx][cy]) {
				traversed[cx][cy] = true;
				tilescovered++;
			}

			switch (state) {
			case 0: // up -> go left
				cy--;
				if (cy <= -1 || (traversed[cx][cy])) {
					cy++;
					state = 2;
				}
				break;
			case 1: // down -> go right
				cy++;
				if (cy >= 9 || (traversed[cx][cy])) {
					cy--;
					state = 3;
				}
				break;
			case 2: // left -> go down
				cx--;
				if (cx <= -1 || (traversed[cx][cy])) {
					cx++;
					state = 1;
				}
				break;
			case 3: // right -> go up
				cx++;
				if (cx >= 16 || (traversed[cx][cy])) {
					cx--;
					state = 0;
				}
				break;
			}
		}

	}

}
