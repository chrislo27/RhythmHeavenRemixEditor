package ionium.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import ionium.templates.Main;

public class Eat implements Transition {

	public Eat(int placements, int speed) {
		if (speed <= 0) throw new IllegalArgumentException(
				"Speed value must be greater than 0 : got " + speed);
		this.speed = speed;
		sizex = (int) (Gdx.graphics.getWidth() / tilewidthpx) + 2;
		sizey = (int) (Gdx.graphics.getHeight() / tileheightpx) + 2;

		traversed = new boolean[sizex][sizey];
		temp = new boolean[sizex][sizey];

		for (int x = 0; x < sizex; x++) {
			for (int y = 0; y < sizey; y++) {
				traversed[x][y] = false;
				temp[x][y] = false;
			}
		}

		for (int i = 0; i < placements; i++) {
			traversed[MathUtils.random(1, sizex) - 1][MathUtils.random(1, sizey) - 1] = true;
		}

	}

	public Eat setColors(Color d, Color o) {
		dark = d;
		outline = o;
		return this;
	}

	int speed = 1;
	int sizex;
	int sizey;
	final float tilewidthpx = 16f;
	final float tileheightpx = 9f;

	boolean[][] traversed;
	boolean[][] temp;

	boolean useOutline = false;

	@Override
	public boolean finished() {
		for (int x = 0; x < sizex; x++) {
			for (int y = 0; y < sizey; y++) {
				if (traversed[x][y] == false) return false;
			}
		}

		return true;
	}

	Color dark = Color.BLACK;
	Color outline = Color.WHITE;

	private boolean filledAdjacent(int x, int y) {
		if (x > 0) {
			if (traversed[x - 1][y]) {
				return true;
			}
		} else if (x < sizex - 1) {
			if (traversed[x + 1][y]) {
				return true;
			}
		} else if (y > 0) {
			if (traversed[x][y - 1]) {
				return true;
			}
		} else if (y < sizey - 1) {
			if (traversed[x][y + 1]) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(Main main) {
		main.batch.setColor(dark);
		for (int x = 0; x < sizex; x++) {
			for (int y = 0; y < sizey; y++) {
				if (useOutline) {
					if (traversed[x][y]) {
						main.batch.setColor(dark);
						Main.fillRect(main.batch, x * (tilewidthpx),
								Main.convertY(y * (tileheightpx) + (tileheightpx)), (tilewidthpx),
								(tileheightpx));
					} else if (!traversed[x][y] && filledAdjacent(x, y)) {
						main.batch.setColor(outline);
						Main.fillRect(main.batch, x * (tilewidthpx),
								Main.convertY(y * (tileheightpx) + (tileheightpx)), (tilewidthpx),
								(tileheightpx));
					}
				} else {
					if (traversed[x][y]) {
						Main.fillRect(main.batch, x * (tilewidthpx),
								Main.convertY(y * (tileheightpx) + (tileheightpx)), (tilewidthpx),
								(tileheightpx));
					}
				}
			}
		}
		main.batch.setColor(Color.WHITE);
	}

	@Override
	public void tickUpdate(Main main) {
		for (int i = 0; i < speed; i++) {
			for (int x = 0; x < sizex; x++) {
				for (int y = 0; y < sizey; y++) {
					temp[x][y] = false;
				}
			}

			for (int x = 0; x < sizex; x++) {
				for (int y = 0; y < sizey; y++) {
					if (!temp[x][y] && traversed[x][y]) {
						temp[x][y] = true;
						if (x > 0 && !(MathUtils.random(1, 2) == 1)) {
							if (!traversed[x - 1][y]) {
								traversed[x - 1][y] = true;
								temp[x - 1][y] = true;
							}
						}
						if (x < sizex - 1 && !(MathUtils.random(1, 2) == 1)) {
							if (!traversed[x + 1][y]) {
								traversed[x + 1][y] = true;
								temp[x + 1][y] = true;
							}
						}
						if (y > 0 && !(MathUtils.random(1, 2) == 1)) {
							if (!traversed[x][y - 1]) {
								traversed[x][y - 1] = true;
								temp[x][y - 1] = true;
							}
						}
						if (y < sizey - 1 && !(MathUtils.random(1, 2) == 1)) {
							if (!traversed[x][y + 1]) {
								traversed[x][y + 1] = true;
								temp[x][y + 1] = true;
							}
						}

					}
				}
			}
		}

	}

}
