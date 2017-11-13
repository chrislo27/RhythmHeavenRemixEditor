package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.goat.GoatData
import io.github.chrislo27.rhre3.goat.GoatHat
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel


class GoatScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, GoatScreen>(main) {

    override val stage: Stage<GoatScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private lateinit var goat: GoatData
    private val goatPrefs: Preferences by lazy {
        Gdx.app.getPreferences("RHRE3_goat")
    }
    private val font: BitmapFont
        get() = main.defaultFont

    private val profileStage: Stage<GoatScreen>
    private val background: ImageLabel<GoatScreen>
    private val medal: ImageLabel<GoatScreen>
    private val face: ImageLabel<GoatScreen>
    private val hat: ImageLabel<GoatScreen>
    private val mask: ImageLabel<GoatScreen>
    private val name: TextField<GoatScreen>
    private val levelLabel: TextLabel<GoatScreen>
    private val foodLabel: TextLabel<GoatScreen>

    init {
        stage as GenericStage
        val palette = main.uiPalette

        stage.titleLabel.text = "Goat"
        stage.titleLabel.isLocalizationKey = false
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("goat_full"))
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        stage.centreStage.updatePositions()
        profileStage = object : Stage<GoatScreen>(stage.centreStage, stage.centreStage.camera) {

        }.apply {
            location.set(screenX = 0.25f, screenWidth = 0.5f)
            location.set(
                    screenHeight = location.screenWidth * 0.5f * stage.location.realWidth / stage.location.realHeight)
            location.set(screenY = 1f - location.screenHeight)
        }
        stage.centreStage.elements += profileStage

        background = ImageLabel(palette, profileStage, profileStage).apply {
            location.set(0f, 0f, 1f, 1f)
        }
        profileStage.elements += background

        val goatStage = object : Stage<GoatScreen>(profileStage, profileStage.camera) {
        }.apply {
            location.set(screenX = 160f / 256, screenY = 16f / 128, screenWidth = 96f / 256, screenHeight = 96f / 128f)
        }
        profileStage.elements += goatStage

        name = object : TextField<GoatScreen>(palette, profileStage, profileStage) {
            override fun onTextChange(oldText: String) {
                super.onTextChange(oldText)
                goat.name = text
            }

            override fun onEnterPressed(): Boolean {
                hasFocus = false
                persist()
                return true
            }
        }.apply {
            location.set(screenHeight = 0.125f)
            textAlign = Align.center
            background = true
        }
        profileStage.elements += name
        val labelPadding = 0.015625f
        levelLabel = object : TextLabel<GoatScreen>(palette, profileStage, profileStage) {
            override fun getFont(): BitmapFont {
                return main.defaultFontLarge
            }
        }.apply {
            this.isLocalizationKey = false
            this.fontScaleMultiplier = 0.75f
            this.background = true
            this.textAlign = Align.left
            this.location.set(screenX = labelPadding, screenY = 0.75f, screenWidth = 0.55f, screenHeight = 0.25f)
        }
        profileStage.elements += levelLabel
        foodLabel = object : TextLabel<GoatScreen>(palette, profileStage, profileStage) {

        }.apply {
            this.isLocalizationKey = false
            this.background = true
            this.textAlign = Align.left
            this.location.set(screenX = labelPadding, screenY = 0.75f - 0.125f, screenWidth = 0.55f, screenHeight = 0.125f)
        }
        profileStage.elements += foodLabel
        profileStage.elements += TextLabel(palette, profileStage, profileStage).apply {
            this.isLocalizationKey = false
            this.background = true
            this.location.set(screenWidth = labelPadding, screenHeight = foodLabel.location.screenHeight + levelLabel.location.screenHeight)
            this.location.set(screenY = 1f - location.screenHeight)
        }

        goatStage.elements += ImageLabel(palette, goatStage, goatStage).apply {
            image = TextureRegion(AssetRegistry.get<Texture>("goat_base"))
        }
        hat = ImageLabel(palette, goatStage, goatStage).apply {
            image = null
        }
        goatStage.elements += hat
        face = ImageLabel(palette, goatStage, goatStage).apply {
            image = null
        }
        goatStage.elements += face
        medal = ImageLabel(palette, goatStage, goatStage).apply {
            image = TextureRegion(AssetRegistry.get<Texture>("goat_medal"))
            visible = false
        }
        goatStage.elements += medal
        mask = ImageLabel(palette, goatStage, goatStage).apply {
            image = null
        }
        goatStage.elements += mask

    }

    private fun updateUI() {
        val hat = goat.hat
        val face = goat.face

        background.image = TextureRegion(AssetRegistry.get<Texture>("goat_bg_${goat.background.name}"))
        this.face.image = TextureRegion(AssetRegistry.get<Texture>("goat_face_${goat.face.name}"))

        val isFaceAbove = (hat != GoatHat.NONE && hat.head) || (face.isMask && (hat == GoatHat.NONE || !hat.helmet))
        this.hat.image = null
        this.mask.image = null
        this.medal.visible = goat.level >= 100
        (if (isFaceAbove) this.hat else this.mask).image = TextureRegion(
                AssetRegistry.get<Texture>("goat_hat_${goat.hat.name}"))

        name.text = goat.name
        name.hasFocus = false

        foodLabel.text = "${goat.food} / ${goat.foodForLevel} food"
        levelLabel.text = "LVL ${goat.level}"
    }

    private fun persist() {
        goat.saveToPreferences(goatPrefs)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            (stage as GenericStage).onBackButtonClick()
        }
    }

    override fun show() {
        super.show()
        goat = GoatData.createFromPreferences(goatPrefs)
        updateUI()
    }

    override fun hide() {
        super.hide()
        persist()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}