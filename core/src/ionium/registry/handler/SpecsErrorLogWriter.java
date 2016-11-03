package ionium.registry.handler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import ionium.templates.Main;
import ionium.util.MemoryUtils;

/**
 * Designed to add the system specs to the error log
 * 
 *
 */
public class SpecsErrorLogWriter implements IErrorLogWriter{

	@Override
	public void appendToStart(StringBuilder builder) {
		builder.append("Game Specifications:\n");
		builder.append("   Version: " + Main.version + "\n");
		builder.append("   Application type: " + Gdx.app.getType().toString() + "\n");
		
		builder.append("\n");
		
		builder.append("Operating System Specifications:\n");
		builder.append("   Java Version: " + System.getProperty("java.version") + " " + (System.getProperty("sun.arch.data.model")) + " bit" + "\n");
		builder.append("   OS Name: " + System.getProperty("os.name") + "\n");
		builder.append("   OS Version: " + System.getProperty("os.version") + "\n");
		builder.append("   JVM memory available: " + (MemoryUtils.getMaxMemory() / 1024) + " MB\n");
		
		builder.append("\n");
		
		builder.append("Processor Specifications:\n");
		builder.append("   Cores: " + MemoryUtils.getCores() + "\n");
		
		builder.append("\n");
		
		builder.append("Graphics Specifications:\n");
		builder.append("   Resolution: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight() + "\n");
		builder.append("   Fullscreen: " + Gdx.graphics.isFullscreen() + "\n");
		builder.append("   GL_VENDOR: " + Gdx.gl.glGetString(GL20.GL_VENDOR) + "\n");
		builder.append("   Graphics: " + Gdx.gl.glGetString(GL20.GL_RENDERER) + "\n");
		builder.append("   GL Version: " + Gdx.gl.glGetString(GL20.GL_VERSION) + "\n");
		
	}

	@Override
	public void appendToEnd(StringBuilder builder) {
	}

}
