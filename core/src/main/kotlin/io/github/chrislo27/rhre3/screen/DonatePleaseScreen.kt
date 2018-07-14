package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Checkbox
import io.github.chrislo27.toolboks.ui.TextLabel


class DonatePleaseScreen(main: RHRE3Application, val nextScreen: Screen?)
    : ToolboksScreen<RHRE3Application, DonatePleaseScreen>(main) {

    override val stage: GenericStage<DonatePleaseScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private var timeElapsed: Float = 0f
    private lateinit var checkbox: Checkbox<DonatePleaseScreen>
    private val button: Button<DonatePleaseScreen>

    init {
        stage.backButton.visible = false
        stage.titleLabel.text = "Just before we start..."
        stage.titleLabel.isLocalizationKey = false
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_credits"))

        button = object : Button<DonatePleaseScreen>(main.uiPalette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (checkbox.checked) {
                    Gdx.net.openURI(RHRE3.DONATION_URL)
                }

                main.screen = nextScreen
            }
        }.apply {
            addLabel(TextLabel(main.uiPalette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = "Click to go to donation page"
            })
            this.location.set(0.15f, 0f, 0.7f, 1f)
            this.enabled = false
        }
        stage.bottomStage.elements += button
        val buttonLabel: TextLabel<DonatePleaseScreen> = button.labels.first() as TextLabel<DonatePleaseScreen>

        checkbox = object : TrueCheckbox<DonatePleaseScreen>(main.uiPalette, stage.centreStage, stage.centreStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (checked) {
                    buttonLabel.text = "Click to go to donation page"
                } else {
                    buttonLabel.text = "[GRAY]No thanks, I'll pass[]"
                }
            }
        }.apply {
            this.textLabel.apply {
                this.text = "Yes, I'd like to support RHRE"
                this.isLocalizationKey = false
            }
            this.checked = true
            this.enabled = false
            this.location.set(0.25f, 0f, 0.5f, 0.15f)
        }
        stage.centreStage.elements += checkbox

        stage.centreStage.elements += TextLabel(main.uiPalette, stage.centreStage, stage.centreStage).apply {
            this.text = """
                Hello,
                I know you'd like to see all the RHRExpansion has to offer, but here's one last message.

                RHRE (the program) has been maintained by me for over two years. It does take up
                my personal time to develop. Over 88 updates later, RHRE's userbase has grown considerably
                and the number of people that can make custom remixes has grown thanks to its accessibility.
                In fact, RHRE goes beyond audio remixes, and enables remixers to make their high-quality
                visual and even modded remixes.

                So I kindly ask you to [YELLOW]consider a donation[]. I suggest donating what you feel
                RHRE is worth to you. This message will only appear once, and won't bother you again.
                All donations are greatly appreciated, and go to serving RHRE in the long-term!

                Thank you,
                chrislo27
            """.trimIndent()
            this.isLocalizationKey = false
            this.location.set(0.05f, 0.15f, 0.9f, 0.85f)
            this.textWrapping = true
            this.textAlign = Align.left
            this.fontScaleMultiplier = 0.75f
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        timeElapsed += Gdx.graphics.deltaTime
        if (timeElapsed > 5f && !checkbox.enabled) {
            checkbox.enabled = true
            button.enabled = true
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}