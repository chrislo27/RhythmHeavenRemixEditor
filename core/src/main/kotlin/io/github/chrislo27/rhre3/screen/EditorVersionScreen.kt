package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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


class EditorVersionScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, EditorVersionScreen>(main) {

    companion object {
        private val DEFAULT_BEGINNING: Pair<Boolean, ToolboksScreen<*, *>?> = false to null
    }

    override val stage: GenericStage<EditorVersionScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val label: TextLabel<EditorVersionScreen>

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

        stage.bottomStage.elements += object : Button<EditorVersionScreen>(palette, stage.bottomStage,
                                                                           stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(RHRE3.GITHUB_RELEASES)
                val stage: GenericStage<EditorVersionScreen> = this@EditorVersionScreen.stage
                stage.backButton.enabled = true
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.version.button"
            })

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        label = object : TextLabel<EditorVersionScreen>(palette, stage.centreStage, stage.centreStage) {
            private var lastGithubVer: Version = main.githubVersion

            override fun render(screen: EditorVersionScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (lastGithubVer != main.githubVersion || text == "") {
                    lastGithubVer = main.githubVersion

                    val ghVer = lastGithubVer
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
                    val humanFriendly: String =
                            (if (ghVer.isUnknown)
                                Localization["screen.version.checking"]
                            else if (ghVer <= RHRE3.VERSION)
                                Localization["screen.version.upToDate"]
                            else
                                Localization["screen.version.outOfDate",
                                        main.preferences.getInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 1)
                                                .coerceAtLeast(1)])
//                    val test = Localization["screen.version.outOfDate", 2]

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
        label.text = ""
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}