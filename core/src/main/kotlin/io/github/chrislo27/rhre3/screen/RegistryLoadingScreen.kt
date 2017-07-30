package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Game
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class RegistryLoadingScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, RegistryLoadingScreen>(main) {

    private var registryData: GameRegistry.RegistryData? = null

    override val stage: Stage<RegistryLoadingScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val gameIcon: ImageLabel<RegistryLoadingScreen>
    private val gameTitle: TextLabel<RegistryLoadingScreen>
    private val texRegion: TextureRegion = TextureRegion()

    init {
        stage as GenericStage
        stage.updatePositions()
        stage.titleLabel.setText("screen.registry.title", isLocalization = true)

        gameIcon = ImageLabel(main.uiPalette, stage.centreStage, stage.centreStage)
        gameIcon.apply {
            this.alignment = Align.bottom or Align.center
            this.location.set(screenWidth = 0f, screenHeight = 0f, screenY = 0.5f, pixelX = -32f, pixelWidth = 64f, pixelHeight = 64f)
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

        stage.updatePositions()

        stage.centreStage.updatePositions()

    }

    override fun render(delta: Float) {
        super.render(delta)
        val registryData = registryData ?: return

        val loadOneAtATime = false

        val progress: Float = if (loadOneAtATime) registryData.loadOne() else registryData.loadFor(Math.min(delta, 2 / 60f))
        val game: Game? = registryData.gameMap[registryData.lastLoadedID]

        val texture = game?.icon
        if (texture == null) {
            gameIcon.image = null
        } else {
            texRegion.setRegion(texture)
            gameIcon.image = texRegion
        }
        gameTitle.text = "${game?.name}\n[GRAY]${game?.id}[]"

        if (progress >= 1f) {
//            main.screen = ScreenRegistry["editor"]
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