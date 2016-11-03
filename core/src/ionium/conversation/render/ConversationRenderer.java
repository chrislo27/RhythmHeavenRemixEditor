package ionium.conversation.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import ionium.conversation.Conversation;
import ionium.conversation.Conversation.Choice;
import ionium.conversation.DialogueLine;
import ionium.conversation.Voice;
import ionium.registry.AssetRegistry;
import ionium.templates.Main;
import ionium.util.i18n.Localization;
import ionium.util.input.ActionType;

public abstract class ConversationRenderer {

	protected Conversation currentConv;
	protected int convStage = 0;
	protected int convScroll = 0;

	protected int selectionIndex = 0;

	private float renderTime = -1;

	private GlyphLayout layout = new GlyphLayout();
	private float textHeight = 0;
	private float choicesHeight = 0;
	private boolean alreadySetLayout = false;

	protected Color reusedColor = new Color();

	public ConversationRenderer() {

	}

	public void render(SpriteBatch batch, BitmapFont font, ConvStyle style, ConvSide side,
			int scrollSpeed) {
		render(batch, font, font, style, side, scrollSpeed);
	}

	public void render(SpriteBatch batch, BitmapFont font, BitmapFont questioningFont,
			ConvStyle style, ConvSide side, int scrollSpeed) {
		if (currentConv == null) return;

		if (renderTime <= -1) {
			renderTime = getCurrent().character.voice.avgLength;
		}

		float textStartX = Gdx.graphics.getWidth() * style.textPaddingX;
		float offsetY = 0;
		float textWidth = Gdx.graphics.getWidth()
				- (Gdx.graphics.getWidth() * style.textPaddingX * 2);

		if (style.shouldFaceBeShown && getCurrent().character.face != null) {
			float faceWidth = AssetRegistry.getTexture(getCurrent().character.face).getWidth();

			if (style.shouldFaceBeRightAligned) {

			} else {
				textStartX += faceWidth;
				textStartX += Gdx.graphics.getWidth() * style.textPaddingX;
			}

			textWidth = Gdx.graphics.getWidth() - (textStartX)
					- (Gdx.graphics.getWidth() * style.textPaddingX);
		}

		float bgHeight = Gdx.graphics.getHeight() * style.percentageOfScreenToOccupy;

		if (!alreadySetLayout) {

			layout.setText(font, getActualMessage(), font.getColor(), textWidth, Align.topLeft,
					true);

			textHeight = layout.height;
			choicesHeight = 0;

			if (currentConv.choices != null) {
				if (currentConv.choices.length > 0) {
					String totalQuestionString = "";

					for (int i = 0; i < currentConv.choices.length; i++) {
						Choice c = currentConv.choices[i];

						totalQuestionString += Localization.get(c.question);

						if (i + 1 < currentConv.choices.length) {
							totalQuestionString += "\n";
						}
					}

					layout.setText(questioningFont, totalQuestionString, questioningFont.getColor(),
							textWidth, Align.topLeft, true);

					choicesHeight = layout.height;
				}
			}

			alreadySetLayout = true;
		}

		if (textHeight + (Gdx.graphics.getHeight() * style.textPaddingY * 2) + choicesHeight
				+ questioningFont.getLineHeight() > bgHeight) {
			bgHeight = textHeight + (Gdx.graphics.getHeight() * style.textPaddingY * 2)
					+ choicesHeight + questioningFont.getLineHeight();
		}

		if (side == ConvSide.TOP) {
			offsetY = Gdx.graphics.getHeight() - bgHeight;
		}

		batch.setColor(0, 0, 0, 0.5f);
		Main.fillRect(batch, 0, offsetY, Gdx.graphics.getWidth(), bgHeight);
		batch.setColor(1, 1, 1, 1);

		if (style.shouldFaceBeShown && getCurrent().character.face != null) {

			Texture tex = AssetRegistry.getTexture(getCurrent().character.face);
			float faceX = Gdx.graphics.getWidth() * style.textPaddingX;
			float faceY = (bgHeight * 0.5f) - (tex.getHeight() / 2) + offsetY;

			if (style.shouldFaceBeRightAligned) {
				faceX += textWidth;
			}

			batch.draw(tex, faceX, faceY);

			if (style.shouldRenderNametag) {
				font.setColor(1, 1, 1, 1);

				font.draw(batch,
						Localization.get(ionium.conversation.Character.LocalizedNamePrefix
								+ getCurrent().character.name),
						faceX + (tex.getWidth() * 0.5f), faceY - font.getCapHeight(),
						tex.getWidth(), Align.center, false);
			}
		}

		font.setColor(1, 1, 1, 1);
		font.draw(batch, getActualMessage(), textStartX,
				bgHeight - (Gdx.graphics.getHeight() * style.textPaddingY) + offsetY, 0,
				getSubstringLength(), textWidth, Align.topLeft, true);

		if (isFinishedScrolling() && choicesHeight > 0
				&& convStage == currentConv.lines.length - 1) {
			// render options if any

			for (int i = 0; i < currentConv.choices.length; i++) {
				Choice c = currentConv.choices[i];

				questioningFont.setColor(1, 1, 1, 1);

				if (i == selectionIndex) {
					questioningFont.setColor(getSelectionColour(reusedColor));
				}

				questioningFont.draw(batch, "   > " + Localization.get(c.question), textStartX,
						(bgHeight - (Gdx.graphics.getHeight() * style.textPaddingY)) - textHeight
								- questioningFont.getLineHeight() + offsetY
								- (i * questioningFont.getLineHeight()),
						textWidth, Align.topLeft, true);

				questioningFont.setColor(1, 1, 1, 1);
			}
		}

		// voice
		if (style.shouldPlayMumbling && convScroll < getActualMessage().length()) {
			Voice voice = getCurrent().character.voice;
			Sound sound = AssetRegistry.getSound(voice.voiceFile);

			if (voice != null && sound != null) {
				if (renderTime >= voice.avgLength) {
					renderTime -= voice.avgLength;

					float pitchOffset = MathUtils.random(0, style.mumblingPitchOffset)
							* MathUtils.randomSign();
					if (pitchOffset < 0) {
						// because it ranges from 0.5 to 2.0, the high end is x2, the low end is x0.5
						pitchOffset *= 0.5f;
					}

					sound.play(1f, 1f + pitchOffset, 0);
				}
			}
		}

		// scrolling
		convScroll = MathUtils.clamp(convScroll + scrollSpeed, 0, getActualMessage().length());
		renderTime += Gdx.graphics.getDeltaTime();
	}

	public void inputUpdate(ActionType action, boolean justPressed) {
		if (currentConv == null) return;

		if (isFinishedScrolling() && choicesHeight > 0 && convStage == currentConv.lines.length - 1
				&& justPressed) {
			if (action == ActionType.UP) {
				selectionIndex--;

				if (selectionIndex < 0) {
					selectionIndex = currentConv.choices.length - 1;
				}
			} else if (action == ActionType.DOWN) {
				selectionIndex++;

				if (selectionIndex >= currentConv.choices.length) {
					selectionIndex = 0;
				}
			}
		}

		if ((action == ActionType.ACCEPT || action == ActionType.CANCEL) && justPressed) {
			if (isFinishedScrolling()) {
				if (action == ActionType.CANCEL && (currentConv.cancelChoice >= 0
						&& currentConv.cancelChoice < currentConv.choices.length)) {
					selectionIndex = currentConv.cancelChoice;
				}

				advanceStage();
			} else {
				finishScrolling();
			}
		}
	}

	public abstract Conversation getConversationFromId(String id);

	public abstract Color getSelectionColour(Color c);

	/**
	 * Accounts for colour tags like [RED]this is in red[]
	 * <br>
	 * Basically checks if the current scroll is inside a bracket set, and moves it
	 * forward to the nearest outside-of-bracket index.
	 * @return
	 */
	protected int getSubstringLength() {

		if (convScroll == getActualMessage().length()) {
			return convScroll;
		}

		// start checking backwards, looking for the [
		for (int i = convScroll - 1; i >= 0; i--) {
			// if a ] was found instead, it means we're not in a bracket set
			if (getActualMessage().charAt(i) == ']') {
				break;
			}

			// we found the [
			if (getActualMessage().charAt(i) == '[') {
				// if it was escaped with \, keep looking
				if (i > 0) {
					if (getActualMessage().charAt(i - 1) == '\\') {
						continue;
					}
				}

				// otherwise, we've found it (it can't be at index 0 since we check before that)
				// now we go forwards starting at convScroll looking for the ], and return at the ]
				for (int j = convScroll; j < getActualMessage().length(); j++) {
					if (getActualMessage().charAt(j) == ']') {
						return Math.min(j + 1, getActualMessage().length());
					}
				}

				break;
			}
		}

		return convScroll;
	}

	protected DialogueLine getCurrent() {
		if (currentConv == null) return null;

		return currentConv.lines[convStage];
	}

	public boolean isInConv() {
		return currentConv != null;
	}

	public String getActualMessage() {
		if (currentConv == null) return "";

		return Localization.get(currentConv.lines[convStage].line);
	}

	public boolean isFinishedScrolling() {
		if (currentConv == null) return true;

		return convScroll == getActualMessage().length();
	}

	public void finishScrolling() {
		if (currentConv == null) return;

		convScroll = getActualMessage().length();
	}

	public void advanceStage() {
		if (currentConv == null) return;

		convScroll = 0;
		convStage += 1;
		renderTime = -1;
		alreadySetLayout = false;

		if (convStage >= currentConv.lines.length) {
			Conversation current = currentConv;

			if (currentConv.choices != null) {
				if (currentConv.choices.length > 0) {
					setToConv(getConversationFromId(currentConv.choices[selectionIndex].gotoNext));
				}
			} else {
				setToConv(getConversationFromId(currentConv.gotoNext));
			}

			onConvFinish(current);
		}

		selectionIndex = 0;
	}

	public void onConvFinish(Conversation conv) {

	}

	public void onConvStart(Conversation conv) {

	}

	public ConversationRenderer setToConv(Conversation conv) {
		currentConv = conv;
		convStage = -1;
		advanceStage();

		if (conv != null) {
			onConvStart(conv);
		}

		return this;
	}

}
