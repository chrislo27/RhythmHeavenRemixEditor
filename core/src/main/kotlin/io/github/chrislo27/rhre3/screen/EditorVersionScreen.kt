package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class EditorVersionScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, EditorVersionScreen>(main) {
    
    companion object {
        private val DEFAULT_BEGINNING: Pair<Boolean, ToolboksScreen<*, *>?> = false to null
    }
    
    private enum class State {
        CHECKING, GOOD, AVAILABLE;
    }
    
    override val stage: GenericStage<EditorVersionScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    
    private var state: State by Delegates.observable(State.CHECKING) { _, _, newState ->
        checkButton.enabled = newState != State.CHECKING
        longChangelogButton.visible = newState != State.AVAILABLE
        shortChangelogButton.visible = newState == State.AVAILABLE
        gotoUpdaterButton.visible = newState == State.AVAILABLE
    }
    private val label: TextLabel<EditorVersionScreen>
    private val checkButton: Button<EditorVersionScreen>
    private val longChangelogButton: Button<EditorVersionScreen>
    private val shortChangelogButton: Button<EditorVersionScreen>
    private val gotoUpdaterButton: Button<EditorVersionScreen>
    
    var isBeginning: Pair<Boolean, ToolboksScreen<*, *>?> = DEFAULT_BEGINNING
    private var timeOnScreen = 0f
    private var timeToStayOnScreen = 0f
    
    init {
        val palette = stage.palette
        
        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_update"))
        }
        stage.backButton.visible = true
        stage.backButton.tooltipTextIsLocalizationKey = true
        stage.onBackButtonClick = {
            main.screen = if (isBeginning.first) isBeginning.second else ScreenRegistry.getNonNull("info")
            isBeginning = DEFAULT_BEGINNING
        }
        
        longChangelogButton = Button(palette, stage.bottomStage, stage.bottomStage).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(RHRE3.GITHUB_RELEASES)
                val stage: GenericStage<EditorVersionScreen> = this@EditorVersionScreen.stage
                stage.backButton.enabled = true
            }
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.version.viewChangelog"
            })
    
            this.location.set(screenX = 0.175f, screenWidth = 0.65f)
            this.visible = true
        }
        stage.bottomStage.elements += longChangelogButton
        checkButton = Button(palette, stage.bottomStage, stage.bottomStage).apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "screen.version.checkForUpdates"
                this.fontScaleMultiplier = 0.9f
                this.textWrapping = false
            })
            this.location.set(screenX = 0.85f, screenWidth = 0.15f)
            this.leftClickAction = { _, _ ->
                if (this@EditorVersionScreen.state != State.CHECKING) {
                    this@EditorVersionScreen.state = State.CHECKING
                    enabled = false
                    GlobalScope.launch {
                        main.fetchGithubVersion()
                    }
                }
            }
            this.enabled = false
        }
        stage.bottomStage.elements += checkButton
        shortChangelogButton = Button(palette, stage.bottomStage, stage.bottomStage).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(RHRE3.GITHUB_RELEASES + "/latest")
                AnalyticsHandler.track("View Changelog", mapOf())
            }
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.fontScaleMultiplier = 0.9f
                this.text = "screen.version.viewChangelog.short"
            })
    
            this.location.set(screenX = 0.125f, screenWidth = 0.2f)
            this.visible = false
        }
        stage.bottomStage.elements += shortChangelogButton
        gotoUpdaterButton = Button(palette, stage.bottomStage, stage.bottomStage).apply {
            this.leftClickAction = { _, _ ->
                AnalyticsHandler.track("Run Auto-Updater", mapOf())
                main.screen = AutoUpdaterScreen(main)
            }
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.version.gotoUpdater"
                this.textColor = Color.CYAN.cpy()
            })
        
            this.location.set(screenX = 0.35f, screenWidth = 0.475f)
            this.visible = false
        }
        stage.bottomStage.elements += gotoUpdaterButton
        
        label = object : TextLabel<EditorVersionScreen>(palette, stage.centreStage, stage.centreStage) {
            private var lastGhVer: Version = main.githubVersion
            
            override fun render(screen: EditorVersionScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (lastGhVer != main.githubVersion || text.isEmpty()) {
                    lastGhVer = main.githubVersion
                    
                    val ghVer = main.githubVersion
                    val currentVer: String = (if (ghVer.isUnknown)
                        "[LIGHT_GRAY]"
                    else if (ghVer <= RHRE3.VERSION)
                        "[CYAN]"
                    else
                        "[ORANGE]") + "${RHRE3.VERSION}[]"
                    val onlineVer: String = (if (ghVer.isUnknown)
                        "[LIGHT_GRAY]...[]"
                    else
                        "[CYAN]$ghVer[]")
                    val newState: State =
                            if (ghVer.isUnknown)
                                State.CHECKING
                            else if (ghVer <= RHRE3.VERSION && !RHRE3.triggerUpdateScreen)
                                State.GOOD
                            else
                                State.AVAILABLE
                    val humanFriendly: String = when (newState) {
                        State.CHECKING -> Localization["screen.version.checking"]
                        State.GOOD -> Localization["screen.version.upToDate"]
                        State.AVAILABLE -> Localization["screen.version.outOfDate",
                                main.preferences.getInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 1)
                                        .coerceAtLeast(1)]
                    }
                    this@EditorVersionScreen.state = newState
                    text = Localization["screen.version.label", currentVer, onlineVer, humanFriendly]
                }
                super.render(screen, batch, shapeRenderer)
            }
        }.apply {
            this.isLocalizationKey = false
            this.textWrapping = true
        }
        stage.centreStage.elements += label
        
        stage.updatePositions()
    }
    
    override fun renderUpdate() {
        super.renderUpdate()
        
        timeOnScreen += Gdx.graphics.deltaTime
        
        if (timeOnScreen >= timeToStayOnScreen) {
            stage.backButton.enabled = true
            stage.backButton.tooltipText = null
            
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                stage.onBackButtonClick()
            }
        }
    }
    
    override fun show() {
        super.show()
        stage.titleLabel.text = "screen.version.title${MathUtils.random(0, 5)}"
        timeOnScreen = 0f
        if (isBeginning.first) {
            stage.backButton.enabled = false
            val timesSkipped = main.preferences.getInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 1).coerceAtLeast(1)
            val limitSkips = 5
            val minWaitTime = 2f
            val waitTimeAtLimit = 4f
            val waitTimeAfterLimit = 6.5f
            timeToStayOnScreen = if (timesSkipped <= limitSkips) ((waitTimeAtLimit / limitSkips) * timesSkipped).coerceAtLeast(minWaitTime) else waitTimeAfterLimit
            
            // Analytics
            AnalyticsHandler.track("Update Notification",
                                   mapOf(
                                           "timeToStayOnScreen" to timeToStayOnScreen,
                                           "timesSkipped" to timesSkipped
                                        ))
        }
        checkButton.visible = !isBeginning.first
        label.text = ""
    }
    
    override fun dispose() {
    }
    
    override fun tickUpdate() {
    }
}