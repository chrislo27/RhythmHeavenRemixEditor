package ionium.audio.captioned;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class CaptionedMusic implements Captioned, Disposable {

	private Music music;
	private String caption;

	public CaptionedMusic(FileHandle handle, String caption) {
		music = Gdx.audio.newMusic(handle);
		this.caption = caption;
	}

	public Music getMusic(){
		return music;
	}
	
	@Override
	public void dispose() {
		music.dispose();
	}

	@Override
	public String getCaption() {
		return caption;
	}

}
