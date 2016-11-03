package ionium.audio.captioned;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class CaptionedSound implements Captioned, Disposable {

	private Sound sound;
	private String caption;

	public CaptionedSound(FileHandle handle, String caption) {
		sound = Gdx.audio.newSound(handle);
		this.caption = caption;
	}

	public Sound getSound(){
		return sound;
	}
	
	@Override
	public void dispose() {
		sound.dispose();
	}

	@Override
	public String getCaption() {
		return caption;
	}

}
