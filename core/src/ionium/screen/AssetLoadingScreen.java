package ionium.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import ionium.registry.AssetRegistry;
import ionium.registry.GlobalVariables;
import ionium.templates.Main;
import ionium.util.AssetLogger;
import ionium.util.Utils;

public class AssetLoadingScreen extends MiscLoadingScreen {

	public AssetLoadingScreen(Main m) {
		super(m);
		AssetRegistry.instance().getAssetManager().setLogger(output);
	}

	private AssetLogger output = new AssetLogger("assetoutput", Logger.DEBUG);

	private long startms = 0;
	private boolean finished = false;

	@Override
	public void render(float delta) {
		AssetManager manager = AssetRegistry.instance().getAssetManager();

		AssetRegistry.instance().loadManagedAssets(((int) (1000f / GlobalVariables.maxFps)));
		do {
			if (AssetRegistry.instance().finishedLoading()) {
				if (canFinishLoading()) {
					AssetRegistry.instance().optionalOnFinish();
					onFinishLoading();

					main.setScreen(main.getScreenToSwitchToAfterLoadingAssets());
					break;
				}

				if (!finished) {
					finished = true;

					Main.logger.info("Finished loading all managed assets, took "
							+ (System.currentTimeMillis() - startms) + " ms");
				}

			}
		} while (false);

		super.render(delta);

		main.batch.begin();
		main.batch.setColor(1, 1, 1, 1);

		Main.drawRect(main.batch, Gdx.graphics.getWidth() * 0.25f - 4,
				Gdx.graphics.getHeight() * 0.5f - 8 - 4, Gdx.graphics.getWidth() * 0.5f + 8, 16 + 8,
				2);

		Main.fillRect(main.batch, Gdx.graphics.getWidth() * 0.25f,
				Gdx.graphics.getHeight() * 0.5f - 8,
				Gdx.graphics.getWidth() * 0.5f * manager.getProgress(), 16);

		if (manager.getAssetNames().size > 0) {
			main.drawTextBg(main.defaultFont, output.getLastMsg(),
					Gdx.graphics.getWidth() * 0.5f
							- (Utils.getWidth(main.defaultFont, output.getLastMsg()) * 0.5f),
					Gdx.graphics.getHeight() * 0.5f - 35);
		}

		String outOf = manager.getLoadedAssets() + " / "
				+ (manager.getLoadedAssets() + manager.getQueuedAssets());
		main.drawTextBg(main.defaultFont, outOf,
				Gdx.graphics.getWidth() * 0.5f - (Utils.getWidth(main.defaultFont, outOf) * 0.5f),
				Gdx.graphics.getHeight() * 0.5f - 60);

		String percent = String.format("%.0f", (manager.getProgress() * 100f)) + "%";
		main.drawTextBg(main.defaultFont, percent,
				Gdx.graphics.getWidth() * 0.5f - (Utils.getWidth(main.defaultFont, percent) * 0.5f),
				Gdx.graphics.getHeight() * 0.5f - 85);

		main.batch.end();
	}

	public boolean canFinishLoading() {
		return true;
	}

	protected final boolean shouldFinishLoading() {
		return AssetRegistry.instance().getAssetManager().getProgress() >= 1;
	}

	public void onFinishLoading() {

	}

	@Override
	public void tickUpdate() {

	}

	@Override
	public void show() {
		startms = System.currentTimeMillis();
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

	@Override
	public void getDebugStrings(Array<String> array) {

	}

	@Override
	public void renderUpdate() {
	}

}
