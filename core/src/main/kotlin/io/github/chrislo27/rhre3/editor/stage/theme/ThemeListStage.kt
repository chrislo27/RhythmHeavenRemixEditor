package io.github.chrislo27.rhre3.editor.stage.theme

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import kotlin.math.roundToInt


abstract class ThemeListStage<T>(val editor: Editor, val palette: UIPalette, parent: Stage<EditorScreen>, camera: OrthographicCamera,
                                 pixelsWidth: Float, pixelsHeight: Float)
    : Stage<EditorScreen>(parent, camera, pixelsWidth, pixelsHeight) {

    abstract val itemList: List<T>
    private val buttons: MutableList<ItemButton> = mutableListOf()
    var buttonScroll: Int = 0
        set(value) {
            field = value.coerceIn(0, maxButtonScroll)
        }
    private val maxButtonScroll: Int
        get() = (itemList.size - buttons.size).coerceAtLeast(0)

    init {

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
            this.location.set(0f, 1f, 0f, 0f, 0f, -23f, 346f, 23f)
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
            this.location.set(0f, 0f, 0f, 0f, 0f, 0f, 346f, 23f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = true
            })
        }
        this.elements += down

        val padding = 3f
        val start = 28f
        val end = 330f
        val area = end - start
        val numberOfButtons = 9
        val buttonHeight = area / numberOfButtons
        for (i in 0 until numberOfButtons) {
            buttons += ItemButton(i, palette, this, this).apply {
                this.location.set(0f, 0f, 0f, 0f, 0f, end - buttonHeight * (i + 1) - 1f, 346f, buttonHeight - padding)
            }
        }

        this.elements.addAll(buttons)
        // Scrollbar
        this.elements += object : UIElement<EditorScreen>(this, this) {
            private var clickPoint: Pair<Float, Int> = -1f to 1

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                val oldColour = batch.packedColor

                batch.packedColor = palette.backColor.toFloatBits()
                batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)

                if (canBeClickedOn()) {
                    // Render window
                    val items = itemList
                    val total = items.size
                    val barHeight = (buttons.size.toFloat() / total.coerceAtLeast(1)) * location.realHeight
                    val barHeightAlpha = barHeight / location.realHeight
                    val click = clickPoint

                    batch.color = if (click.first in 0f..1f) palette.clickedBackColor else palette.highlightedBackColor
                    batch.fillRect(location.realX, location.realY + location.realHeight - buttonScroll.toFloat() / total * location.realHeight, location.realWidth, -barHeight)

                    val blueBarLimit = getBlueBarLimit()
                    if (blueBarLimit > 0) {
                        if (total > blueBarLimit) {
                            // Has custom, add blue bar
                            batch.setColor(0f, 1f, 1f, 0.5f)
                            val smallBarHeight = location.realHeight * (1f / total)
                            batch.fillRect(location.realX, location.realY + (1f - blueBarLimit.toFloat() / total) * location.realHeight, location.realWidth, -smallBarHeight)
                        }
                    }

                    if (click.first in 0f..1f) {
                        val currentAlpha = 1f - (this.stage.camera.getInputY() - location.realY) / location.realHeight
                        val buttonScrollAlpha = click.second.toFloat() / total
                        val alpha = ((currentAlpha - (click.first - buttonScrollAlpha)) / (1f - barHeightAlpha)).coerceIn(0f, 1f)
                        val targetScroll = (alpha * maxButtonScroll).roundToInt().coerceIn(0, maxButtonScroll)
                        if (buttonScroll != targetScroll) {
                            buttonScroll = targetScroll
                            resetButtons()
                        }
                    }
                }

                batch.packedColor = oldColour
            }

            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (canBeClickedOn() && button == Input.Buttons.LEFT && isMouseOver()) {
                    val scrollAlpha = 1f - (this.stage.camera.getInputY() - location.realY) / location.realHeight
                    val total = itemList.size
                    val alphaInBar = (scrollAlpha - (buttonScroll.toFloat() / total)) / (buttons.size.toFloat() / total.coerceAtLeast(1))
                    if (alphaInBar in 0f..1f) {
                        clickPoint = scrollAlpha to buttonScroll
                    }
                }
                return super.touchDown(screenX, screenY, pointer, button)
            }

            override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (canBeClickedOn() && button == Input.Buttons.LEFT) {
                    clickPoint = -1f to 1
                }
                return super.touchDown(screenX, screenY, pointer, button)
            }

            override fun canBeClickedOn(): Boolean = maxButtonScroll > 0
        }.apply {
            this.location.set(1f, 0f, 0f, 0f, -13f, 27f, 13f, 299f)
        }
    }

    abstract fun getItemName(item: T): String
    abstract fun isItemNameLocalizationKey(item: T): Boolean
    abstract fun getItemBgColor(item: T): Color
    abstract fun getItemLineColor(item: T): Color
    open fun getBlueBarLimit(): Int = -1

    abstract fun onItemButtonSelected(leftClick: Boolean, realIndex: Int, buttonIndex: Int)

    open fun resetButtons() {
        buttons.forEach { it.update() }
    }

    fun scroll(amount: Int) {
        if (amount < 0 && buttonScroll > 0) {
            buttonScroll--
        } else if (amount > 0 && buttonScroll < maxButtonScroll) {
            buttonScroll++
        }

        resetButtons()
    }

    inner class ItemButton(val index: Int, palette: UIPalette, parent: UIElement<EditorScreen>,
                           stage: Stage<EditorScreen>)
        : Button<EditorScreen>(palette, parent, stage) {

        private val textLabel: TextLabel<EditorScreen> = TextLabel(palette, this, stage)
        private val colourIcon: Label<EditorScreen> = object : Label<EditorScreen>(palette, this, stage) {
            override var background: Boolean = false

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (index + buttonScroll >= itemList.size)
                    return

                val item = itemList[index + buttonScroll]
                val oldBatchColor = batch.packedColor

                batch.color = getItemBgColor(item)
                batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)
                batch.color = getItemLineColor(item)
                val lines = 4
                for (i in 0 until lines) {
                    batch.fillRect(location.realX,
                                   location.realY + location.realHeight * 1f / 3f
                                           + (location.realHeight / 3f * (i.toFloat() / (lines))),
                                   location.realWidth, 1f)
                }

                batch.packedColor = oldBatchColor
            }
        }

        init {
            val part = 1 / 6f

            textLabel.apply {
                this.location.set(screenX = part)
                this.location.set(screenWidth = 1f - this.location.screenX - 0.005f)
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
            if (index + buttonScroll >= itemList.size)
                return

            val item = itemList[index + buttonScroll]
            textLabel.text = getItemName(item)
            textLabel.isLocalizationKey = isItemNameLocalizationKey(item)
            textLabel.textColor = if (index + buttonScroll == LoadedThemes.index) Editor.SELECTED_TINT else null
        }

        override fun frameUpdate(screen: EditorScreen) {
            this.visible = index + buttonScroll < LoadedThemes.themes.size

            super.frameUpdate(screen)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)

            onItemButtonSelected(true, index + buttonScroll, index)
            resetButtons()
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)

            onItemButtonSelected(false, index + buttonScroll, index)
            resetButtons()
        }
    }
}