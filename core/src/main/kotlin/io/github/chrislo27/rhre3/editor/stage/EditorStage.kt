package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.ClickOccupation
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.editor.picker.*
import io.github.chrislo27.rhre3.editor.quickswitch.SwitchToGame
import io.github.chrislo27.rhre3.editor.stage.advopt.CopyGamesUsedButton
import io.github.chrislo27.rhre3.editor.stage.advopt.SelectionToJSONButton
import io.github.chrislo27.rhre3.entity.model.IEditableText
import io.github.chrislo27.rhre3.entity.model.special.SubtitleEntity
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameMetadata
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.util.OSUtils
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.*
import java.util.*
import kotlin.math.roundToInt


class EditorStage(parent: UIElement<EditorScreen>?,
                  camera: OrthographicCamera, val main: RHRE3Application, val editor: Editor)
    : Stage<EditorScreen>(parent, camera), Palettable {

    override var palette: UIPalette = main.uiPalette.copy(
            backColor = Color(main.uiPalette.backColor).apply { this.a = 0.5f })
    val paneLikeStages: List<Stage<EditorScreen>> = mutableListOf()
    val messageBarStage: Stage<EditorScreen>
    val buttonBarStage: Stage<EditorScreen>
    val pickerStage: Stage<EditorScreen>
    val pickerDisplay: PickerDisplay
    val minimapBarStage: Stage<EditorScreen>
    lateinit var minimap: Minimap
        private set
    val centreAreaStage: Stage<EditorScreen>
    val subtitleStage: Stage<EditorScreen>
    val patternAreaStage: Stage<EditorScreen>
    val tapalongStage: TapalongStage
    val presentationModeStage: PresentationModeStage
    val themeEditorStage: ThemeEditorStage
    val themeChooserStage: ThemeChooserStage
    val viewChooserStage: ViewChooserStage

    val gameButtons: List<GameButton>
    val variantButtons: List<GameButton>
    val filterButtons: List<FilterButton>
    val toolButtons: List<ToolButton>
    val gameScrollButtons: List<Button<EditorScreen>>
    val variantScrollButtons: List<Button<EditorScreen>>
    val datamodelScrollButtons: List<Button<EditorScreen>>

    val selectorRegion: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("ui_selector_fever")) }
    val favouriteTagRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("ui_selector_favourite")) }
    val selectorRegionSeries: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("ui_selector")) }
    lateinit var searchBar: SearchBar<EditorScreen>
        private set
    val messageLabel: TextLabel<EditorScreen>
    val controlsLabel: TextLabel<EditorScreen>

    val hoverTextLabel: TextLabel<EditorScreen>
    val searchFilter = SearchFilter(this)
    val favouritesFilter = FavouritesFilter()
    val recentsFilter = RecentFilter()
    val storedPatternsFilter = StoredPatternsFilter()

    lateinit var playButton: PlaybackButton
        private set
    lateinit var pauseButton: PlaybackButton
        private set
    lateinit var stopButton: PlaybackButton
        private set
    lateinit var langButton: LangButton<EditorScreen>
        private set
    lateinit var newsButton: NewsButton
        private set
    lateinit var jumpToField: JumpToField
        private set
    lateinit var subtitleLabel: TextLabel<EditorScreen>
        private set
    lateinit var entityTextField: TextField<EditorScreen>
        private set
    lateinit var gameStageText: TextLabel<EditorScreen>
        private set
    lateinit var patternAreaArrowLabel: TextLabel<EditorScreen>
        private set
    lateinit var patternPreviewButton: PatternPreviewButton
        private set
    lateinit var editStoredPatternButton: EditStoredPatternButton
        private set
    lateinit var baseBpmLabel: TextLabel<EditorScreen>
        private set
    lateinit var bottomBaseBpmLabel: TextLabel<EditorScreen>
        private set
    lateinit var customSoundsFolderButton: Button<EditorScreen>
        private set

    val topOfMinimapBar: Float
        get() {
            return centreAreaStage.location.realY
        }
    val isTyping: Boolean
        get() {
            return searchBar.textField.hasFocus || jumpToField.hasFocus || entityTextField.hasFocus
        }
    val tapalongMarkersEnabled: Boolean
        get() = tapalongStage.markersEnabled

    private var isDirty = DirtyType.CLEAN
    private var wasDebug = false

    private var gameSwitchBack: SwitchToGame? = null

    enum class DirtyType {
        CLEAN, DIRTY, SEARCH_DIRTY
    }

    interface HasHoverText {
        fun getHoverText(): String
    }

    init {
        Localization.listeners += {
            updateSelected(DirtyType.SEARCH_DIRTY)
        }
    }

    private fun setHoverText(text: String) {
        val label = hoverTextLabel
        val labelLoc = label.location
        val font = label.getFont()

        label.visible = true
        label.text = text

        font.data.setScale(label.palette.fontScale * label.fontScaleMultiplier)

        labelLoc.set(pixelX = camera.getInputX(), pixelY = camera.getInputY() + 2,
                     pixelWidth = font.getTextWidth(label.text) + 6,
                     pixelHeight = font.getTextHeight(text) + font.capHeight)

        val yLimit = label.stage.camera.viewportHeight
        val top = labelLoc.pixelY + labelLoc.pixelHeight
        if (top > yLimit) {
            val height = labelLoc.pixelHeight
            labelLoc.set(pixelY = yLimit - labelLoc.pixelHeight,
                         pixelX = labelLoc.pixelX + ((top - yLimit) / height).coerceAtMost(1f) * height)
        }

        // clamp X
        labelLoc.set(pixelX = Math.min(labelLoc.pixelX,
                                       label.stage.camera.viewportWidth - labelLoc.pixelWidth))

        font.data.setScale(1f)
        val labelParent = label.parent!!
        label.onResize(labelParent.location.realWidth, labelParent.location.realHeight)
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        hoverTextLabel.visible = false
        elements.firstOrNull {
            if (it is HasHoverText && it.isMouseOver() && it.visible) {
                setHoverText(it.getHoverText())
                true
            } else false
        }
                ?: elements.firstOrNull { stage ->
                    if (stage !is Stage || !stage.visible) {
                        false
                    } else {
                        stage.elements.any {
                            if (it is HasHoverText && it.isMouseOver() && it.visible) {
                                setHoverText(it.getHoverText())
                                true
                            } else false
                        }
                    }
                }
                ?: searchBar.elements.firstOrNull {
                    // hack
                    if (searchBar.visible) {
                        if (it is HasHoverText && it.isMouseOver() && it.visible) {
                            setHoverText(it.getHoverText())
                            true
                        } else false
                    } else {
                        false
                    }
                }
                ?: editor.getHoverText().takeIf(String::isNotEmpty)?.also(this::setHoverText)

        if (Toolboks.debugMode != wasDebug) {
            wasDebug = Toolboks.debugMode
            updateSelected(DirtyType.DIRTY)
        }

        patternAreaArrowLabel.textColor = if (editor.currentTool == Tool.SELECTION) Editor.SELECTED_TINT else null

        super.render(screen, batch, shapeRenderer)

        if (!Gdx.input.isControlDown() && !Gdx.input.isAltDown() && !Gdx.input.isShiftDown()) {
            val quickSwitch = gameSwitchBack
            if (Gdx.input.isKeyJustPressed(Input.Keys.F) && quickSwitch != null && !isTyping) { // Quick switch
                val qsCopy = quickSwitch
                val button = qsCopy.button
                val filter = button.filter
                val gameList = filter.currentGameList

                button.onLeftClick(0f, 0f)

                filter.groupScroll = qsCopy.groupScroll
                if (qsCopy.currentGroup != null)
                    filter.currentGroupIndex = filter.gameGroups.indexOf(qsCopy.currentGroup).coerceAtLeast(0)
                if (!filter.areGamesEmpty && qsCopy.currentGame != null && gameList != null) {
                    gameList.currentIndex = gameList.list.indexOf(qsCopy.currentGame).coerceAtLeast(0)
                }

                updateSelected()
            }
        }

        if (isDirty != DirtyType.CLEAN && !GameRegistry.isDataLoading()) {
            val pickerSelection = editor.pickerSelection
            val filter = pickerSelection.filter
            val isSearching = filter === searchFilter

            if (isSearching) {
                if (isDirty == DirtyType.SEARCH_DIRTY) {
                    val query = searchBar.textField.text.toLowerCase(Locale.ROOT)

                    searchFilter.query = query
                    searchFilter.update()
                    searchFilter.sort()
                }
            } else {
                filter.update()
            }
            filter.sort()

            filterButtons.forEach {
                it.selected = it.filter === filter
            }
            toolButtons.forEach {
                it.selected = it.tool == editor.currentTool
            }

            gameButtons.forEach {
                it.game = null
            }
            filter.gameGroups
                    .forEachIndexed { index, group ->
                        val x: Int = index % Editor.ICON_COUNT_X
                        val y: Int = index / Editor.ICON_COUNT_X - filter.groupScroll

                        if (y in 0 until Editor.ICON_COUNT_Y) {
                            val buttonIndex = y * Editor.ICON_COUNT_X + x
                            gameButtons[buttonIndex].apply {
                                val gameList = filter.gamesPerGroup[group]
                                        ?: return@apply
                                this.game = if (gameList.isEmpty) return@apply else gameList.current
                                if (filter.currentGroupIndex == index) {
                                    this.selected = true
                                }
                                val isFavourited = isFavourited()
                                if (isFavourited) {
                                    addLabel(2, favouriteLabel)
                                    favouriteLabel.onResize(location.realWidth, location.realHeight)
                                } else {
                                    removeLabel(favouriteLabel)
                                }
                            }
                        }
                    }
            val anyGames = gameButtons.any {
                it.game != null
            }
            gameScrollButtons.forEach {
                it.visible = anyGames
            }

            variantButtons.forEach {
                it.game = null
            }
            filter.currentGroup?.also { group ->
                val currentGameList = filter.currentGameList
                        ?: return@also
                filter.gamesPerGroup[group]?.list?.forEachIndexed { index, game ->
                    val y = index - (currentGameList.scroll)
                    if (y in 0 until Editor.ICON_COUNT_Y) {
                        variantButtons[y].apply {
                            this.game = game
                            if (currentGameList.currentIndex == index) {
                                this.selected = true
                            }

                            val isFavourited = isFavourited()
                            if (isFavourited) {
                                addLabel(2, favouriteLabel)
                                favouriteLabel.onResize(location.realWidth, location.realHeight)
                            } else {
                                removeLabel(favouriteLabel)
                            }
                        }
                    }
                }
            }
            val anyVariants = variantButtons.any {
                it.game != null
            }
            variantScrollButtons.forEach {
                it.visible = anyVariants
            }

            val anyDatamodels = !filter.areDatamodelsEmpty
            baseBpmLabel.visible = anyDatamodels
            bottomBaseBpmLabel.visible = false
            val currentDatamodelList = filter.currentDatamodelList
            if (!filter.areDatamodelsEmpty && currentDatamodelList != null) {
                fun <T> MutableList<T>.getOrAdd(index: Int, function: (Int) -> T): T {
                    return getOrNull(index)
                            ?: function(index).also {
                                add(it)
                            }
                }

                val objects = currentDatamodelList.list

                objects.forEachIndexed { index, datamodel ->
                    var text = datamodel.name
                    var color = Color.WHITE
                    val label: PickerDisplay.Label = pickerDisplay.labels.getOrAdd(index) {
                        PickerDisplay.Label("", Color.WHITE)
                    }
                    if (Toolboks.debugMode) {
                        text += " [GRAY](${datamodel.id})[]"
                    }
                    if (index != currentDatamodelList.currentIndex && datamodel is Cue) {
                        color = Editor.CUE_PATTERN_COLOR
                    }
                    label.string = text
                    label.color = color
                }
                while (pickerDisplay.labels.size > objects.size) {
                    pickerDisplay.labels.removeAt(pickerDisplay.labels.size - 1)
                }

                val possibleBaseBpm = filter.currentDatamodel?.possibleBaseBpm
                if (possibleBaseBpm == null) {
                    baseBpmLabel.visible = false
                } else {
                    baseBpmLabel.text = "♩=" + possibleBaseBpm.start.roundToInt()
                    if (possibleBaseBpm.start.roundToInt() != possibleBaseBpm.endInclusive.roundToInt()) {
                        bottomBaseBpmLabel.visible = true
                        bottomBaseBpmLabel.text = "♩=" + possibleBaseBpm.endInclusive.roundToInt()
                    }
                }
            }
            datamodelScrollButtons.forEach {
                it.visible = anyDatamodels
            }
            patternAreaArrowLabel.visible = anyDatamodels
            if (filter == storedPatternsFilter) {
                editStoredPatternButton.visible = storedPatternsFilter.currentPattern != null
                patternPreviewButton.visible = false
            } else {
                patternPreviewButton.visible = editor.remix.playState == PlayState.STOPPED // Allows the update method to change visibility if stopped
                patternPreviewButton.update(if (anyDatamodels) filter.currentDatamodel else null)
                editStoredPatternButton.visible = false
            }

            editor.updateMessageLabel()

            gameStageText.text = ""
            customSoundsFolderButton.visible = filter is CustomFilter
            if (filter.areGroupsEmpty) {
                if (filter == favouritesFilter) {
                    gameStageText.text = Localization["editor.nothing.favourites"]
                } else if (filter == recentsFilter) {
                    gameStageText.text = Localization["editor.nothing.recents"]
                } else if (filter == searchFilter) {
                    gameStageText.text = Localization["editor.nothing.search"]
                } else if (filter is CustomFilter) {
                    gameStageText.text = Localization["editor.nothing.customs", "${if (OSUtils.IS_WINDOWS) "<user>" else "~"}/" + RHRE3.RHRE3_FOLDER.name() + "/" + GameRegistry.CUSTOM_SFX_FOLDER.name()]
                    val screenX = 0.25f - customSoundsFolderButton.location.screenWidth / 2f
                    if (customSoundsFolderButton.location.screenX != screenX) {
                        customSoundsFolderButton.location.set(screenY = 0.5f * pickerStage.percentageOfHeight(Editor.ICON_PADDING), screenX = screenX)
                        customSoundsFolderButton.stage.updatePositions()
                    }
                } else if (filter is SeriesFilter) {
                    gameStageText.text = Localization["editor.nothing.series"]
                } else if (filter is StoredPatternsFilter) {
                    gameStageText.text = Localization["editor.storedPatterns.add"]
                }
            }

            isDirty = DirtyType.CLEAN
        }

        val filter = editor.pickerSelection.filter
        if (filter is StoredPatternsFilter) {
            gameStageText.text = Localization[if (editor.clickOccupation is ClickOccupation.SelectionDrag && pickerStage.isMouseOver() && !patternAreaStage.isMouseOver()) "editor.msg.storingSelection" else "editor.storedPatterns.add"]
        }
    }

    fun updateSelected(type: DirtyType = DirtyType.DIRTY) {
        isDirty = type
    }

    fun createQuickSwitch(): SwitchToGame? {
        val fb = filterButtons.firstOrNull(FilterButton::selected)
                ?: return null
        return SwitchToGame(fb)
    }

    init {
        paneLikeStages as MutableList
        gameButtons = mutableListOf()
        variantButtons = mutableListOf()
        filterButtons = mutableListOf()
        toolButtons = mutableListOf()
        gameScrollButtons = mutableListOf()
        variantScrollButtons = mutableListOf()
        datamodelScrollButtons = mutableListOf()

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
        messageLabel = object : TextLabel<EditorScreen>(palette,
                                                        messageBarStage, messageBarStage) {
            private var lastVersionTextWidth: Float = -1f

            override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
                if (main.versionTextWidth != lastVersionTextWidth) {
                    lastVersionTextWidth = main.versionTextWidth
                    this.location.set(screenX = messageBarStage.percentageOfWidth(2f),
                                      screenY = messageBarStage.percentageOfHeight(2f))
                    this.location.set(
                            screenWidth = 1f - (main.versionTextWidth / messageBarStage.location.realWidth + this.location.screenX))
                    this.stage.updatePositions()
                }
            }
        }.apply {
            this.fontScaleMultiplier = 0.5f
            this.textAlign = Align.left
            this.textWrapping = false
            this.location.set(0f, 0f,
                              1f,
                              0.5f,
                              pixelWidth = -8f,
                              pixelY = -2f)
            this.isLocalizationKey = false
        }
        messageBarStage.elements += messageLabel
        controlsLabel = TextLabel(palette, messageBarStage, messageBarStage).apply {
            this.fontScaleMultiplier = 0.5f
            this.textAlign = Align.left
            this.textWrapping = false
            this.location.set(messageBarStage.percentageOfWidth(2f), 0.5f + messageBarStage.percentageOfHeight(2f),
                              1f - messageBarStage.percentageOfWidth(2f) * 2,
                              0.5f,
                              pixelWidth = 0f,
                              pixelY = -2f)
            this.isLocalizationKey = false
        }
        messageBarStage.elements += controlsLabel
        elements += messageBarStage
        buttonBarStage = Stage(this, camera).apply {
            this.location.set(screenX = (Editor.BUTTON_PADDING / RHRE3.WIDTH),
                              screenY = 1f - ((Editor.BUTTON_PADDING + Editor.BUTTON_SIZE) / RHRE3.HEIGHT),
                              screenWidth = 1f - (Editor.BUTTON_PADDING / RHRE3.WIDTH) * 2f,
                              screenHeight = Editor.BUTTON_SIZE / RHRE3.HEIGHT)
        }
        pickerStage = object : Stage<EditorScreen>(this, camera) {
            override fun scrolled(amount: Int): Boolean {
                if (isMouseOver()) {
                    val filter = editor.pickerSelection.filter
                    when (stage.camera.getInputX()) {
                        // datamodel
                        in (location.realX + location.realWidth * 0.5f)..(location.realX + location.realWidth) -> {
                            val currentDatamodelList = filter.currentDatamodelList
                            if (!filter.areDatamodelsEmpty && currentDatamodelList != null) {
                                val old = currentDatamodelList.currentIndex
                                currentDatamodelList.currentIndex += amount
                                if (old != currentDatamodelList.currentIndex) {
                                    updateSelected()
                                    return true
                                }
                            }
                        }
                        // variants
                        in (variantButtons.first().location.realX)..(location.realX + location.realWidth * 0.5f) -> {
                            val currentGameList = filter.currentGameList
                            if (!filter.areGamesEmpty && currentGameList != null) {
                                val old = currentGameList.scroll
                                currentGameList.scroll += amount
                                if (old != currentGameList.scroll) {
                                    updateSelected()
                                    return true
                                }
                            }
                        }
                        // game groups
                        in (location.realX)..(variantButtons.first().location.realX) -> {
                            if (!filter.areGroupsEmpty) {
                                val old = filter.groupScroll
                                filter.groupScroll += amount
                                if (old != filter.groupScroll) {
                                    updateSelected()
                                    return true
                                }
                            }
                        }
                    }
                    return true
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
                this.location.set(screenX = 0.5f, screenWidth = 0f, screenHeight = 1f, pixelX = -0.5f, pixelWidth = 1f)
            }
        }
        patternAreaStage = Stage(this, camera).apply {
            this.location.set(screenY = pickerStage.location.screenY,
                              screenHeight = pickerStage.location.screenHeight,
                              screenX = 0.5f,
                              screenWidth = 0.5f)
        }
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

        centreAreaStage = Stage(this, camera).apply {
            this.location.set(screenY = minimapBarStage.location.screenY + minimapBarStage.location.screenHeight)
            this.location.set(
                    screenHeight = (buttonBarStage.location.screenY - this.location.screenY - (Editor.BUTTON_PADDING / RHRE3.HEIGHT)))
            this.updatePositions()
            this.elements += GameDisplayStage(editor, palette, this, this.camera).apply display@{
                this.location.set(screenHeight = this@apply.percentageOfHeight(32f),
                                  screenX = this@apply.percentageOfWidth(8f),
                                  screenWidth = this@apply.percentageOfWidth(
                                          32f) * GameDisplayStage.WIDTH_MULTIPLICATION)
                this.location.set(screenY = 1f - (this@apply.percentageOfHeight(8f) + this.location.screenHeight))
                this.updatePositions()
            }
        }
        subtitleStage = Stage(this, camera).apply {
            this.location.set(centreAreaStage.location.screenX, centreAreaStage.location.screenY,
                              centreAreaStage.location.screenWidth, centreAreaStage.location.screenHeight)
            subtitleLabel = object : TextLabel<EditorScreen>(palette, this@apply, this@apply) {
                override fun getFont(): BitmapFont {
                    return main.defaultBorderedFont
                }

                override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    text = (if (main.preferences.getBoolean(PreferenceKeys.SETTINGS_SUBTITLE_ORDER, false))
                        editor.remix.currentSubtitles
                    else editor.remix.currentSubtitlesReversed).joinToString(separator = "\n", transform = SubtitleEntity::subtitle)

                    super.render(screen, batch, shapeRenderer)
                }
            }.apply {
                this.location.set(screenY = 0.025f)
                this.location.set(screenHeight = 0.1f - this.location.screenY)
                this.textWrapping = false
                this.isLocalizationKey = false
                this.textAlign = Align.bottom or Align.center
            }
            this.elements += subtitleLabel
            entityTextField = object : TextField<EditorScreen>(palette, this@apply, this@apply) {
                override fun frameUpdate(screen: EditorScreen) {
                    super.frameUpdate(screen)

                    attemptSetInvisible()
                }

                private fun attemptSetInvisible() {
                    if (visible &&
                            (!hasFocus
                                    || editor.selection.size != 1
                                    || editor.selection.first() !is IEditableText)) {
                        visible = false
                    }
                }

                override fun onTextChange(oldText: String) {
                    super.onTextChange(oldText)

                    if (!hasFocus || !visible)
                        return

                    if (editor.selection.firstOrNull() is IEditableText) {
                        val entity = editor.selection.first() as IEditableText
                        entity.text = this.text
                    }
                }

                override fun onEnterPressed(): Boolean {
                    hasFocus = false
                    attemptSetInvisible()
                    return true
                }
            }.apply {
                this.location.set(screenY = subtitleLabel.location.screenHeight,
                                  screenHeight = 0.1f,
                                  screenWidth = 0.5f,
                                  screenX = 0.25f)
                this.background = true
                this.visible = false
                this.canInputNewlines = true
            }
            this.elements += entityTextField
        }

        hoverTextLabel = TextLabel(palette.copy(backColor = Color(palette.backColor).also { it.a *= 1.5f }),
                                   this, this).apply {
            this.background = true
            this.isLocalizationKey = false
            this.fontScaleMultiplier = 0.75f
            this.textWrapping = false
            this.visible = false
            this.alignment = Align.bottomLeft
            this.textAlign = Align.center
            this.location.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }
        tapalongStage = TapalongStage(editor, palette, this, camera).apply {
            this.location.set(0f,
                              messageBarStage.location.screenY + messageBarStage.location.screenHeight,
                              1f, pickerStage.location.screenHeight + minimapBarStage.location.screenHeight)

            this.visible = false
        }
        presentationModeStage = PresentationModeStage(editor, palette, this, camera).apply {
            this.location.set(0f,
                              messageBarStage.location.screenY + messageBarStage.location.screenHeight,
                              1f, pickerStage.location.screenHeight + minimapBarStage.location.screenHeight)

            this.visible = false
        }
        themeEditorStage = ThemeEditorStage(editor, palette, this, camera).apply {
            this.location.set(screenWidth = 0.3f,
                              screenY = minimapBarStage.location.screenY + minimapBarStage.location.screenHeight)
            this.location.set(screenX = 1f - this.location.screenWidth,
                              screenHeight = (buttonBarStage.location.screenY - this@EditorStage.percentageOfHeight(
                                      Editor.BUTTON_PADDING)) - (this.location.screenY))
            this.visible = false
        }
        themeChooserStage = ThemeChooserStage(editor, palette, this, camera).apply {
            this.location.set(screenWidth = 0.3f,
                              screenY = minimapBarStage.location.screenY + minimapBarStage.location.screenHeight)
            this.location.set(screenX = 1f - this.location.screenWidth,
                              screenHeight = (buttonBarStage.location.screenY - this@EditorStage.percentageOfHeight(
                                      Editor.BUTTON_PADDING)) - (this.location.screenY))
            this.visible = false
        }
        viewChooserStage = ViewChooserStage(editor, palette, this, camera).apply {
            this.location.set(screenWidth = 0.3f,
                              screenY = minimapBarStage.location.screenY + minimapBarStage.location.screenHeight)
            this.location.set(screenX = 1f - this.location.screenWidth,
                              screenHeight = (buttonBarStage.location.screenY - this@EditorStage.percentageOfHeight(
                                      Editor.BUTTON_PADDING)) - (this.location.screenY))
            this.visible = false
        }
        elements += presentationModeStage
        elements += tapalongStage
        elements += buttonBarStage
        elements += pickerStage
        elements += patternAreaStage
        elements += minimapBarStage
        elements += centreAreaStage
        elements += subtitleStage
//        elements += themeEditorStage
        elements += themeChooserStage
        elements += viewChooserStage
        elements += hoverTextLabel
        paneLikeStages += themeChooserStage
        paneLikeStages += viewChooserStage
        paneLikeStages += themeEditorStage
        this.updatePositions()

        pickerDisplay = PickerDisplay(editor, Editor.PATTERN_COUNT, palette, patternAreaStage, patternAreaStage)

        run pickerAndCo@{
            val iconWidth = pickerStage.percentageOfWidth(Editor.ICON_SIZE)
            val iconHeight = pickerStage.percentageOfHeight(Editor.ICON_SIZE)
            val iconWidthPadded = pickerStage.percentageOfWidth(Editor.ICON_SIZE + Editor.ICON_PADDING)
            val iconHeightPadded = pickerStage.percentageOfHeight(Editor.ICON_SIZE + Editor.ICON_PADDING)
            val startX = pickerStage.percentageOfWidth(
                    (pickerStage.location.realWidth / 2f) -
                            ((Editor.ICON_SIZE + Editor.ICON_PADDING) * (Editor.ICON_COUNT_X + 3)
                                    - Editor.ICON_PADDING)
                                                      ) / 2f
            val startY = 1f - (pickerStage.percentageOfHeight(
                    (Editor.ICON_SIZE + Editor.ICON_PADDING) * (Editor.ICON_COUNT_Y - 2) / 2f
                                                             ))

            // Picker area
            run picker@{
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

                gameStageText = TextLabel(palette.copy(textColor = Color.LIGHT_GRAY.cpy().apply { a = 0.8f }),
                                          pickerStage, pickerStage).apply {
                    this.location.set(0f, 0f, 0.5f, 1f)
                    this.isLocalizationKey = false
                    this.text = ""
                    this.textAlign = Align.center
                    this.fontScaleMultiplier = 0.9f
                }
                pickerStage.elements += gameStageText
                customSoundsFolderButton = object : Button<EditorScreen>(palette, pickerStage, pickerStage) {
                    override fun onLeftClick(xPercent: Float, yPercent: Float) {
                        super.onLeftClick(xPercent, yPercent)

                        Gdx.net.openURI("file:///${GameRegistry.CUSTOM_SFX_FOLDER.file().absolutePath}")
                    }
                }.apply {
                    setLocation(Editor.ICON_COUNT_X, 0)
                    this.location.set(screenY = 0.5f - this.location.screenHeight / 2f)
                    this.addLabel(ImageLabel(palette, this, this.stage).apply {
                        renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                        image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
                    })
                    this.visible = false
                }
                pickerStage.elements += customSoundsFolderButton

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
                                    val filter = editor.pickerSelection.filter
                                    val label = this.labels.first() as TextLabel
                                    if (GameRegistry.isDataLoading() || if (isVariant) filter.areGamesEmpty else filter.areGroupsEmpty) {
                                        if (isUp) {
                                            label.text = Editor.ARROWS[2]
                                        } else {
                                            label.text = Editor.ARROWS[3]
                                        }
                                    } else {
                                        if (isVariant) {
                                            val gameList = filter.currentGameList
                                            val scroll = gameList?.scroll
                                                    ?: 0
                                            if (isUp) {
                                                label.text = Editor.ARROWS[if (scroll > 0) 0 else 2]
                                            } else {
                                                label.text = Editor.ARROWS[if (scroll < gameList?.maxScroll ?: 0) 1 else 3]
                                            }
                                        } else {
                                            if (isUp) {
                                                label.text = Editor.ARROWS[if (filter.groupScroll > 0) 0 else 2]
                                            } else {
                                                label.text = Editor.ARROWS[if (filter.groupScroll < filter.maxGroupScroll) 1 else 3]
                                            }
                                        }
                                    }
                                }

                                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                                    super.onLeftClick(xPercent, yPercent)
                                    val filter = editor.pickerSelection.filter
                                    val gameList = filter.currentGameList
                                            ?: return
                                    if (isVariant) {
                                        if (isUp) {
                                            if (gameList.scroll > 0) {
                                                gameList.scroll--
                                                updateSelected()
                                            }
                                        } else {
                                            if (gameList.scroll < gameList.maxIndex) {
                                                gameList.scroll++
                                                updateSelected()
                                            }
                                        }
                                    } else {
                                        if (isUp) {
                                            if (filter.groupScroll > 0) {
                                                filter.groupScroll--
                                                updateSelected()
                                            }
                                        } else {
                                            if (filter.groupScroll < filter.maxGroupScroll) {
                                                filter.groupScroll++
                                                updateSelected()
                                            }
                                        }
                                    }
                                }

                                override fun onRightClick(xPercent: Float, yPercent: Float) {
                                    super.onRightClick(xPercent, yPercent)
                                    val filter = editor.pickerSelection.filter
                                    val gameList = filter.currentGameList
                                            ?: return
                                    if (isVariant) {
                                        if (isUp) {
                                            if (gameList.scroll > 0) {
                                                gameList.scroll -= Math.min(Editor.ICON_COUNT_Y, gameList.scroll)
                                                updateSelected()
                                            }
                                        } else {
                                            if (gameList.scroll < gameList.maxIndex) {
                                                gameList.scroll += Math.min(Editor.ICON_COUNT_Y, gameList.maxIndex - gameList.scroll)
                                                updateSelected()
                                            }
                                        }
                                    } else {
                                        if (isUp) {
                                            if (filter.groupScroll > 0) {
                                                filter.groupScroll -= Math.min(Editor.ICON_COUNT_Y, filter.groupScroll)
                                                updateSelected()
                                            }
                                        } else {
                                            if (filter.groupScroll < filter.maxGroupScroll) {
                                                filter.groupScroll += Math.min(Editor.ICON_COUNT_Y, filter.maxGroupScroll - filter.groupScroll)
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
                            val button = GameButton(x, y, isVariant, palette, pickerStage, pickerStage) { _, _ ->
                                this as GameButton

                                gameSwitchBack = createQuickSwitch() ?: gameSwitchBack

                                if (visible && this.game != null) {
                                    val filter = editor.pickerSelection.filter
                                    if (isVariant) {
                                        val currentGameList = filter.currentGameList
                                        if (!filter.areGamesEmpty && currentGameList != null) {
                                            currentGameList.currentIndex = y + currentGameList.scroll
                                        }
                                    } else {
                                        filter.currentGroupIndex = (y + filter.groupScroll) * Editor.ICON_COUNT_X + x
                                    }
                                    updateSelected()
                                }
                            }.apply {
                                this.setLocation(x, y)
                                this.visible = false
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

            run patternArea@{
                val borderedPalette = palette.copy(ftfont = main.fonts[main.defaultBorderedFontKey])
                val padding2 = pickerStage.percentageOfWidth(
                        Editor.ICON_PADDING * 2)

                val upButton = object : Button<EditorScreen>(borderedPalette, patternAreaStage, patternAreaStage) {
                    override fun render(screen: EditorScreen, batch: SpriteBatch,
                                        shapeRenderer: ShapeRenderer) {
                        super.render(screen, batch, shapeRenderer)
                        val filter = editor.pickerSelection.filter
                        val label = this.labels.first() as TextLabel

                        val currentDatamodelList = filter.currentDatamodelList
                        if (GameRegistry.isDataLoading() || currentDatamodelList == null) {
                            label.text = Editor.ARROWS[2]
                        } else {
                            if (currentDatamodelList.currentIndex > 0) {
                                label.text = Editor.ARROWS[0]
                            } else {
                                label.text = Editor.ARROWS[2]
                            }
                        }
                    }

                    override fun onLeftClick(xPercent: Float, yPercent: Float) {
                        super.onLeftClick(xPercent, yPercent)
                        val filter = editor.pickerSelection.filter
                        val currentDatamodelList = filter.currentDatamodelList
                        if (!filter.areDatamodelsEmpty && currentDatamodelList != null && currentDatamodelList.currentIndex > 0) {
                            currentDatamodelList.currentIndex--
                            updateSelected()
                        }
                    }

                    override fun onRightClick(xPercent: Float, yPercent: Float) {
                        super.onRightClick(xPercent, yPercent)
                        val filter = editor.pickerSelection.filter
                        val currentDatamodelList = filter.currentDatamodelList
                        if (!filter.areDatamodelsEmpty && currentDatamodelList != null && currentDatamodelList.currentIndex > 0) {
                            currentDatamodelList.currentIndex -= Math.min(currentDatamodelList.currentIndex, Editor.PATTERN_COUNT)
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
                        val filter = editor.pickerSelection.filter
                        val label = this.labels.first() as TextLabel

                        val currentDatamodelList = filter.currentDatamodelList
                        if (GameRegistry.isDataLoading() || currentDatamodelList == null) {
                            label.text = Editor.ARROWS[3]
                        } else {
                            if (currentDatamodelList.currentIndex < currentDatamodelList.maxIndex) {
                                label.text = Editor.ARROWS[1]
                            } else {
                                label.text = Editor.ARROWS[3]
                            }
                        }
                    }

                    override fun onLeftClick(xPercent: Float, yPercent: Float) {
                        super.onLeftClick(xPercent, yPercent)
                        val filter = editor.pickerSelection.filter
                        val currentDatamodelList = filter.currentDatamodelList
                        if (!filter.areDatamodelsEmpty && currentDatamodelList != null && currentDatamodelList.currentIndex < currentDatamodelList.maxIndex) {
                            currentDatamodelList.currentIndex++
                            updateSelected()
                        }
                    }

                    override fun onRightClick(xPercent: Float, yPercent: Float) {
                        super.onRightClick(xPercent, yPercent)
                        val filter = editor.pickerSelection.filter
                        val currentDatamodelList = filter.currentDatamodelList
                        if (!filter.areDatamodelsEmpty && currentDatamodelList != null && currentDatamodelList.currentIndex < currentDatamodelList.maxIndex) {
                            currentDatamodelList.currentIndex += Math.min(currentDatamodelList.maxIndex - currentDatamodelList.currentIndex, Editor.PATTERN_COUNT)
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

                datamodelScrollButtons as MutableList
                datamodelScrollButtons += upButton
                datamodelScrollButtons += downButton
                patternAreaStage.elements.addAll(datamodelScrollButtons)

                val labelCount = Editor.PATTERN_COUNT
                val height = 1f / labelCount

                patternAreaArrowLabel = TextLabel(borderedPalette, patternAreaStage, patternAreaStage).apply {
                    this.location.set(
                            screenX = padding2,
                            screenWidth = patternAreaStage.percentageOfWidth(
                                    Editor.ICON_SIZE),
                            screenHeight = height,
                            screenY = 1f - (height * (1 + (labelCount / 2)))
                                     )
                    this.isLocalizationKey = false
                    this.textAlign = Align.center
                    this.textWrapping = false
                    this.text = Editor.ARROWS[4]
                }
                patternAreaStage.elements += patternAreaArrowLabel
                baseBpmLabel = TextLabel(borderedPalette, patternAreaStage, patternAreaStage).apply {
                    this.location.set(
                            screenX = padding2 / 2,
                            screenWidth = patternAreaStage.percentageOfWidth(Editor.ICON_SIZE) + padding2,
                            screenHeight = 0.05f
                                     )
                    this.location.set(screenY = 0.5f + 0.1f)
                    this.isLocalizationKey = false
                    this.textAlign = Align.center or Align.bottom
                    this.textWrapping = false
                    this.text = ""
                    this.fontScaleMultiplier = 0.5f
                }
                patternAreaStage.elements += baseBpmLabel
                bottomBaseBpmLabel = TextLabel(borderedPalette, patternAreaStage, patternAreaStage).apply {
                    this.location.set(
                            screenX = padding2 / 2,
                            screenWidth = patternAreaStage.percentageOfWidth(Editor.ICON_SIZE) + padding2,
                            screenHeight = 0.05f
                                     )
                    this.location.set(screenY = 0.5f - 0.1f - this.location.screenHeight)
                    this.isLocalizationKey = false
                    this.textAlign = Align.center or Align.bottom
                    this.textWrapping = false
                    this.text = ""
                    this.fontScaleMultiplier = 0.5f
                }
                patternAreaStage.elements += bottomBaseBpmLabel

                patternPreviewButton = PatternPreviewButton(editor, borderedPalette, patternAreaStage,
                                                            patternAreaStage).apply {
                    this.location.set(
                            screenWidth = patternAreaStage.percentageOfWidth(
                                    Editor.ICON_SIZE),
                            screenHeight = height,
                            screenY = 1f - (height * (1 + (labelCount / 2)))
                                     )
                    this.location.set(screenX = 1f - this.location.screenWidth)
                }
                patternAreaStage.elements += patternPreviewButton
                editStoredPatternButton = EditStoredPatternButton(editor, this, borderedPalette, patternAreaStage, patternAreaStage).apply {
                    this.location.set(patternPreviewButton.location.screenX, patternPreviewButton.location.screenY, patternPreviewButton.location.screenWidth, patternPreviewButton.location.screenHeight)
                    this.visible = false
                }
                patternAreaStage.elements += editStoredPatternButton

                patternAreaStage.elements += pickerDisplay.apply {
                    this.location.set(
                            screenHeight = 1f,
                            screenY = 0f,
                            screenX = upButton.location.screenX + upButton.location.screenWidth +
                                    padding2
                                     )
                    this.location.set(
                            screenWidth = 1f - this.location.screenX - patternPreviewButton.location.screenWidth)
                }

            }
        }

        // Minimap area
        run minimap@{
            minimapBarStage.updatePositions()
            filterButtons as MutableList

            val buttonWidth: Float = minimapBarStage.percentageOfWidth(
                    Editor.ICON_SIZE)
            val buttonHeight: Float = 1f

            Series.VALUES.forEachIndexed { index, series ->
                val filter: Filter = SeriesFilter.allSeriesFilters[series]
                        ?: error("Series filter not found: $series")
                val tmp: FilterButton = FilterButton(filter, series.localization,
                                                     palette, minimapBarStage, minimapBarStage).apply {
                    this.location.set(
                            screenWidth = buttonWidth,
                            screenHeight = buttonHeight,
                            screenX = index * buttonWidth
                                     )
                    this.label.image = TextureRegion(AssetRegistry.get<Texture>(series.textureId))
                }
                filterButtons += tmp
            }
            filterButtons += FilterButton(CustomFilter(), "editor.customSfx", palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(
                        screenWidth = buttonWidth,
                        screenHeight = buttonHeight,
                        screenX = filterButtons.size * buttonWidth
                                 )
                this.label.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_tab_custom"))
            }
            filterButtons += FilterButton(recentsFilter, "editor.recents",
                                          palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(
                        screenWidth = buttonWidth,
                        screenHeight = buttonHeight,
                        screenX = filterButtons.size * buttonWidth
                                 )
                this.label.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_tab_recents"))
            }
            filterButtons += FilterButton(favouritesFilter, "editor.favourites",
                                          palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(
                        screenWidth = buttonWidth,
                        screenHeight = buttonHeight,
                        screenX = filterButtons.size * buttonWidth
                                 )
                this.label.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_tab_favourites"))
            }
            filterButtons += FilterButton(storedPatternsFilter, "editor.storedPatterns",
                                          palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(
                        screenWidth = buttonWidth,
                        screenHeight = buttonHeight,
                        screenX = filterButtons.size * buttonWidth
                                 )
                this.label.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_tab_stored_patterns"))
            }
            minimapBarStage.elements.addAll(filterButtons)

            val lastSeriesButton = filterButtons.last()

            val searchBarX = lastSeriesButton.location.screenX + lastSeriesButton.location.screenWidth
            val searchBarWidth = 0.5f - searchBarX
            minimapBarStage.updatePositions()
            searchBar = SearchBar(searchBarWidth,
                                  editor, this, palette, minimapBarStage, camera).apply {
                this.location.set(screenX = searchBarX,
                                  screenHeight = 1f)
                this.location.set(screenWidth = searchBarWidth)
            }
            minimapBarStage.elements += searchBar
            minimapBarStage.elements += ColourPane(minimapBarStage, minimapBarStage).apply {
                this.colour.set(1f, 1f, 1f, 1f)
                this.location.set(screenX = searchBarX, screenHeight = 1f, screenWidth = 0f, screenY = 0f,
                                  pixelX = -1f, pixelWidth = 1f)
            }

            toolButtons as MutableList
            Tool.VALUES.forEachIndexed { index, tool ->
                toolButtons += ToolButton(tool, palette, minimapBarStage, minimapBarStage) { x, y ->
                    if (editor.clickOccupation == ClickOccupation.None) {
                        editor.currentTool = tool
                        updateSelected()
                    }
                }.apply {
                    this.location.set(
                            screenWidth = buttonWidth,
                            screenHeight = buttonHeight,
                            screenX = 1f - Tool.VALUES.size * buttonWidth + index * buttonWidth
                                     )
                    this.background = true
                }
            }
            minimapBarStage.elements.addAll(toolButtons)
            val idealMinimapWidth = 0.5f - Tool.VALUES.size * buttonWidth
            minimap = Minimap(editor, palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(screenX = 0.5f + buttonWidth / 2, screenWidth = idealMinimapWidth - buttonWidth)
            }
            minimapBarStage.elements += minimap

            minimapBarStage.elements += PanButton(editor, true, palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(screenWidth = buttonWidth / 2, screenHeight = buttonHeight)
                this.location.set(screenX = 0.5f)
            }
            minimapBarStage.elements += PanButton(editor, false, palette, minimapBarStage, minimapBarStage).apply {
                this.location.set(screenWidth = buttonWidth / 2, screenHeight = buttonHeight)
                this.location.set(screenX = 0.5f + idealMinimapWidth - buttonWidth / 2)
            }

            minimapBarStage.elements += ColourPane(minimapBarStage, minimapBarStage).apply {
                this.colour.set(1f, 1f, 1f, 1f)
                this.location.set(screenX = 0.5f, screenHeight = 1f, screenWidth = 0f, screenY = 0f,
                                  pixelX = -0.5f, pixelWidth = 1f)
            }
        }

        // Button bar / Toolbar
        run buttonBar@{
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

            playButton = PlaybackButton(PlayState.PLAYING, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size, screenHeight = 1f)
                this.location.set(screenX = 0.5f - size / 2)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_play"))
                    this.tint = Color(0f, 0.5f, 0.055f, 1f)
                })
            }
            pauseButton = PlaybackButton(PlayState.PAUSED, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size, screenHeight = 1f)
                this.location.set(screenX = 0.5f - size / 2 - size - padding)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_pause"))
                    this.tint = Color(0.75f, 0.75f, 0.25f, 1f)
                })
            }
            stopButton = PlaybackButton(PlayState.STOPPED, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size, screenHeight = 1f)
                this.location.set(screenX = 0.5f - size / 2 + size + padding)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_stop"))
                    this.tint = Color(242 / 255f, 0.0525f, 0.0525f, 1f)
                })
            }
            buttonBarStage.elements += playButton
            buttonBarStage.elements += pauseButton
            buttonBarStage.elements += stopButton

            buttonBarStage.elements +=
                    IOButton(editor, "newRemix", "screen.new.title", palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_new_button"))
                        })
                    }
            buttonBarStage.elements +=
                    IOButton(editor, "openRemix", "screen.open.title", palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size,
                                          screenX = size + padding)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_load_button"))
                        })
                    }
            buttonBarStage.elements +=
                    IOButton(editor, "saveRemix", "screen.save.title", palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size,
                                          screenX = size * 2 + padding * 2)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_save_button"))
                        })
                    }
            buttonBarStage.elements +=
                    ExportButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = size, screenX = size * 3 + padding * 3)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_export"))
                        })
                    }
            buttonBarStage.elements += UndoRedoButton(editor, true, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = size * 4 + padding * 4)
            }
            buttonBarStage.elements += UndoRedoButton(editor, false, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = size * 5 + padding * 5)
            }
            buttonBarStage.elements += MusicButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = size * 6 + padding * 6)
            }
            buttonBarStage.elements += MetronomeButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = size * 7 + padding * 7)
            }
            buttonBarStage.elements += TapalongToggleButton(editor, this@EditorStage, palette, buttonBarStage,
                                                            buttonBarStage).apply {
                this.location.set(screenWidth = size, screenX = size * 8 + padding * 8)
            }
            buttonBarStage.elements += ScrollModeButton(editor, palette, buttonBarStage,
                                                        buttonBarStage).apply {
                this.location.set(screenWidth = size, screenX = size * 9 + padding * 9)
            }
            buttonBarStage.elements += TrackChangeButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size, screenX = size * 10 + padding * 10)
            }
            buttonBarStage.elements += SelectionToJSONButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size * 5 - padding * 2, screenX = size * 12 + padding * 11)
            }

            // right aligned
            // info button
            buttonBarStage.elements += InfoButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - size)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_info_button"))
                })
            }
            newsButton = NewsButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 2 + padding))
            }
            buttonBarStage.elements += newsButton
            // language button
            langButton = LangButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 3 + padding * 2))
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_language"))
                })
            }
            buttonBarStage.elements += langButton
            buttonBarStage.elements += FullscreenButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 4 + padding * 3))
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_fullscreen"))
                })
            }
            buttonBarStage.elements += ResetWindowButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 5 + padding * 4))
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_resetwindow"))
                })
            }
            buttonBarStage.elements += ThemeButton(editor, this, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 6 + padding * 5))
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_palette"))
                })
                this.enabled = true
            }
            buttonBarStage.elements += ViewButton(editor, this, palette, buttonBarStage,
                                                  buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 7 + padding * 6))
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_views"))
                })
            }
            buttonBarStage.elements += PresentationModeButton(editor, this@EditorStage, palette, buttonBarStage,
                                                              buttonBarStage).apply {
                this.location.set(screenWidth = size,
                                  screenX = 1f - (size * 8 + padding * 7))
            }
            buttonBarStage.elements += SnapButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size * 3,
                                  screenX = 1f - (size * 11 + padding * 8))
            }
            jumpToField = JumpToField(editor, palette, buttonBarStage, buttonBarStage).apply {
                this.location.set(screenWidth = size * 4,
                                  screenX = 1f - (size * 15 + padding * 9))
                this.textAlign = Align.center
                this.background = true
            }
            buttonBarStage.elements += jumpToField
            buttonBarStage.elements += CopyGamesUsedButton(editor, palette, buttonBarStage, buttonBarStage).apply {
                val endX = 1f - (size * 15 + padding * 10)
                this.location.set(screenX = 0.5f + (size * 1.5f + padding * 2))
                this.location.set(screenWidth = endX - this.location.screenX)
            }
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
                        this.addLabel(1, selectedLabel)
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

    open inner class GameButton(val x: Int, val y: Int, val isVariant: Boolean,
                                palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>,
                                f: SelectableButton.(Float, Float) -> Unit)
        : SelectableButton(palette, parent, stage, f), HasHoverText {

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

        override fun getHoverText(): String {
            val game = game
            if (game != null) {
                return (if (if (isVariant) game.isFavourited else game.gameGroup.isFavourited) "[YELLOW]★[] " else "") +
                        (if (game.isCustom) "[CYAN]★[]" else "") +
                        (if (isVariant) game.name else game.gameGroup.name) + "\n[LIGHT_GRAY]${Localization["editor.favouriteToggle"]}[]" +
                        if (ModdingUtils.moddingToolsEnabled && (isVariant || isSingleInGameGroup())) {
                            GameRegistry.moddingMetadata.currentData.joinToStringFromData(game, null).takeIf { it.isNotEmpty() }?.let { "\n$it" }
                        } else ("")
            }
            return ""
        }

        fun isSingleInGameGroup(): Boolean {
            val filter = editor.pickerSelection.filter
            val game = game
                    ?: return false
            return isVariant && !filter.areGroupsEmpty && game.gameGroup.games.size == 1
        }

        fun isFavourited(): Boolean {
            val game = game
                    ?: return false
            return if (isVariant)
                (if (isSingleInGameGroup())
                    game.gameGroup.isFavourited
                else game.isFavourited)
            else game.gameGroup.isFavourited
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)
            val game = game
            if (visible && game != null) {
                // toggle
                val wasFavourited = isFavourited()
                if (isVariant) {
                    if (isSingleInGameGroup()) {
                        GameMetadata.setFavourited(game.gameGroup, !wasFavourited)
                    } else {
                        GameMetadata.setFavourited(game, !wasFavourited)
                    }
                } else {
                    GameMetadata.setFavourited(game.gameGroup, !wasFavourited)
                }
                GameMetadata.persist()

                favouritesFilter.shouldUpdate = true
                updateSelected()
            }
        }

        override val selectedLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.image = selectorRegion
        }
        val favouriteLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.image = favouriteTagRegion
            this.tint.set(Color.YELLOW)
        }

    }

    open inner class FilterButton(val filter: Filter, val localization: String,
                                  palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
        : SelectableButton(palette, parent, stage, { _, _ -> }), HasHoverText {

        override fun getHoverText(): String {
            return Localization[localization]
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            gameSwitchBack = createQuickSwitch() ?: gameSwitchBack
            super.onLeftClick(xPercent, yPercent)
            editor.pickerSelection.filter = filter
            updateSelected()
        }

        override val selectedLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.image = selectorRegionSeries
        }
    }

    open inner class PlaybackButton(val type: PlayState, palette: UIPalette, parent: UIElement<EditorScreen>,
                                    stage: Stage<EditorScreen>)
        : Button<EditorScreen>(palette, parent, stage), HasHoverText {

        private fun updateEnabledness() {
            this.enabled = false
            when (editor.remix.playState) {
                PlayState.STOPPED -> if (type == PlayState.PLAYING) enabled = true
                PlayState.PAUSED -> if (type != PlayState.PAUSED) enabled = true
                PlayState.PLAYING -> if (type != PlayState.PLAYING) enabled = true
            }
        }

        override fun getHoverText(): String {
            return Localization[when (type) {
                PlayState.STOPPED -> "editor.stop"
                PlayState.PAUSED -> "editor.pause"
                PlayState.PLAYING -> "editor.play"
            }]
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            editor.remix.playState = type
        }

        override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            updateEnabledness()
            super.render(screen, batch, shapeRenderer)
        }
    }

    open inner class ToolButton(val tool: Tool,
                                palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>,
                                onLeftClickFunc: SelectableButton.(Float, Float) -> Unit)
        : SelectableButton(palette, parent, stage, onLeftClickFunc), HasHoverText {
        override val selectedLabel: ImageLabel<EditorScreen> = ImageLabel(palette, this, stage).apply {
            this.image = selectorRegionSeries
        }

        private val keyText: String by lazy {
            val moddedIndex = tool.index + 1
            when (moddedIndex) {
                10 -> " [LIGHT_GRAY][[0][]"
                in 1..9 -> " [LIGHT_GRAY][[$moddedIndex][]"
                else -> ""
            } + (if (tool.keybinds.isNotEmpty()) " [LIGHT_GRAY]${tool.keybinds.joinToString(" ") { "[[$it]" }}[]" else "")
        }

        override fun getHoverText(): String {
            return Localization[tool.nameId] + keyText
        }

        init {
            label.image = TextureRegion(tool.texture)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            editor.updateMessageLabel()
        }
    }
}
