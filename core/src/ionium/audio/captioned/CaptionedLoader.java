package ionium.audio.captioned;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class CaptionedLoader
		extends AsynchronousAssetLoader<Captioned, CaptionedLoader.CaptionParameter> {

	private Captioned captioned;

	public CaptionedLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file,
			CaptionParameter parameter) {
		captioned = (parameter.streamed ? new CaptionedMusic(file, parameter.caption)
				: new CaptionedSound(file, parameter.caption));
	}

	@Override
	public Captioned loadSync(AssetManager manager, String fileName, FileHandle file,
			CaptionParameter parameter) {
		Captioned captioned = this.captioned;
		this.captioned = null;
		return captioned;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file,
			CaptionParameter parameter) {
		return null;
	}

	public static class CaptionParameter extends AssetLoaderParameters<Captioned> {

		protected boolean streamed = false;
		protected String caption = "";

		public CaptionParameter(boolean streamed, String caption) {
			this.streamed = streamed;
			this.caption = caption;
		}

	}
}
