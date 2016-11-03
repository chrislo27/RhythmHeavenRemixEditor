package ionium.util.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import ionium.templates.Main;
import ionium.util.noise.SimplexNoise;

public class LiquidContainer {

	public float width, height;
	/**
	 * Gap between vertices
	 */
	float waveGap = 4;
	float maxPeak = 32;
	Color color = new Color();

	float[] waves;
	float endWave;

	SimplexNoise noise = new SimplexNoise(System.currentTimeMillis());
	float secondsElapsed = 0;

	public LiquidContainer(float w, float h, float gap, float peak, Color c) {
		width = w;
		height = h;
		waveGap = gap;
		color = c;
		maxPeak = peak;

		waves = new float[(int) Math.max(width / waveGap, 2f)];
	}

	protected void renderUpdate(Main main) {
		secondsElapsed += Gdx.graphics.getDeltaTime();
		
		for(int i = 0; i < 4; i++){
			smooth(main);
		}
	}
	
	private void smooth(Main main){
		for (int i = 0; i < waves.length; i++) {
			float old = waves[i];

			if (i > 0) {
				waves[i - 1] += old / 4;
			}

			if (i < waves.length - 1) {
				waves[i + 1] += old / 4;
			} else {
				endWave += old / 4;
			}

			waves[i] /= 2;

		}

		waves[waves.length - 1] += endWave / 4;
		endWave /= 2;
	}

	public void perturb(float x, float intensity) {
		waves[(int) ((width * x) / waveGap)] += intensity * MathUtils.random()
				* MathUtils.randomSign();
	}
	
	public void perturbAll(float intensity){
		for(int i = 0; i < waves.length; i++){
			perturb(MathUtils.random(), intensity);
		}
	}

	public void render(Main main, OrthographicCamera camera, float x, float y) {
		renderUpdate(main);

		main.verticesRenderer.begin(camera.combined, GL20.GL_TRIANGLES);

		float lowestPoint = endWave * maxPeak;

		for (int i = 0; i < waves.length; i++) {
			if (waves[i] * maxPeak < lowestPoint) {
				lowestPoint = waves[i] * maxPeak;
			}
		}

		// first draw a rectangle where the the liquid doesn't have spikes
		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x, y, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width, y, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width, y + height + lowestPoint, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width, y + height + lowestPoint, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x, y + height + lowestPoint, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x, y, 0);

		// then render all our spikes with three points:
		// the vertex before it (or x, y if it's at index 0)
		// the current vertex
		// lowest point with X at the next area

		for (int i = 0; i < waves.length; i++) {
			float vertex = waves[i];
			float xPos = i * waveGap;

			// before
			main.verticesRenderer.color(color);
			if (i <= 0) {
				// starting point
				main.verticesRenderer.vertex(x, y + height + lowestPoint, 0);
			} else {
				// one before it
				main.verticesRenderer.vertex(x + xPos - waveGap, y + height + waves[i - 1]
						* maxPeak, 0);
			}

			// center
			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(x + xPos, y + height + vertex * maxPeak, 0);

			// after
			main.verticesRenderer.color(color);
			if (i >= waves.length - 1) {
				main.verticesRenderer.vertex(x + xPos + waveGap, y + height + endWave * maxPeak, 0);
			} else {
				main.verticesRenderer.vertex(x + xPos + waveGap, y + height + waves[i + 1]
						* maxPeak, 0);
			}

			// filling

			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(x + xPos, y + height + vertex * maxPeak, 0);

			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(x + xPos - waveGap + (i == 0 ? waveGap : 0), y + height
					+ lowestPoint, 0);

			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(
					x + xPos + waveGap - (i == waves.length - 1 ? waveGap : 0), y + height
							+ lowestPoint, 0);

			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(x + xPos, y + height + vertex * maxPeak, 0);

			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(x + xPos + (waveGap / 2f), y + height + lowestPoint, 0);

			main.verticesRenderer.color(color);
			main.verticesRenderer.vertex(x + xPos + waveGap, y + height
					+ (i == waves.length - 1 ? endWave : waves[i + 1]) * maxPeak, 0);
		}

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width, y + height + endWave * maxPeak, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width, y + height + lowestPoint, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width - waveGap / 2, y + height + lowestPoint, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width - waveGap, y + height + waves[waves.length - 1]
				* maxPeak, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width - waveGap, y + height + lowestPoint, 0);

		main.verticesRenderer.color(color);
		main.verticesRenderer.vertex(x + width - waveGap / 2, y + height + lowestPoint, 0);

		main.verticesRenderer.end();
	}

}
