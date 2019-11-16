package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.news.Article
import io.github.chrislo27.rhre3.news.ThumbnailFetcher
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown


class PartnersScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, PartnersScreen>(main) {

    companion object {
        private val PARTNERS: List<Article> = listOf(
                Article("partner_Lvl100Feraligatr", "Lvl100Feraligatr", "", "https://i.imgur.com/LRBS001.jpg", 0L, "https://www.youtube.com/channel/UCigmB3cCbQ7wuY4SKHAL4WQ", null, false),
                Article("partner_NP", "NP", "", "https://i.imgur.com/0RzgqHr.jpg", 0L, "https://www.youtube.com/channel/UCKr25WU0dEPvWqWotAGljpg", null, false),
                Article("partner_PikaMasterJesi", "PikaMasterJesi", "", "https://yt3.ggpht.com/a-/AN66SAzhzcIikkC8EBbMCnezXYw43T_RgLobzl2Cjw=s288-mo-c-c0xffffffff-rj-k-no", 0L, "https://www.youtube.com/channel/UCu8Ltmlr5jH77Y8A5YclqgA", null, false),
                Article("partner_AngryTapper", "AngryTapper", "", "https://i.imgur.com/b0LyjcW.jpg", 0L, "https://www.youtube.com/channel/UC4ZPmmnRHUwmA0_q1GavPEA", null, false),
                Article("partner_meuol", "The Meuol", "", "https://i.imgur.com/usMnMO0.jpg", 0L, "https://www.youtube.com/channel/UCNAUWWq3RKyGDHzBuKgEcPg", null, false),
                Article("partner_Killble", "Killble", "", "https://i.imgur.com/AZ5hCOs.png", 0L, "https://www.youtube.com/user/sdllv", null, false),
                Article("partner_spoopster", "spoopster", "", "https://i.imgur.com/fttfacd.png", 0L, "https://www.youtube.com/channel/UCXIUIzLliw5c6BRIR2CRJkQ", null, false),
                Article("partner_RedCrowNose", "SilverLinkYT", "", "https://i.imgur.com/sOyFqSw.png", 0L, "https://www.youtube.com/channel/UCqwMGag_C4x1XMGp_XJBM5w", null, false),
                Article("partner_SportaDerp9000", "SportaDerp9000", "", "https://i.imgur.com/tsSWlRa.png", 0L, "https://www.youtube.com/channel/UCUbBQ2x33-KhEbP3FTesY8A", null, false),
                Article("partner_Suwa-ko", "Suwa-ko", "", "https://i.imgur.com/ArJ3Gqe.jpg", 0L, "https://www.youtube.com/channel/UCj-veAzBbeVqac7ISPsGvLw", null, false),
                Article("partner_Draster", "Draster", "", "https://i.imgur.com/SwZ60Wb.png", 0L, "https://www.youtube.com/channel/UCtlLcwZ_UXdyfvPGI5Meu0w", null, false)
                                                    )
        const val PARTNERS_VERSION: Int = 3
    }

    override val stage: GenericStage<PartnersScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val articleListStage: Stage<PartnersScreen> = Stage(stage.centreStage, stage.centreStage.camera)
    private val articleButtons: List<ArticleButton>
    private val descLabel: TextLabel<PartnersScreen>

    init {
        val palette = main.uiPalette
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_credits"))
        stage.titleLabel.text = "screen.partners.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        val paddingX = 0.01f
        val paddingY = 0.025f
        val columns = 4 // 4 up to 6
        val rows = 3 // 3 up to 4, but at 4 the text size has to be reduced slightly
        articleButtons = (0 until columns * rows).map { index ->
            val cellX = index % columns
            val cellY = index / columns

            ArticleButton(palette, articleListStage, articleListStage).apply {
                this.location.set(screenWidth = (1f - paddingX * (columns)) / columns, screenHeight = (1f - paddingY * (rows + 1)) / rows)
                this.location.set(screenX = paddingX * (cellX + 1) + this.location.screenWidth * cellX,
                                  screenY = 1f - paddingY * (1 + cellY) - this.location.screenHeight * (1 + cellY))

                this.title.text = "Lorem ipsum $index @ ($cellX, $cellY)"
            }
        }
        articleListStage.elements.addAll(articleButtons)

        descLabel = TextLabel(palette, stage.bottomStage, stage.bottomStage).apply {
            val backButtonWidth = this@PartnersScreen.stage.backButton.location.screenWidth
            this.location.set(screenX = backButtonWidth * 1.05f, screenWidth = 1f - (backButtonWidth * (2f + 0.05f * 2)))
            this.isLocalizationKey = true
            this.text = "screen.partners.desc"
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
        }

        stage.centreStage.elements += articleListStage

        stage.bottomStage.elements += descLabel

        randomizePartners()
    }

    private fun randomizePartners() {
        val shuffled = PARTNERS.shuffled()
        articleButtons.forEachIndexed { index, it ->
            it.article = shuffled.elementAtOrNull(index)
            it.visible = it.article != null
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isControlDown()) {
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                stage.backButton.onLeftClick(0f, 0f)
            }
        }
    }

    override fun show() {
        super.show()
        randomizePartners()
        DiscordHelper.updatePresence(PresenceState.ViewingPartners)
        main.preferences.putInteger(PreferenceKeys.VIEWED_PARTNERS_VERSION, PARTNERS_VERSION).flush()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        ThumbnailFetcher.cancelAll()
        ThumbnailFetcher.removeAll()
    }

    inner class ArticleButton(palette: UIPalette, parent: UIElement<PartnersScreen>,
                              stage: Stage<PartnersScreen>) : Button<PartnersScreen>(palette, parent, stage) {
        val title = TextLabel(palette, this, stage).apply {
            this.location.set(screenHeight = 0.25f)
            this.isLocalizationKey = false
            this.fontScaleMultiplier = 0.75f
            this.textWrapping = false
        }
        val thumbnail = ImageLabel(palette, this, stage).apply {
            this.location.set(screenY = title.location.screenHeight,
                              screenHeight = 1f - title.location.screenHeight)
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        }
        var article: Article? = null
            set(value) {
                field = value

                if (value != null) {
                    title.text = value.title
                    thumbnail.image = try {
                        if (value.thumbnail.isBlank()) {
                            TextureRegion(AssetRegistry.get<Texture>("logo_256"))
                        } else if (value.thumbnail.startsWith("tex:")) {
                            val id = value.thumbnail.substring(4)
                            if (AssetRegistry.containsAsType<Texture>(id)) {
                                TextureRegion(AssetRegistry.get<Texture>(id))
                            } else {
                                TextureRegion(AssetRegistry.get<Texture>("logo_256"))
                            }
                        } else if (value.thumbnail in ThumbnailFetcher.map) {
                            TextureRegion(ThumbnailFetcher.map[value.thumbnail])
                        } else {
                            ThumbnailFetcher.fetch(value.thumbnail) { tex, _ ->
                                if (tex != null && field == value) {
                                    thumbnail.image = TextureRegion(tex)
                                }
                            }
                            null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        TextureRegion(AssetRegistry.get<Texture>("logo_256"))
                    }
                }
            }

        init {
            addLabel(title)
            addLabel(thumbnail)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)

            val article = article
            if (article != null) {
                val link = article.url
                try {
                    Gdx.net.openURI(link)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                AnalyticsHandler.track("View Partner Page", mapOf("partner" to article.title))
            }
        }
    }

}