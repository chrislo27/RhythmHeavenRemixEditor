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
import io.github.chrislo27.rhre3.sfxdb.GameRegistry
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class RegistryLoadingScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, RegistryLoadingScreen>(main) {

    companion object {
        val DEF_AFTER_LOAD_SCREEN: String = "editor"
    }

    private var registryData: GameRegistry.RegistryData? = null

    override val stage: Stage<RegistryLoadingScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val gameIcon: ImageLabel<RegistryLoadingScreen>
    private val gameTitle: TextLabel<RegistryLoadingScreen>
    private val texRegion: TextureRegion = TextureRegion()

    init {
        stage as GenericStage
        stage.updatePositions()
        stage.titleLabel.setText("screen.registry.title", isLocalization = true)
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))

        gameIcon = ImageLabel(main.uiPalette, stage.centreStage, stage.centreStage)
        gameIcon.apply {
            this.alignment = Align.bottom or Align.center
            this@RegistryLoadingScreen.stage.centreStage.updatePositions()
            val width = 64f / this@RegistryLoadingScreen.stage.centreStage.location.realWidth
            val height = 64f / this@RegistryLoadingScreen.stage.centreStage.location.realHeight
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

        stage.bottomStage.elements += object : Button<RegistryLoadingScreen>(main.uiPalette, stage.bottomStage,
                                                                             stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(RHRE3.DATABASE_RELEASES)
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.database"
//                this.fontScaleMultiplier = 0.9f
            })

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        stage.updatePositions()
    }

    override fun render(delta: Float) {
        super.render(delta)

        val registryData = registryData ?: return

        val progress: Float = try {
            registryData.loadFor(1 / 60f)
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(1)
            throw e // should never happen since System.exit doesn't return
        }
//        println("Loaded ${registryData.gameMap.size - numLoaded} this frame in ${(System.nanoTime() - nano) / 1_000_000.0} ms")
        val game: Game? = registryData.gameMap[registryData.lastLoadedID]

        val texture = game?.icon
        if (texture == null) {
            gameIcon.image = null
        } else {
            texRegion.setRegion(texture)
            gameIcon.image = texRegion
        }
        gameTitle.text = "${game?.name}\n[GRAY]${game?.id}[]"

        if (progress >= 1f && !Toolboks.debugMode) {
            val normalScreen = if (RemixRecovery.shouldBeRecovered()) "recoverRemix" else DEF_AFTER_LOAD_SCREEN
            val nextScreen = if (!main.githubVersion.isUnknown && RHRE3.VERSION < main.githubVersion) {
                ScreenRegistry.getNonNullAsType<EditorVersionScreen>("editorVersion").also {
                    it.isBeginning = true to ScreenRegistry[normalScreen]
                }
            } else {
                ScreenRegistry[normalScreen]
            }
            val possibleEvent: Screen? = EventScreen.getPossibleEvent(main, nextScreen)
            main.screen = possibleEvent ?: (nextScreen)
        }
    }

    override fun show() {
        super.show()
        registryData = GameRegistry.initialize()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}