package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.news.Article
import io.github.chrislo27.rhre3.news.Articles
import io.github.chrislo27.rhre3.news.ThumbnailFetcher
import io.github.chrislo27.rhre3.screen.NewsScreen.State.ARTICLES
import io.github.chrislo27.rhre3.screen.NewsScreen.State.ERROR
import io.github.chrislo27.rhre3.screen.NewsScreen.State.FETCHING
import io.github.chrislo27.rhre3.screen.NewsScreen.State.IN_ARTICLE
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown


class NewsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, NewsScreen>(main) {

    override val stage: GenericStage<NewsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    var hasNewNews: Boolean = false
        private set

    enum class State {
        ARTICLES, IN_ARTICLE, FETCHING, ERROR
    }

    private var state: State = FETCHING
        @Synchronized set(value) {
            field = value

            articleStage.visible = false
            articleListStage.visible = false
            fetchingStage.visible = false
            errorLabel.visible = false
            refreshButton.visible = false
            articleLinkButton.visible = value == IN_ARTICLE && articleLinkButton.visible
            articlePaginationStage.visible = false

            when (value) {
                ARTICLES -> listOf(articleListStage, articlePaginationStage, refreshButton)
                IN_ARTICLE -> listOf(articleStage)
                FETCHING -> listOf(fetchingStage)
                ERROR -> listOf(errorLabel, refreshButton)
            }.forEach { (@Suppress("Unchecked_Cast")(it as UIElement<NewsScreen>)).visible = true }

            if (value == ARTICLES) {
                articleButtons.forEachIndexed { index, it ->
                    it.article = Articles.articles.elementAtOrNull(index)
                    it.visible = it.article != null
                }

                articlePaginationStage.update(1, 1) // Fix when pagination added
            }
        }
    private val articleListStage: Stage<NewsScreen> = Stage(stage.centreStage, stage.centreStage.camera)
    private val fetchingStage: Stage<NewsScreen> = Stage(stage.centreStage, stage.centreStage.camera)
    private val errorLabel: TextLabel<NewsScreen> = TextLabel(main.uiPalette, stage.centreStage,
                                                              stage.centreStage).apply {
        this.isLocalizationKey = true
        this.text = "screen.news.cannotLoad"
        this.textAlign = Align.center
    }
    private val articleStage: ArticleStage = ArticleStage(main.uiPalette, stage.centreStage, stage.centreStage.camera)
    private val articlePaginationStage = ArticlePaginationStage(main.uiPalette, stage.bottomStage,
                                                                stage.bottomStage.camera).apply {
        this.location.set(screenX = 0.25f, screenWidth = 0.5f)
    }
    private val articleLinkButton = ArticleLinkButton(main.uiPalette, stage.bottomStage, stage.bottomStage).apply {
        this.location.set(screenX = 0.15f, screenWidth = 0.7f)
    }
    private val refreshButton: RefreshButton
    private val articleButtons: List<ArticleButton>

    private val newsPreferences: Preferences = Gdx.app.getPreferences("RHRE3-news")

    init {
        val palette = main.uiPalette
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_news_big"))
        stage.titleLabel.text = "screen.news.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (state == IN_ARTICLE) {
                state = ARTICLES
            } else {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        refreshButton = RefreshButton(palette, stage.bottomStage, stage.bottomStage).apply {
            val backButton = this@NewsScreen.stage.backButton
            this.location.set(backButton.location.screenX, backButton.location.screenY, backButton.location.screenWidth, backButton.location.screenHeight)
            this.location.set(screenX = 1f - (this.location.screenX + this.location.screenWidth))

            addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))
            })
        }
        stage.bottomStage.elements += refreshButton

        val centreLoadingIcon = LoadingIcon(main.uiPalette, fetchingStage)
        fetchingStage.elements += centreLoadingIcon.apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.25f, screenY = 0.5f - 0.25f)
        }
        fetchingStage.elements += TextLabel(palette, fetchingStage, fetchingStage).apply {
            this.text = "screen.news.fetching"
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.location.set(
                    screenY = centreLoadingIcon.location.screenY + centreLoadingIcon.location.screenHeight + 0.1f,
                    screenHeight = centreLoadingIcon.location.screenHeight / 2)
        }

        // Article button populating
        val padding = 0.025f
        articleButtons = (0 until Articles.ARTICLE_COUNT).map { index ->
            val cellX = index % 3
            val cellY = index / 3

            ArticleButton(palette, articleListStage, articleListStage).apply {
                this.location.set(screenWidth = (1f - padding * 3) / 3, screenHeight = (1f - padding * 4) / 3)
                this.location.set(screenX = padding * (cellX) + this.location.screenWidth * cellX,
                                  screenY = 1f - padding * (1 + cellY) - this.location.screenHeight * (1 + cellY))

                this.title.text = "Lorem ipsum $index @ ($cellX, $cellY)"
            }
        }
        articleListStage.elements.addAll(articleButtons)

        stage.centreStage.elements += fetchingStage
        stage.centreStage.elements += errorLabel
        stage.centreStage.elements += articleListStage
        stage.centreStage.elements += articleStage
//        stage.bottomStage.elements += articlePaginationStage // Pagination disabled
        stage.bottomStage.elements += articleLinkButton

        state = state // Change visibility

        Articles.fetchStateListeners += { _, new ->
            when (new) {
                Articles.FetchState.FETCHING -> {
                    state = FETCHING
                    articleButtons.forEach {
                        it.article = null
                        it.visible = it.article != null
                    }
                }
                Articles.FetchState.DONE -> {
                    state = ARTICLES

                    // Check new news
                    val lastNews = main.preferences.getString(PreferenceKeys.LAST_NEWS, null)
                    hasNewNews = if (lastNews.isNullOrBlank() && Articles.articles.isNotEmpty()) {
                        true
                    } else {
                        Articles.articles.firstOrNull()?.id != lastNews && Articles.articles.isNotEmpty()
                    }
                    main.preferences.putString(PreferenceKeys.LAST_NEWS, Articles.articles.firstOrNull()?.id ?: Article.BLANK.id).flush()
                }
                Articles.FetchState.ERROR -> {
                    state = ERROR
                    articleButtons.forEach {
                        it.article = null
                        it.visible = it.article != null
                    }
                }
            }
        }
        Articles.fetch()
    }

    override fun show() {
        super.show()

        if (state == ERROR) {
            state = FETCHING
            Articles.fetch()
        }

        if (Articles.isFetching == Articles.FetchState.ERROR) {
            state = ERROR
        } else if (Articles.isFetching == Articles.FetchState.FETCHING) {
            state = FETCHING
        }

        if (Articles.isFetching == Articles.FetchState.DONE) {
            state = ARTICLES
        }

        DiscordHelper.updatePresence(PresenceState.ViewingNews)
    }

    override fun hide() {
        super.hide()

        hasNewNews = false
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isControlDown()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                refresh()
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.U) && state == ARTICLES) {
                articleButtons.filter { it.article != null }.forEach { articleButton ->
                    val article = articleButton.article
                    if (article != null) {
                        Articles.setArticleViewed(article, newsPreferences, false)
                        articleButton.article = article // Refresh title colours
                    }
                }
                hasNewNews = true
            }
        }
    }

    private fun refresh() {
        state = FETCHING
        Articles.fetch()
    }

    override fun getDebugString(): String? {
        return "CTRL+R - Refetch | CTRL+U - Mark all unread"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        ThumbnailFetcher.cancelAll()
        ThumbnailFetcher.removeAll()
        newsPreferences.flush()
    }

    inner class ArticleButton(palette: UIPalette, parent: UIElement<NewsScreen>,
                              stage: Stage<NewsScreen>) : Button<NewsScreen>(palette, parent, stage) {
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
                    updateTitleColor(value)
                    thumbnail.image = try {
                        if (value.thumbnail.isBlank()) {
                            TextureRegion(AssetRegistry.get<Texture>("logo_256"))
                        } else if (value.thumbnail.startsWith("tex:")){
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

        private fun updateTitleColor(article: Article) {
            title.textColor = if (Articles.isArticleViewed(article, newsPreferences)) Color.LIGHT_GRAY else null
        }

        init {
            addLabel(title)
            addLabel(thumbnail)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)

            val article = article
            if (article != null) {
                articleStage.prep(article)
                Articles.setArticleViewed(article, newsPreferences, true)
                updateTitleColor(article)
                state = IN_ARTICLE
            }
        }
    }

    inner class ArticleLinkButton(palette: UIPalette, parent: UIElement<NewsScreen>,
                                  stage: Stage<NewsScreen>) : Button<NewsScreen>(palette, parent, stage) {
        val title = TextLabel(palette, this, stage).apply {
            this.isLocalizationKey = false
            this.fontScaleMultiplier = 0.9f
            this.textWrapping = true

            addLabel(this)
        }
        var link: String? = null

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            val link = link
            if (link != null) {
                try {
                    Gdx.net.openURI(link)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class ArticleStage(palette: UIPalette, parent: UIElement<NewsScreen>?, camera: OrthographicCamera)
        : Stage<NewsScreen>(parent, camera) {

        var article: Article = Article.BLANK
            private set
        val label = TextLabel(palette, this, this).apply {
            this.textAlign = Align.left or Align.top
            this.isLocalizationKey = false
            this.textWrapping = false
            this.location.set(screenY = 0.825f, screenHeight = 0.175f)
            elements += this
        }
        val body = TextLabel(palette, this, this).apply {
            this.textAlign = Align.left or Align.top
            this.location.set(screenHeight = 0.825f)
            this.isLocalizationKey = false
            this.textWrapping = true
            this.textColor = Color.valueOf("#EEEEEEFF")
            this.fontScaleMultiplier = 0.75f
            elements += this
        }

        fun prep(article: Article) {
            this.article = article

            label.text = "[LIGHT_GRAY]${article.publishedDate}[]${if (article.experimental) " - EXPERIMENTAL" else ""}\n${article.title}"
            body.text = article.body
            articleLinkButton.visible = article.url != null
            articleLinkButton.title.text = article.urlTitle ?: article.url ?: ""
            articleLinkButton.link = article.url
        }

    }

    inner class ArticlePaginationStage(palette: UIPalette, parent: UIElement<NewsScreen>?, camera: OrthographicCamera)
        : Stage<NewsScreen>(parent, camera) {

        val label = TextLabel(palette, this, this).apply {
            this.location.set(screenX = 0.1f, screenWidth = 0.8f)
            this.isLocalizationKey = false
        }
        val leftButton = object : Button<NewsScreen>(palette, this, this) {

        }.apply {
            this.location.set(screenX = 0f, screenWidth = 0.1f)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_right_chevron")).apply {
                    flip(true, false)
                }
            })
        }
        val rightButton = object : Button<NewsScreen>(palette, this, this) {

        }.apply {
            this.location.set(screenX = 0.9f, screenWidth = 0.1f)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_right_chevron")).apply {
                    flip(false, false)
                }
            })
        }

        fun update(current: Int, total: Int) {
            label.text = Localization["screen.news.page", "$current", "$total"]
            leftButton.visible = current > 1
            rightButton.visible = current < total
        }

        init {
            elements += label
            elements += leftButton
            elements += rightButton

            update(0, 0)
        }

    }

    inner class RefreshButton(palette: UIPalette, parent: UIElement<NewsScreen>, stage: Stage<NewsScreen>)
        : Button<NewsScreen>(palette, parent, stage) {
        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            refresh()
        }
    }

}