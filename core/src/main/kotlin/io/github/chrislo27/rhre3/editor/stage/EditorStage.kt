package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.registry.datamodel.Cue
import io.github.chrislo27.rhre3.registry.datamodel.Game
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import java.util.*


class EditorStage(parent: UIElement<EditorScreen>?,
                  camera: OrthographicCamera, val main: RHRE3Application, val editor: Editor)
    : Stage<EditorScreen>(parent, camera), Palettable {

    override var palette: UIPalette = main.uiPalette.copy(
            backColor = Color(main.uiPalette.backColor).apply { this.a = 0.5f })
    val messageBarStage: Stage<EditorScreen>
    val buttonBarStage: Stage<EditorScreen>
    val pickerStage: Stage<EditorScreen>
    val minimapBarStage: Stage<EditorScreen>
    val centreAreaStage: Stage<EditorScreen>
    val patternAreaStage: Stage<EditorScreen>

    val gameButtons: List<GameButton>
    val variantButtons: List<GameButton>
    val seriesButtons: List<SeriesButton>
    val patternLabels: List<TextLabel<EditorScreen>>
    val gameScrollButtons: List<Button<EditorScreen>>
    val variantScrollButtons: List<Button<EditorScreen>>

    val selectorRegion: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("ui_selector_fever")) }
    val selectorRegionSeries: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("ui_selector_tengoku")) }
    val searchBar: TextField<EditorScreen>
    val messageLabel: TextLabel<EditorScreen>

    val hoverTextLabel: TextLabel<EditorScreen>

    val topOfMinimapBar: Float
        get() {
            return centreAreaStage.location.realY
        }
    val isTyping: Boolean
        get() {
            return searchBar.hasFocus
        }

    private var isDirty = DirtyType.CLEAN

    enum class DirtyType {
        CLEAN, DIRTY, SEARCH_DIRTY
    }

    private fun setHoverText(text: String) {
        hoverTextLabel.visible = true
        hoverTextLabel.text = text
        val font = hoverTextLabel.getFont()
        font.data.setScale(hoverTextLabel.palette.fontScale * hoverTextLabel.fontScaleMultiplier)
        hoverTextLabel.location.set(pixelX = camera.getInputX(), pixelY = camera.getInputY() + 2,
                                    pixelWidth = font.getTextWidth(hoverTextLabel.text) + 6,
                                    pixelHeight = font.lineHeight)
        font.data.setScale(1f)
        hoverTextLabel.onResize(hoverTextLabel.parent!!.location.realWidth, hoverTextLabel.parent!!.location.realHeight)
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        hoverTextLabel.visible = false
        pickerStage.elements.filterIsInstance<GameButton>().firstOrNull {
            if (it.isMouseOver() && it.game != null) {
                setHoverText(it.game!!.name)
                return@firstOrNull true
            }

            false
        } ?: minimapBarStage.elements.filterIsInstance<SeriesButton>().firstOrNull {
            if (it.isMouseOver()) {
                setHoverText(Localization[it.series.localization])
                return@firstOrNull true
            }

            false
        }

        super.render(screen, batch, shapeRenderer)

        if (isDirty != DirtyType.CLEAN && !GameRegistry.isDataLoading()) {
            val selection = editor.pickerSelection.currentSelection
            val series = editor.pickerSelection.currentSeries
            val isSearching = editor.pickerSelection.isSearching

            messageLabel.text = ""

            selection.groups.clear()
            if (isSearching) {
                if (isDirty == DirtyType.SEARCH_DIRTY) {
                    selection.variants.clear()
                    selection.group = 0
                    val query = searchBar.text.toLowerCase(Locale.ROOT)
                    GameRegistry.data.gameGroupsList
                            .filter {
                                query in it.name.toLowerCase(Locale.ROOT) ||
                                        it.games.any { query in it.name.toLowerCase(Locale.ROOT) }
                            }.mapTo(selection.groups) { it }
                }
            } else {
                GameRegistry.data.gameGroupsList
                        .filter { it.series == series }
                        .mapTo(selection.groups) { it }
            }

            seriesButtons.forEach {
                it.selected = series == it.series && !isSearching
            }

            gameButtons.forEach {
                it.game = null
            }
            selection.groups
                    .forEachIndexed { index, it ->
                        val x: Int = index % Editor.ICON_COUNT_X
                        val y: Int = index / Editor.ICON_COUNT_X - selection.groupScroll

                        if (y in 0 until Editor.ICON_COUNT_Y) {
                            val buttonIndex = y * Editor.ICON_COUNT_X + x
                            gameButtons[buttonIndex].apply {
                                this.game = it.games[selection.getVariant(index).variant]
                                if (selection.group == index) {
                                    this.selected = true
                                }
                            }
                        }
                    }

            variantButtons.forEach {
                it.game = null
            }
            selection.groups.getOrNull(selection.group)?.also { group ->
                group.games.forEachIndexed { index, game ->
                    val y = index - selection.getCurrentVariant().variantScroll
                    if (y in 0 until Editor.ICON_COUNT_Y) {
                        variantButtons[y].apply {
                            this.game = game
                            if (selection.getCurrentVariant().variant == index) {
                                this.selected = true
                                messageLabel.text = game.name
                            }
                        }
                    }
                }
            }

            patternLabels.forEach {
                it.text = ""
                it.textColor = null
            }
            if (selection.groups.isNotEmpty() && selection.getCurrentVariant().placeableObjects.isNotEmpty()) {
                val variant = selection.getCurrentVariant()
                val objects = variant.placeableObjects

                objects.forEachIndexed { index, datamodel ->
                    val y = 2 + (index - variant.pattern)
                    if (y in 0 until Editor.PATTERN_COUNT) {
                        patternLabels[y].text = datamodel.name
                        if (y != (Editor.PATTERN_COUNT / 2) && datamodel is Cue) {
                            patternLabels[y].textColor = Editor.CUE_PATTERN_COLOR
                        }
                    }
                }
            }

            isDirty = DirtyType.CLEAN
        }
    }

    fun updateSelected(type: DirtyType = DirtyType.DIRTY) {
        isDirty = type
    }

    init {
        gameButtons = mutableListOf()
        variantButtons = mutableListOf()
        seriesButtons = mutableListOf()
        patternLabels = mutableListOf()
        gameScrollButtons = mutableListOf()
        variantScrollButtons = mutableListOf()

        messageBarStage = Stage(this, camera).apply {
            this.location.set(0f, 0f,
                              1f, Editor.MESSAGE_BAR_HEIGHT / RHRE3.HEIGHT.toFloat())

            this.updatePositions()
            this.elements +=
                    ColourPane(this, this).apply {
                        this.colour.set(Editor.TRANSLUCENT_BLACK)
                        this.colour.a = 0.75f
                    }
        }
        messageLabel = object : TextLabel<EditorScreen>(palette, messageBarStage, messageBarStage) {
            private var lastVersionTextWidth: Float = -1f

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
                if (main.versionTextWidth != lastVersionTextWidth) {
                    lastVersionTextWidth = main.versionTextWidth
                    this.location.set(
                            screenWidth = 1f - (main.versionTextWidth / messageBarStage.location.realWidth))
                    this.stage.updatePositions()
                }
            }
        }.apply {
            this.fontScaleMultiplier = 0.5f
            this.textAlign = Align.bottomLeft
            this.textWrapping = false
            this.location.set(0f, -0.5f,
                              1f,
                              1.5f,
                              pixelWidth = -8f)
            this.isLocalizationKey = false
        }
        messageBarStage.elements += messageLabel
        elements += messageBarStage
        buttonBarStage = Stage(this, camera).apply {
            this.location.set(screenX = (Editor.BUTTON_PADDING / RHRE3.WIDTH),
                              screenY = 1f - ((Editor.BUTTON_PADDING + Editor.BUTTON_SIZE) / RHRE3.HEIGHT),
                              screenWidth = 1f - (Editor.BUTTON_PADDING / RHRE3.WIDTH) * 2f,
                              screenHeight = Editor.BUTTON_SIZE / RHRE3.HEIGHT)
        }
        elements += buttonBarStage
        pickerStage = object : Stage<EditorScreen>(this, camera) {
            override fun scrolled(amount: Int): Boolean {
                if (isMouseOver()) {
                    val selection = editor.pickerSelection.currentSelection
                    when (stage.camera.getInputX()) {
                        in (location.realX + location.realWidth * 0.5f)..(location.realX + location.realWidth) -> {
                            val old = selection.getCurrentVariant().pattern
                            selection.getCurrentVariant().pattern =
                                    (selection.getCurrentVariant().pattern + amount)
                                            .coerceIn(0, selection.getCurrentVariant().maxPatternScroll)
                            if (old != selection.getCurrentVariant().pattern) {
                                updateSelected()
                                return true
                            }
                        }
                        in (variantButtons.first().location.realX)..(location.realX + location.realWidth * 0.5f) -> {
                            val old = selection.getCurrentVariant().variantScroll
                            selection.getCurrentVariant().variantScroll =
                                    (selection.getCurrentVariant().variantScroll + amount)
                                            .coerceIn(0, selection.getCurrentVariant().maxScroll)
                            if (old != selection.getCurrentVariant().variantScroll) {
                                updateSelected()
                                return true
                            }
                        }
                        in (location.realX)..(variantButtons.first().location.realX) -> {
                            val old = selection.groupScroll
                            selection.groupScroll =
                                    (selection.groupScroll + amount)
                                            .coerceIn(0, selection.maxGroupScroll)
                            if (old != selection.groupScroll) {
                                updateSelected()
                                return true
                            }
                        }
                    }
                }

                return false
            }
        }.apply {
            this.location.set(screenY = messageBarStage.location.screenY + messageBarStage.location.screenHeight,
                              screenHeight = ((Editor.ICON_SIZE + Editor.ICON_PADDING) * Editor.ICON_COUNT_Y + Editor.ICON_PADDING) / RHRE3.HEIGHT
                             )
            this.elements += ColourPane(this, this).apply {
                this.colour.set(Editor.TRANSLUCENT_BLACK)
            }
            this.elements += ColourPane(this, this).apply {
                this.colour.set(1f, 1f, 1f, 1f)
                this.location.set(screenX = 0.5f, screenWidth = 0f, screenHeight = 1f, pixelX = 1f, pixelWidth = 1f)
            }
        }
        elements += pickerStage
        patternAreaStage = Stage(this, camera).apply {
            this.location.set(screenY = pickerStage.location.screenY,
                              screenHeight = pickerStage.location.screenHeight,
                              screenX = 0.5f,
                              screenWidth = 0.5f)
        }
        elements += patternAreaStage
        minimapBarStage = Stage(this, camera).apply {
            this.location.set(screenY = pickerStage.location.screenY + pickerStage.location.screenHeight,
                              screenHeight = Editor.ICON_SIZE / RHRE3.HEIGHT)
            this.elements += ColourPane(this, this).apply {
                this.colour.set(Editor.TRANSLUCENT_BLACK)
            }
            this.elements += ColourPane(this, this).apply {
                this.colour.set(1f, 1f, 1f, 1f)
                this.location.set(screenX = 0f, screenWidth = 1f, screenHeight = 0f, pixelY = -1f, pixelHeight = 1f)
            }
        }
        elements += minimapBarStage
        centreAreaStage = Stage(this, camera).apply {
            this.location.set(screenY = minimapBarStage.location.screenY + minimapBarStage.location.screenHeight)
            this.location.set(
                    screenHeight = (buttonBarStage.location.screenY - this.location.screenY - (Editor.BUTTON_PADDING / RHRE3.HEIGHT)))
        }
        elements += centreAreaStage
        hoverTextLabel = TextLabel(palette, this, this).apply {
            this.background = true
            this.isLocalizationKey = false
            this.fontScaleMultiplier = 0.75f
            this.textWrapping = false
            this.visible = false
            this.alignment = Align.bottomLeft
            this.textAlign = Align.center
            this.location.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }
        elements += hoverTextLabel
        this.updatePositions()

        // Message bar
        run messageBar@ {

        }

        run pickerAndCo@ {
            val iconWidth = pickerStage.percentageOfWidth(Editor.ICON_SIZE)
            val iconHeight = pickerStage.percentageOfHeight(Editor.ICON_SIZE)
            val iconWidthPadded = pickerStage.percentageOfWidth(
                    Editor.ICON_SIZE + Editor.ICON_PADDING)
            val iconHeightPadded = pickerStage.percentageOfHeight(
                    Editor.ICON_SIZE + Editor.ICON_PADDING)
            val startX = pickerStage.percentageOfWidth(
                    (pickerStage.location.realWidth / 2f) -
                            ((Editor.ICON_SIZE + Editor.ICON_PADDING) * (Editor.ICON_COUNT_X + 3)
                                    - Editor.ICON_PADDING)
                                                      ) / 2f
            val startY = 1f - (pickerStage.percentageOfHeight(
                    (Editor.ICON_SIZE + Editor.ICON_PADDING) * (Editor.ICON_COUNT_Y - 2) / 2f
                                                             ))

            // Picker area
            run picker@ {
                pickerStage.updatePositions()
                gameButtons as MutableList
                variantButtons as MutableList

                fun UIElement<*>.setLocation(x: Int, y: Int) {
                    this.location.set(
                            screenX = startX + iconWidthPadded * x,
                            screenY = startY - iconHeightPadded * y,
                            screenWidth = iconWidth,
                            screenHeight = iconHeight
                                     )
                }


                for (y in 0 until Editor.ICON_COUNT_Y) {
                    for (x in 0 until Editor.ICON_COUNT_X + 3) {
                        if (x == Editor.ICON_COUNT_X || x == Editor.ICON_COUNT_X + 2) {
                            if (y != 0 && y != Editor.ICON_COUNT_Y - 1)
                                continue
                            val isUp: Boolean = y == 0
                            val isVariant: Boolean = x == Editor.ICON_COUNT_X + 2
                            val button = object : Button<EditorScreen>(palette, pickerStage, pickerStage) {
                                override fun render(screen: EditorScreen, batch: SpriteBatch,
                                                    shapeRenderer: ShapeRenderer) {
                                    super.render(screen, batch, shapeRenderer)
                                    val selection = editor.pickerSelection.currentSelection
                                    val label = this.labels.first() as TextLabel
                                    if (GameRegistry.isDataLoading() || editor.pickerSelection.currentSelection.groups.isEmpty()) {
                                        if (isUp) {
                                            label.text = Editor.ARROWS[2]
                                        } else {
                                            label.text = Editor.ARROWS[3]
                                        }
                                    } else {
                                        if (isVariant) {
                                            val current = selection.getCurrentVariant()
                                            if (isUp) {
                                                if (current.variantScroll > 0) {
                                                    label.text = Editor.ARROWS[0]
                                                } else {
                                                    label.text = Editor.ARROWS[2]
                                                }
                                            } else {
                                                if (current.variantScroll < current.maxScroll) {
                                                    label.text = Editor.ARROWS[1]
                                                } else {
                                                    label.text = Editor.ARROWS[3]
                                                }
                                            }
                                        } else {
                                            if (isUp) {
                                                if (selection.groupScroll > 0) {
                                                    label.text = Editor.ARROWS[0]
                                                } else {
                                                    label.text = Editor.ARROWS[2]
                                                }
                                            } else {
                                                if (selection.groupScroll < selection.maxGroupScroll) {
                                                    label.text = Editor.ARROWS[1]
                                                } else {
                                                    label.text = Editor.ARROWS[3]
                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                                    super.onLeftClick(xPercent, yPercent)
                                    val selection = editor.pickerSelection.currentSelection
                                    if (isVariant) {
                                        val current = selection.getCurrentVariant()
                                        if (isUp) {
                                            if (current.variantScroll > 0) {
                                                current.variantScroll--
                                                updateSelected()
                                            }
                                        } else {
                                            if (current.variantScroll < current.maxScroll) {
                                                current.variantScroll++
                                                updateSelected()
                                            }
                                        }
                                    } else {
                                        if (isUp) {
                                            if (selection.groupScroll > 0) {
                                                selection.groupScroll--
                                                updateSelected()
                                            }
                                        } else {
                                            if (selection.groupScroll < selection.maxGroupScroll) {
                                                selection.groupScroll++
                                                updateSelected()
                                            }
                                        }
                                    }
                                }
                            }.apply {
                                this.setLocation(x, y)
                                this.background = false
                                this.addLabel(
                                        object : TextLabel<EditorScreen>(palette, this, this.stage) {
                                            override fun getFont(): BitmapFont {
                                                return main.defaultBorderedFont
                                            }
                                        }.apply {
                                            this.setText(
                                                    if (isUp) Editor.ARROWS[2] else Editor.ARROWS[3],
                                                    Align.center, false, false
                                                        )
                                            this.background = false
                                        })
                            }

                            pickerStage.elements += button
                            if (isVariant) {
                                (variantScrollButtons as MutableList) += button
                            } else {
                                (gameScrollButtons as MutableList) += button
                            }
                        } else {
                            val isVariant = x == Editor.ICON_COUNT_X + 1
                            val button = GameButton(x, y, palette, pickerStage, pickerStage, { _, _ ->
                                this as GameButton
                                if (visible && this.game != null) {
                                    val selection = editor.pickerSelection.currentSelection
                                    if (isVariant) {
                                        selection.getVariant(selection.group).variant =
                                                y + selection.getVariant(selection.group).variantScroll
                                    } else {
                                        selection.group =
                                                (y + editor.pickerSelection.currentSelection.groupScroll) * Editor.ICON_COUNT_X + x
                                    }
                                    updateSelected()
                                }
                            }).apply {
                                this.setLocation(x, y)
                            }
                            if (isVariant) {
                                variantButtons += button
                            } else {
                                gameButtons += button
                            }
                        }
                    }
                }
                pickerStage.elements.addAll(gameButtons)
                pickerStage.elements.addAll(variantButtons)
            }

            run patternArea@ {
                patternLabels as MutableList
                val borderedPalette = palette.copy(ftfont = main.fonts[main.defaultBorderedFontKey])
                val padding2 = pickerStage.percentageOfWidth(
                        Editor.ICON_PADDING * 2)

                val upButton = object : Button<EditorScreen>(borderedPalette, patternAreaStage, patternAreaStage) {
                    override fun render(screen: EditorScreen, batch: SpriteBatch,
                                        shapeRenderer: ShapeRenderer) {
                        super.render(screen, batch, shapeRenderer)
                        val selection = editor.pickerSelection.currentSelection
                        val label = this.labels.first() as TextLabel
                        if (GameRegistry.isDataLoading() || editor.pickerSelection.currentSelection.groups.isEmpty()) {
                            label.text = Editor.ARROWS[2]
                        } else {
                            val current = selection.getCurrentVariant()
                            if (current.pattern > 0) {
                                label.text = Editor.ARROWS[0]
                            } else {
                                label.text = Editor.ARROWS[2]
                            }
                        }
                    }

                    override fun onLeftClick(xPercent: Float, yPercent: Float) {
                        super.onLeftClick(xPercent, yPercent)
                        val selection = editor.pickerSelection.currentSelection
                        val current = selection.getCurrentVariant()
                        if (current.pattern > 0) {
                            current.pattern--
                            updateSelected()
                        }
                    }
                }.apply {
                    this.location.set(screenX = padding2,
                                      screenWidth = patternAreaStage.percentageOfWidth(
                                              Editor.ICON_SIZE),
                                      screenHeight = patternAreaStage.percentageOfHeight(
                                              Editor.ICON_SIZE),
                                      screenY = startY)
                    this.background = false
                    this.addLabel(
                            TextLabel(borderedPalette, this, this.stage).apply {
                                this.setText(
                                        Editor.ARROWS[2],
                                        Align.center, false, false
                                            )
                                this.background = false
                            })
                }
                val downButton = object : Button<EditorScreen>(borderedPalette, patternAreaStage, patternAreaStage) {
                    override fun render(screen: EditorScreen, batch: SpriteBatch,
                                        shapeRenderer: ShapeRenderer) {
                        super.render(screen, batch, shapeRenderer)
                        val selection = editor.pickerSelection.currentSelection
                        val label = this.labels.first() as TextLabel
                        if (GameRegistry.isDataLoading() || editor.pickerSelection.currentSelection.groups.isEmpty()) {
                            label.text = Editor.ARROWS[3]
                        } else {
                            val current = selection.getCurrentVariant()
                            if (current.pattern < current.maxPatternScroll) {
                                label.text = Editor.ARROWS[1]
                            } else {
                                label.text = Editor.ARROWS[3]
                            }
                        }
                    }

                    override fun onLeftClick(xPercent: Float, yPercent: Float) {
                        super.onLeftClick(xPercent, yPercent)
                        val selection = editor.pickerSelection.currentSelection
                        val current = selection.getCurrentVariant()
                        if (current.pattern < current.maxPatternScroll) {
                            current.pattern++
                            updateSelected()
                        }
                    }
                }.apply {
                    this.location.set(screenX = padding2,
                                      screenWidth = patternAreaStage.percentageOfWidth(
                                              Editor.ICON_SIZE),
                                      screenHeight = patternAreaStage.percentageOfHeight(
                                              Editor.ICON_SIZE),
                                      screenY = startY - iconHeightPadded * (Editor.ICON_COUNT_Y - 1))
                    this.background = false
                    this.addLabel(
                            TextLabel(borderedPalette, this, this.stage).apply {
                                this.setText(
                                        Editor.ARROWS[3],
                                        Align.center, false, false
                                            )
                                this.background = false
                            })
                }

                patternAreaStage.elements += upButton
                patternAreaStage.elements += downButton

                val labelCount = Editor.PATTERN_COUNT
                val height = 1f / labelCount
                for (i in 1..labelCount) {
                    val centre = i == 1 + (labelCount / 2)
                    val borderedPalette = if (!centre) borderedPalette else borderedPalette.copy(
                            textColor = Color(Editor.SELECTED_TINT))
                    patternLabels +=
                            TextLabel(borderedPalette, patternAreaStage, patternAreaStage).apply {
                                this.location.set(
                                        screenHeight = height,
                                        screenY = 1f - (height * i),
                                        screenX = upButton.location.screenX + upButton.location.screenWidth +
                                                padding2
                                                 )
                                this.location.set(screenWidth = 1f - (this.location.screenX + padding2))
                                this.isLocalizationKey = false
                                this.textAlign = Align.left
                                this.textWrapping = false
                                this.text = "text label $i"
                            }

                    if (centre) {
                        patternAreaStage.elements +=
                                TextLabel(borderedPalette, patternAreaStage, patternAreaStage).apply {
                                    this.location.set(
                                            screenX = padding2,
                                            screenWidth = patternAreaStage.percentageOfWidth(
                                                    Editor.ICON_SIZE),
                                            screenHeight = height,
                                            screenY = 1f - (height * i)
                                                     )
                                    this.isLocalizationKey = false
                                    this.textAlign = Align.center
                                    this.textWrapping = false
                                    this.text = Editor.ARROWS[4]
                                }
                    }
                }

                patternAreaStage.elements.addAll(patternLabels)
            }
        }

        // Minimap area
        searchBar = object : TextField<EditorScreen>(palette, minimapBarStage, minimapBarStage) {

            init {
                this.textWhenEmptyColor = Color.LIGHT_GRAY
            }

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
                this.textWhenEmpty = Localization["picker.search"]
            }

            override fun onTextChange(oldText: String) {
                super.onTextChange(oldText)
                updateSelected(DirtyType.SEARCH_DIRTY)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                editor.pickerSelection.isSearching = true
                updateSelected(DirtyType.SEARCH_DIRTY)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                if (hasFocus) {
                    text = ""
                    updateSelected(DirtyType.SEARCH_DIRTY)
                }
            }
        }
        run minimap@ {
            minimapBarStage.updatePositions()
            seriesButtons as MutableList

            val buttonWidth: Float = minimapBarStage.percentageOfWidth(
                    Editor.ICON_SIZE)
            val buttonHeight: Float = 1f

            Series.VALUES.forEachIndexed { index, series ->
                val tmp = SeriesButton(series, palette, minimapBarStage, minimapBarStage, { x, y ->
                    editor.pickerSelection.currentSeries = series
                    updateSelected()
                }).apply {
                    this.location.set(
                            screenWidth = buttonWidth,
                            screenHeight = buttonHeight,
                            screenX = index * buttonWidth
                                     )
                    this.addLabel(
                            ImageLabel(palette, this, this.stage).apply {
                                this.image = TextureRegion(AssetRegistry.get<Texture>(series.textureId))
                                this.renderType = ImageLabel.ImageRendering.RENDER_FULL
                            }
                                 )
                }
                seriesButtons += tmp

            }
            minimapBarStage.elements.addAll(seriesButtons)

            searchBar.apply {
                val last = seriesButtons.last()
                this.location.set(screenX = last.location.screenX + last.location.screenWidth,
                                  screenHeight = 1f)
                this.location.set(screenWidth = 0.5f - location.screenX)
            }
            minimapBarStage.elements += searchBar
        }

        // Button bar
        run buttonBar@ {
            buttonBarStage.updatePositions()
            val stageWidth = buttonBarStage.location.realWidth
            val stageHeight = buttonBarStage.location.realHeight
            val padding = Editor.BUTTON_PADDING / stageWidth
            val size = Editor.BUTTON_SIZE / stageWidth
            buttonBarStage.elements +=
                    ColourPane(buttonBarStage, buttonBarStage).apply {
                        this.colour.set(Editor.TRANSLUCENT_BLACK)
                        this.colour.a = 0.5f
                        this.location.set(
                                screenX = -(Editor.BUTTON_PADDING / stageWidth),
                                screenY = -(Editor.BUTTON_PADDING / stageHeight),
                                screenWidth = 1f + (Editor.BUTTON_PADDING / stageWidth) * 2f,
                                screenHeight = 1f + (Editor.BUTTON_PADDING / stageHeight) * 2f
                                         )
                    }
            buttonBarStage.elements +=
                    Button(palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = AssetRegistry.get<TextureAtlas>("ui-icons").findRegion("newFile")
                        })
                    }
            buttonBarStage.elements +=
                    Button(palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size,
                                          screenX = size + padding)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = AssetRegistry.get<TextureAtlas>("ui-icons").findRegion("openFile")
                        })
                    }
            buttonBarStage.elements +=
                    Button(palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size,
                                          screenX = size * 2 + padding * 2)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = AssetRegistry.get<TextureAtlas>("ui-icons").findRegion("saveFile")
                        })
                    }
            val themeButton = ThemeButton(palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = size * 3 + padding * 3)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_palette"))
                })
            }
            buttonBarStage.elements += themeButton
            buttonBarStage.elements += themeButton.contextMenu
        }

        this.updatePositions()
        this.updateSelected()
    }

    abstract inner class SelectableButton(palette: UIPalette, parent: UIElement<EditorScreen>,
                                          stage: Stage<EditorScreen>,
                                          val onLeftClickFunc: SelectableButton.(Float, Float) -> Unit = { x, y -> })
        : Button<EditorScreen>(palette, parent, stage) {

        val label: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.renderType = ImageLabel.ImageRendering.RENDER_FULL
            this.image = TextureRegion(AssetRegistry.missingTexture)
        }

        var selected: Boolean = false
            set(value) {
                val old = field
                field = value
                if (field != old) {
                    this.removeLabel(selectedLabel)
                    if (field) {
                        this.addLabel(selectedLabel)
                        selectedLabel.onResize(this.location.realWidth, this.location.realHeight)
                    }
                }
            }
        abstract val selectedLabel: ImageLabel<EditorScreen>

        init {
            addLabel(label)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            this.onLeftClickFunc(xPercent, yPercent)
        }
    }

    open inner class GameButton(val x: Int, val y: Int,
                                palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>,
                                f: SelectableButton.(Float, Float) -> Unit)
        : SelectableButton(palette, parent, stage, f) {

        var game: Game? = null
            set(value) {
                field = value
                this.visible = field != null
                if (value != null) {
                    this.label.image!!.setRegion(value.icon)
                } else {
                    this.selected = false
                }
            }

        override val selectedLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.image = selectorRegion
        }

    }

    open inner class SeriesButton(val series: Series,
                                  palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>,
                                  onLeftClick: SelectableButton.(Float, Float) -> Unit)
        : SelectableButton(palette, parent, stage, onLeftClick) {

        override val selectedLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.image = selectorRegionSeries
        }
    }

}
