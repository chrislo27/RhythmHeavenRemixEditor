package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.RemixRecovery
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import kotlin.system.exitProcess


class SFXDBLoadingScreen(main: RHRE3Application, val nextScreen: () -> ToolboksScreen<*, *>? = {null})
    : ToolboksScreen<RHRE3Application, SFXDBLoadingScreen>(main) {

    companion object {
        val DEF_AFTER_LOAD_SCREEN: String = "editor"
    }

    private var backingData: SFXDatabase.SFXDBData? = null

    override val stage: Stage<SFXDBLoadingScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val gameIcon: ImageLabel<SFXDBLoadingScreen>
    private val gameTitle: TextLabel<SFXDBLoadingScreen>
    private val texRegion: TextureRegion = TextureRegion()

    init {
        stage as GenericStage
        stage.updatePositions()
        stage.titleLabel.setText("screen.sfxdbLoading.title", isLocalization = true)
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))

        gameIcon = ImageLabel(main.uiPalette, stage.centreStage, stage.centreStage)
        gameIcon.apply {
            this.alignment = Align.bottom or Align.center
            this@SFXDBLoadingScreen.stage.centreStage.updatePositions()
            val width = 64f / this@SFXDBLoadingScreen.stage.centreStage.location.realWidth
            val height = 64f / this@SFXDBLoadingScreen.stage.centreStage.location.realHeight
            this.location.set(screenX = -width / 2f, screenWidth = width, screenHeight = height,
                              screenY = 0.5f - width / 2)
        }

        gameTitle = TextLabel(main.uiPalette, stage.centreStage, stage.centreStage)
        gameTitle.apply {
            this.alignment = Align.bottomLeft
            this.location.set(screenHeight = 0.5f, pixelY = -32f)
            this.textAlign = Align.top or Align.center
            this.isLocalizationKey = false
        }

        stage.centreStage.elements.apply {
            add(gameIcon)
            add(gameTitle)
        }

        stage.bottomStage.elements += Button(main.uiPalette, stage.bottomStage, stage.bottomStage).apply {
            this.leftClickAction = {_, _ ->
                Gdx.net.openURI(RHRE3.DATABASE_RELEASES)
            }
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.database"
            })
            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        stage.updatePositions()
    }

    override fun render(delta: Float) {
        super.render(delta)

        val dbData = backingData ?: return

        val progress: Float = try {
            dbData.loadFor(1 / 60f)
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
        val game: Game? = dbData.gameMap[dbData.lastLoadedID]

        val texture = game?.icon
        if (texture == null) {
            gameIcon.image = null
        } else {
            texRegion.setRegion(texture)
            gameIcon.image = texRegion
        }
        gameTitle.text = "${game?.name}\n[GRAY]${game?.id}[]\n[LIGHT_GRAY]${Localization["screen.sfxdbLoading.objects", game?.objects?.size]}[]"

        if (progress >= 1f && !Toolboks.debugMode) {
            val next = nextScreen()
            main.screen = if (next == null) {
                val normalScreen = if (RemixRecovery.shouldBeRecovered()) "recoverRemix" else DEF_AFTER_LOAD_SCREEN
                val nextScreen = if (!main.githubVersion.isUnknown && RHRE3.VERSION < main.githubVersion) {
                    ScreenRegistry.getNonNullAsType<EditorVersionScreen>("editorVersion").also {
                        it.isBeginning = true to ScreenRegistry[normalScreen]
                    }
                } else {
                    ScreenRegistry[normalScreen]
                }
                val possibleEvent: Screen? = EventScreen.getPossibleEvent(main, nextScreen)
                possibleEvent ?: (nextScreen)
            } else next
        }
    }

    override fun show() {
        super.show()
        backingData = SFXDatabase.initialize()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}