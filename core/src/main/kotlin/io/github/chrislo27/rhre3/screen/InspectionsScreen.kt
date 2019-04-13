package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.inspections.InspectionTab
import io.github.chrislo27.rhre3.inspections.LangInspectionTab
import io.github.chrislo27.rhre3.inspections.SeriesInspectionTab
import io.github.chrislo27.rhre3.inspections.StatsTab
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask


class InspectionsScreen(main: RHRE3Application, val remix: Remix)
    : ToolboksScreen<RHRE3Application, InspectionsScreen>(main) {

    companion object {
        val GLYPHS = listOf("◉", "○", "\uE149", "\uE14A")
    }

    private val tabs: List<InspectionTab> = listOf(StatsTab(remix), SeriesInspectionTab(remix), LangInspectionTab(remix))
    private var tabIndex: Int = 0
    private var tabLerp: Float = tabIndex.toFloat()

    override val stage: GenericStage<InspectionsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val leftButton: PageButton
    private val rightButton: PageButton
    private val titleLabel: TextLabel<InspectionsScreen>
    private val dotsLabel: TextLabel<InspectionsScreen>
    private val area: ColourPane<InspectionsScreen>

    init {
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_inspections_big"))
        stage.titleLabel.text = "screen.inspections.title"
        stage.onBackButtonClick = { main.screen = ScreenRegistry["editor"] }
        stage.backButton.visible = true

        val palette = main.uiPalette

        val topBarCentre = 0.4f
        val buttonHeight = 0.1f
        leftButton = PageButton(palette, stage.centreStage, stage.centreStage, true).apply {
            location.set(screenX = 0f, screenWidth = (1f - topBarCentre) / 2f, screenHeight = buttonHeight, screenY = 1f - buttonHeight)
        }
        stage.centreStage.elements += leftButton
        rightButton = PageButton(palette, stage.centreStage, stage.centreStage, false).apply {
            location.set(screenWidth = (1f - topBarCentre) / 2f, screenHeight = buttonHeight, screenY = 1f - buttonHeight)
            location.set(screenX = 1f - location.screenWidth)
        }
        stage.centreStage.elements += rightButton
        titleLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            location.set(screenX = 0.5f - topBarCentre / 2f, screenWidth = topBarCentre, screenHeight = buttonHeight, screenY = 1f - buttonHeight)
        }
        stage.centreStage.elements += titleLabel
        dotsLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.fontScaleMultiplier = 0.5f
            location.set(screenX = 0.5f - topBarCentre / 2f, screenWidth = topBarCentre, screenHeight = buttonHeight / 2, screenY = 1f - buttonHeight * 1.5f)
            // FIXME
            this.text = "${GLYPHS[1]}  ${GLYPHS[0]}  ${GLYPHS[1]}"
        }
        stage.centreStage.elements += dotsLabel
        area = ColourPane(stage.centreStage, stage.centreStage).apply {
            this.colour.a = 0f
            this.location.set(screenHeight = dotsLabel.location.screenY)
        }
        stage.centreStage.elements += area

        updateLabels()
    }

    override fun render(delta: Float) {
        super.render(delta)
        val batch = main.batch
        val shapeRenderer = main.shapeRenderer

        val camera = stage.camera
        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined
        batch.begin()
        shapeRenderer.prepareStencilMask(batch) {
            begin(ShapeRenderer.ShapeType.Filled)
            rect(area.location.realX, area.location.realY, area.location.realWidth, area.location.realHeight)
            end()
        }.useStencilMask {
            val roundedTab = tabLerp.toInt()
            val oldX = camera.position.x
            val oldY = camera.position.y
            camera.position.y = area.location.realY
            for (i in (roundedTab - 1).coerceAtLeast(0)..(roundedTab + 1).coerceAtMost(tabs.size - 1)) {
                val tab = tabs[i]
                camera.position.x = (tabLerp - i + 1) * area.location.realWidth
                camera.update()
                batch.projectionMatrix = camera.combined
                val w = area.location.realWidth
                val h = area.location.realHeight
                tab.render(main, batch, w, h)
            }
            camera.position.set(oldX, oldY, 0f)
            camera.update()
        }
        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
        shapeRenderer.projectionMatrix = main.defaultCamera.combined
    }

    override fun renderUpdate() {
        super.renderUpdate()

        tabLerp = MathUtils.lerp(tabLerp, tabIndex.toFloat(), (7f * Gdx.graphics.deltaTime).coerceAtMost(1f)).coerceIn(tabIndex - 1f, tabIndex + 1f)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.enabled && stage.backButton.visible) {
            stage.onBackButtonClick()
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) && leftButton.visible && leftButton.enabled) {
            leftButton.onLeftClick(0f, 0f)
        }
        if ((Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) && rightButton.visible && rightButton.enabled) {
            rightButton.onLeftClick(0f, 0f)
        }
    }

    private fun updateLabels() {
        if (tabs.isEmpty()) return
        val index = tabIndex
        dotsLabel.text = tabs.indices.joinToString(separator = "  ") { GLYPHS[if (it == index) 0 else 1] }
        leftButton.visible = true
        rightButton.visible = true
        if (index == 0) {
            leftButton.visible = false
        }
        if (index == tabs.size - 1) {
            rightButton.visible = false
        }
        if (index != tabs.size - 1) {
            rightButton.textLabel.text = Localization[tabs[index + 1].nameKey]
        }
        if (index != 0) {
            leftButton.textLabel.text = Localization[tabs[index - 1].nameKey]
        }
        titleLabel.text = Localization[tabs[index].nameKey]
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    inner class PageButton(palette: UIPalette, parent: UIElement<InspectionsScreen>, stage: Stage<InspectionsScreen>, val left: Boolean)
        : Button<InspectionsScreen>(palette, parent, stage) {

        val textLabel: TextLabel<InspectionsScreen>

        init {
            val portion = 0.15f
            val arrowLabel = TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = GLYPHS[if (left) 2 else 3]
            }
            textLabel = TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = ""
                this.fontScaleMultiplier = 0.85f
            }
            if (left) {
                arrowLabel.location.set(screenWidth = portion)
                textLabel.textAlign = Align.left
                textLabel.location.set(screenX = portion, screenWidth = 1f - portion)
            } else {
                textLabel.location.set(screenWidth = 1f - portion)
                textLabel.textAlign = Align.right
                arrowLabel.location.set(screenX = 1f - portion, screenWidth = portion)
            }

            addLabel(arrowLabel)
            addLabel(textLabel)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            if (left && tabIndex > 0) {
                tabIndex--
            } else if (!left && tabIndex < tabs.size - 1) {
                tabIndex++
            }
            updateLabels()
        }
    }

}