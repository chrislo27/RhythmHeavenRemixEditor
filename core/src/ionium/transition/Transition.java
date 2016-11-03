package ionium.transition;

import ionium.templates.Main;

public interface Transition {

	public boolean finished();

	public void render(Main main);

	public void tickUpdate(Main main);
}
