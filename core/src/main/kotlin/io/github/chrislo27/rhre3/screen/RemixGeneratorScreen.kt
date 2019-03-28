package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.remixgen.RemixGenerator
import io.github.chrislo27.rhre3.remixgen.RemixGeneratorSettings
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel


class RemixGeneratorScreen(main: RHRE3Application, val editor: Editor)
    : ToolboksScreen<RHRE3Application, RemixGeneratorScreen>(main) {

    private val remix: Remix get() = editor.remix

    override val stage: GenericStage<RemixGeneratorScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val seedField: TextField<RemixGeneratorScreen>
    private val rootBeatLabel: TextLabel<RemixGeneratorScreen>
    private val generateButton: Button<RemixGeneratorScreen>

    private var settings: RemixGeneratorSettings = RemixGeneratorSettings(System.currentTimeMillis())
        set(value) {
            field = value
            generator = RemixGenerator(remix, settings)
            Gdx.app.postRunnable {
                updateLabels()
            }
        }
    private var generator: RemixGenerator = RemixGenerator(remix, settings)

    init {
        val palette = main.uiPalette
        stage.titleLabel.text = "screen.remixGen.title"
//        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_inspections_big"))
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        val labelWidth = 0.2f
        val labelPadding = 0.025f
        val labelHeight = 0.125f
        val fieldHeight = 0.1f

        rootBeatLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.text = ""
            this.location.set(screenHeight = labelHeight)
        }
        stage.centreStage.elements += rootBeatLabel
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.remixGen.seed"
            this.textAlign = Align.bottomLeft
            this.textColor = Color.LIGHT_GRAY
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenHeight = labelHeight, screenY = 0.8f - labelHeight * -0.5f)
        }
        seedField = object : TextField<RemixGeneratorScreen>(palette, stage.centreStage, stage.centreStage) {
            override fun onTextChange(oldText: String) {
                val longSeed = text.trim().toLongOrNull()
                settings = settings.copy(seed = longSeed ?: text.hashCode().toLong())
            }
        }.apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenHeight = fieldHeight, screenY = 0.8f - labelHeight * 0.5f)
            this.background = true
        }
        stage.centreStage.elements += seedField
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.text = "screen.remixGen.seed.empty"
            this.textAlign = Align.left
            this.textColor = Color.LIGHT_GRAY
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenHeight = labelHeight, screenY = 0.8f - labelHeight * 1.5f)
        }

        generateButton = object : Button<RemixGeneratorScreen>(palette, stage.bottomStage, stage.bottomStage){
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                this.enabled = false
                generator.generate()
                main.screen = ScreenRegistry["editor"]
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.text = "screen.remixGen.generate"
                this.textWrapping = false
            })
            this.location.set(screenX = 0.2f, screenWidth = 0.6f)
        }
        stage.bottomStage.elements += generateButton

        updateLabels()
    }

    fun updateLabels() {
        generateButton.enabled = generator.canGenerate
        rootBeatLabel.text = if (generator.canGenerate) Localization["screen.remixGen.rootBeat", generator.rootBeat.toString()] else {
            if (remix.duration == Float.POSITIVE_INFINITY) {
                Localization["screen.remixGen.missingEndRemix", GameRegistry.data.objectMap[GameRegistry.END_REMIX_ENTITY_ID]?.name]
            } else {
                Localization["screen.remixGen.alreadyGenerated"]
            }
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}