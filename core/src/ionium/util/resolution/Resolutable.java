package ionium.util.resolution;


public interface Resolutable {

	public void setWidth(int w);
	public void setHeight(int h);
	public void setFullscreen(boolean fs);
	
	public int getWidth();
	public int getHeight();
	public boolean isFullscreen();
	
}
