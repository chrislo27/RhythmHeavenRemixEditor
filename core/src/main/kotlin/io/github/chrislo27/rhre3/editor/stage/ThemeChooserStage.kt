package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class ThemeChooserStage(val editor: Editor, val palette: UIPalette, parent: EditorStage, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    private val buttons: List<ThemeChangeButton> = mutableListOf()
    private val preferences: Preferences
        get() = editor.main.preferences

    private var buttonScroll = 0
    private val maxButtonScroll: Int
        get() = (LoadedThemes.themes.size - buttons.size).coerceAtLeast(0)

    init {
        buttons as MutableList

        this.elements += object : ColourPane<EditorScreen>(this, this) {
            override fun scrolled(amount: Int): Boolean {
                scroll(-amount)
                return true
            }
        }.apply {
            this.colour.set(Editor.TRANSLUCENT_BLACK)
            this.colour.a = 0.8f
        }

        this.elements += TextLabel(palette, this, this).apply {
            this.location.set(screenX = 0f, screenWidth = 1f, screenY = 0.875f, screenHeight = 0.125f)

            this.textAlign = Align.center
            this.textWrapping = false
            this.isLocalizationKey = true
            this.text = "editor.themeChooser.title"
        }

        this.elements += object : Button<EditorScreen>(palette, this, this) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                LoadedThemes.reloadPalettes(preferences, false)
                LoadedThemes.persistIndex(preferences)
                editor.theme = LoadedThemes.currentTheme
                buttonScroll = 0

                resetButtons()
            }
        }.apply {
            this.location.set(0.05f, 0.025f, 0.9f, 0.075f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "editor.themeChooser.reset"
                this.textWrapping = true
                this.fontScaleMultiplier = 0.75f
            })
        }

        val up = object : Button<EditorScreen>(palette, this, this) {
            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                (labels.first() as TextLabel).text = Editor.ARROWS[if (buttonScroll <= 0) 2 else 0]
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                scroll(-1)
            }
        }.apply {
            this.location.set(0.05f, 0.825f, 0.9f, 0.05f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = true
            })
        }
        this.elements += up
        val down = object : Button<EditorScreen>(palette, this, this) {
            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                (labels.first() as TextLabel).text = Editor.ARROWS[if (buttonScroll >= maxButtonScroll) 3 else 1]
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                scroll(1)
            }
        }.apply {
            this.location.set(0.05f, 0.1125f, 0.9f, 0.05f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = true
            })
        }
        this.elements += down

        val padding = 0.0125f
        val start = down.location.screenY + down.location.screenHeight + padding
        val end = up.location.screenY
        val area = end - start
        val numberOfButtons = 9
        val buttonHeight = area / numberOfButtons
        for (i in 0 until numberOfButtons) {
            buttons += ThemeChangeButton(i, palette, this, this).apply {
                this.location.set(screenX = 0.05f,
                                  screenY = end - (buttonHeight) * (i + 1),
                                  screenWidth = 0.9f,
                                  screenHeight = buttonHeight - padding)
            }
        }

        this.elements.addAll(buttons)
    }

    fun resetButtons() {
        buttons.forEach(ThemeChangeButton::update)
    }

    private fun scroll(amount: Int) {
        if (amount < 0 && buttonScroll > 0) {
            buttonScroll--
        } else if (amount > 0 && buttonScroll < maxButtonScroll) {
            buttonScroll++
        }

        resetButtons()
    }

    override fun scrolled(amount: Int): Boolean {
        scroll(amount)

        return true
    }

    inner class ThemeChangeButton(val index: Int, palette: UIPalette, parent: UIElement<EditorScreen>,
                                  stage: Stage<EditorScreen>)
        : Button<EditorScreen>(palette, parent, stage) {

        private val textLabel: TextLabel<EditorScreen> = TextLabel(palette, this, stage)
        private val colourIcon: Label<EditorScreen> = object : Label<EditorScreen>(palette, this, stage) {
            override var background: Boolean = false

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (index + buttonScroll >= LoadedThemes.themes.size)
                    return

                val theme = LoadedThemes.themes[index + buttonScroll]
                val oldBatchColor = batch.packedColor

                batch.color = theme.background
                batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
                batch.color = theme.trackLine
                val lines = 4
                for (i in 0 until lines) {
                    batch.fillRect(location.realX,
                                   location.realY + location.realHeight * 1f / 3f
                                           + (location.realHeight / 3f * (i.toFloat() / (lines))),
                                   location.realWidth, 1f)
                }

                batch.setColor(oldBatchColor)
            }
        }

        init {
            val part = 1 / 6f

            textLabel.apply {
                this.location.set(screenX = part)
                this.location.set(screenWidth = 1f - this.location.screenX)
                this.textWrapping = false
                this.textAlign = Align.left
                this.fontScaleMultiplier = 0.7f
            }

            colourIcon.apply {
                val padding = 1f / 8f
                this.location.set(screenX = padding * part, screenWidth = part - padding * part * 2,
                                  screenY = padding, screenHeight = 1f - padding * 2)
            }

            addLabel(textLabel)
            addLabel(colourIcon)
        }

        fun update() {
            if (index + buttonScroll >= LoadedThemes.themes.size)
                return

            val theme = LoadedThemes.themes[index + buttonScroll]
            textLabel.text = theme.name
            textLabel.isLocalizationKey = theme.nameIsLocalization
            textLabel.textColor = if (index + buttonScroll == LoadedThemes.index) Editor.SELECTED_TINT else null
        }

        override fun frameUpdate(screen: EditorScreen) {
            this.visible = index + buttonScroll < LoadedThemes.themes.size

            super.frameUpdate(screen)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)

            LoadedThemes.index = index + buttonScroll
            LoadedThemes.persistIndex(preferences)
            editor.theme = LoadedThemes.currentTheme

            resetButtons()
        }
    }

}