package ionium.menutree;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import ionium.templates.Main;
import ionium.util.Utils;
import ionium.util.input.ActionType;

/**
 * The container class for the MenuElements in a screen.
 * 
 *
 */
public class MenuTree {

	private Array<MenuElement> recomputationQueue = new Array<>();
	private Array<MenuElement> elements = new Array<>();
	private int totalElementsCount = 0;

	private int selected = 0;

	private float x, y;
	private float indent = 32;

	private Main main;
	
	protected Color selectedColor = new Color(0.25f, 0.25f, 0.25f, 1f);

	public MenuTree(Main m, float x, float y, float indent) {
		main = m;
		this.x = x;
		this.y = y;
		this.indent = indent;
	}

	public void render(SpriteBatch batch, BitmapFont font, float alpha) {
		float offsetX = 0;
		float offsetY = 0;

		renderSublevel(batch, font, offsetX, offsetY, elements, selected, true, alpha);
	}
	
	public void renderUpdate(ActionType action){
		if (action == ActionType.DOWN) {
			pressDown();
		} else if (action == ActionType.UP) {
			pressUp();
		} else if (action == ActionType.LEFT) {
			pressLeft();
		} else if (action == ActionType.RIGHT) {
			pressRight();
		} else if (action == ActionType.ACCEPT) {
			pressEnter();
		} else if (action == ActionType.CANCEL) {
			pressBack();
		}
	}

	protected void pressEnter() {
		if (elements.get(selected).isEnabled()) elements.get(selected).handleEnter();
	}

	protected void pressBack() {
		pressLeft();
	}

	protected void pressUp() {
		increaseSelected(-1);
	}

	protected void pressDown() {
		increaseSelected(1);
	}

	protected void pressLeft() {
		if (elements.get(selected).onNextSublevel) {
			// tell to move left
			elements.get(selected).moveSublevel(false);
		}
	}

	protected void pressRight() {
		if (elements.get(selected).sublevel.size > 0) {
			elements.get(selected).moveSublevel(true);
		}
	}

	public void onScreenShow() {
		
	}

	public void onScreenHide() {
		
	}
	
	protected Color getSelectionColor(float alpha){
		selectedColor.a = alpha;
		
		return selectedColor;
	}

	private float renderSublevel(SpriteBatch batch, BitmapFont font, float offsetX, float offsetY,
			Array<MenuElement> level, int selected, boolean isThisGroupEvenSelected, float alpha) {
		boolean shouldBeGreyedOut = false;

		// grey out if group not selected
		if (!isThisGroupEvenSelected) {
			shouldBeGreyedOut = true;
		}

		// grey out if not already and any members of group are in their sublevel
		if (!shouldBeGreyedOut) {
			for (MenuElement m : level) {
				if (m.onNextSublevel) {
					shouldBeGreyedOut = true;
					break;
				}
			}
		}

		for (int i = 0; i < level.size; i++) {
			// draw text
			font.setColor(1, 1, 1, alpha);

			if (shouldBeGreyedOut || !level.get(i).isEnabled()) {
				font.setColor(0.25f, 0.25f, 0.25f, alpha);
			} else {
				if (i == selected) {
					font.setColor(getSelectionColor(alpha));
				}
			}

			font.draw(batch, level.get(i).getRenderText(), x * Gdx.graphics.getWidth() + offsetX, y
					* Gdx.graphics.getHeight() - offsetY);

			if (level.get(i).sublevel.size > 0) {
				main.defaultFont.setColor(0.25f, 0.25f, 0.25f, alpha);
				main.defaultFont.draw(
						batch,
						"   >",
						x * Gdx.graphics.getWidth() + offsetX
								+ Utils.getWidth(font, level.get(i).getRenderText()), y
								* Gdx.graphics.getHeight() - offsetY);
				main.defaultFont.setColor(1, 1, 1, alpha);
			}

			// show the selected arrow if the menu is:
			//    - selected, the group selected, and not on the next sublevel
			if (i == selected && isThisGroupEvenSelected && !level.get(i).onNextSublevel) {
				font.draw(batch, "> ",
						x * Gdx.graphics.getWidth() + offsetX - Utils.getWidth(font, "> "), y
								* Gdx.graphics.getHeight() - offsetY);
			}

			// increase Y offset
			offsetY += font.getCapHeight() * 2.5f;

			// find sublevels, recurse through them and render
			if (level.get(i).sublevel.size > 0 && i == selected && isThisGroupEvenSelected) {
				offsetY = renderSublevel(batch, font, offsetX + indent, offsetY,
						level.get(i).sublevel, level.get(i).getSelected(),
						level.get(i).onNextSublevel, alpha);
			}
		}

		return offsetY;
	}

	public MenuTree addElement(MenuElement me) {
		if (me == null) throw new IllegalArgumentException("MenuElement cannot be null!");

		elements.add(me);
		recomputeTotalElements();

		return this;
	}

	public int recomputeTotalElements() {
		// reset total elements number
		totalElementsCount = 0;

		// reset recomputation queue
		resetRecomputationQueue();

		// traverse recomputation queue, adding new sublevels as we go
		int index = 0;
		MenuElement element = null;
		while (recomputationQueue.size > 0 && index < recomputationQueue.size) {
			element = recomputationQueue.get(index);
			// add this element
			totalElementsCount++;

			index++;

			// traverse this element's sublevel
			if (element.sublevel.size <= 0) continue;
			for (MenuElement m : element.sublevel) {
				// add the sublevel elements into the recomputation
				recomputationQueue.add(m);
			}
		}

		return getAmountOfTotalElements();
	}

	/**
	 * Clears and refills the recomputation queue with the topmost elements
	 */
	private void resetRecomputationQueue() {
		recomputationQueue.clear();
		for (MenuElement m : elements) {
			recomputationQueue.add(m);
		}
	}

	public int getAmountOfTotalElements() {
		return totalElementsCount;
	}

	public int getSelected() {
		return selected;
	}

	public void increaseSelected(int amt) {
		if (!elements.get(selected).onNextSublevel) {
			selected = MathUtils.clamp(selected + amt, 0, elements.size - 1);
		} else {
			elements.get(selected).increaseSelected(amt);
		}
	}

}
