package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.version.Version


class EditorVersionScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, EditorVersionScreen>(main) {

    override val stage: Stage<EditorVersionScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val label: TextLabel<EditorVersionScreen>

    var isBeginning: Boolean = false
    private var timeOnScreen = 0f

    init {
        stage as GenericStage
        val palette = stage.palette

        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_update"))
        }
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull(if (isBeginning) "editor" else "info")
            isBeginning = false
        }

        stage.bottomStage.elements += object : Button<EditorVersionScreen>(palette, stage.bottomStage,
                                                                           stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(RHRE3.GITHUB_RELEASES)
                val stage = stage as GenericStage
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
            private var lastGithubVer: Version? = main.githubVersion

            override fun render(screen: EditorVersionScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (lastGithubVer != main.githubVersion || text == "") {
                    lastGithubVer = main.githubVersion

                    val ghVer = lastGithubVer
                    val currentVer: String = (if (ghVer == null)
                        "[LIGHT_GRAY]"
                    else if (ghVer <= RHRE3.VERSION)
                        "[CYAN]"
                    else
                        "[ORANGE]") + "${RHRE3.VERSION}[]"
                    val onlineVer: String = (if (ghVer == null)
                        "[LIGHT_GRAY]...[]"
                    else
                        "[CYAN]$ghVer[]")
                    val humanFriendly: String =
                            (if (ghVer == null)
                                "screen.version.checking"
                            else if (ghVer <= RHRE3.VERSION)
                                "screen.version.upToDate"
                            else
                                "screen.version.outOfDate")

                    text = Localization["screen.version.label", currentVer, onlineVer, Localization[humanFriendly]]
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

        if (timeOnScreen >= 3f) {
            stage as GenericStage
            stage.backButton.enabled = true
        }
    }

    override fun show() {
        super.show()
        stage as GenericStage
        stage.titleLabel.text = "screen.version.title${MathUtils.random(0, 5)}"
        timeOnScreen = 0f
        if (isBeginning) {
            stage.backButton.enabled = false
        }
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}