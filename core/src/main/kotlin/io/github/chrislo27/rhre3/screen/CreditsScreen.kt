package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.credits.Credits
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask


class CreditsScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, CreditsScreen>(main) {

    override val stage: Stage<CreditsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val font: BitmapFont
        get() = main.defaultFont
    private val scrollSpeed: Float
    private var text: String = ""
    private var scroll: Float = 0f
    private var maxScroll: Float = 1f
    private val element: UIElement<CreditsScreen>

    init {
        stage as GenericStage

        // TODO localize title + icon
        stage.titleLabel.text = "credits.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        element = object : UIElement<CreditsScreen>(stage.centreStage, stage.centreStage) {
            override fun render(screen: CreditsScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                shapeRenderer.prepareStencilMask(batch) {
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                    shapeRenderer.rect(location.realX, location.realY, location.realWidth, location.realHeight)
                    shapeRenderer.end()
                }.useStencilMask {
                    font.setColor(1f, 1f, 1f, 1f)

                    for (i in -1..1) {
                        font.draw(batch, text, location.realX,
                                  location.realY + location.realHeight * 0.75f + (scroll % maxScroll) - maxScroll * i,
                                  location.realWidth, Align.center, true)
                    }
                }
            }
        }.apply {

        }
        stage.centreStage.elements += element

        stage.bottomStage.elements += object : Button<CreditsScreen>(main.uiPalette, stage.bottomStage,
                                                                           stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(RHRE3.GITHUB)
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = Localization["screen.info.github", RHRE3.GITHUB]
                this.fontScaleMultiplier = 0.9f
            })

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        scrollSpeed = font.lineHeight * 2f
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            (stage as GenericStage).onBackButtonClick()
        } else {
            var scrolled = false
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
                scroll -= scrollSpeed * 6 * Gdx.graphics.deltaTime
                scrolled = true
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
                scroll += scrollSpeed * 6 * Gdx.graphics.deltaTime
                scrolled = true
            }

            if (!scrolled) {
                scroll += scrollSpeed * Gdx.graphics.deltaTime
            }
        }
    }

    private fun createText() {
        text = Credits.list.joinToString(separator = "") {
            "[RAINBOW]${Localization[it.localization]}[]\n${it.persons}\n\n"
        }

        maxScroll = (font.getTextHeight(text, element.location.realWidth, true) + element.location.realHeight * 0.75f).coerceAtLeast(1f)
    }

    override fun show() {
        super.show()

        scroll = 0f
        maxScroll = 1f
        createText()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}