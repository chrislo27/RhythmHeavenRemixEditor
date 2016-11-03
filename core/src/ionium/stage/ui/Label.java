package ionium.stage.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ionium.stage.Actor;
import ionium.stage.Stage;
import ionium.stage.ui.skin.Palette;

public abstract class Label extends Actor {

	private Palette palette;

	public Label(Stage s, Palette palette) {
		super(s);

		this.palette = palette;
	}

	public Label setPalette(Palette p) {
		palette = p;

		return this;
	}

	public Palette getPalette() {
		return palette;
	}

	@Override
	public abstract void render(SpriteBatch batch, float alpha);

}
