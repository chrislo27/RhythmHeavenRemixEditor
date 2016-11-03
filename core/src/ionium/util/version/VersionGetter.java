package ionium.util.version;

import com.badlogic.gdx.Gdx;
import ionium.registry.GlobalVariables;
import ionium.templates.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class VersionGetter {

	private static VersionGetter instance;

	private VersionGetter() {
	}

	public static VersionGetter instance() {
		if (instance == null) {
			instance = new VersionGetter();
			instance.loadResources();
		}
		return instance;
	}

	private void loadResources() {

	}

	/**
	 * NOTE: This method blocks until it fails or completes
	 */
	public void getVersionFromServer() {
		final String path = GlobalVariables.versionUrl;

		if (path == null) {
			Main.logger.warn("Version URL is null!");
			return;
		}

		long start = System.currentTimeMillis();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new URL(path).openStream()));

			final StringBuilder file = new StringBuilder();
			String inputline;
			while ((inputline = br.readLine()) != null)
				file.append(inputline);

			br.close();

			Main.logger.info("Finished getting version, took "
					+ (System.currentTimeMillis() - start) + " ms");

			Gdx.app.postRunnable(new Runnable() {

				@Override
				public void run() {
					Main.githubVersion = file.toString();
				}

			});

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			Main.logger.error("Failed to parse/get latest version info", e);
		}
	}
}
