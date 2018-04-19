package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.screen.NewsScreen.State.ARTICLES
import io.github.chrislo27.rhre3.screen.NewsScreen.State.ERROR
import io.github.chrislo27.rhre3.screen.NewsScreen.State.FETCHING
import io.github.chrislo27.rhre3.screen.NewsScreen.State.IN_ARTICLE
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*


class NewsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, NewsScreen>(main) {

    override val stage: GenericStage<NewsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    val hasNewNews: Boolean
        get() = true

    enum class State {
        ARTICLES, IN_ARTICLE, FETCHING, ERROR
    }

    private var state: State = FETCHING
        @Synchronized set(value) {
            field = value

            articleListStage.visible = false
            fetchingStage.visible = false
            errorLabel.visible = false

            when (value) {
                ARTICLES -> articleListStage
                IN_ARTICLE -> articleStage
                FETCHING -> fetchingStage
                ERROR -> errorLabel
            }.visible = true
        }
    private val articleListStage: Stage<NewsScreen> = Stage(stage.centreStage, stage.centreStage.camera)
    private val fetchingStage: Stage<NewsScreen> = Stage(stage.centreStage, stage.centreStage.camera)
    private val errorLabel: TextLabel<NewsScreen> = TextLabel(main.uiPalette, stage.centreStage, stage.centreStage).apply {
        this.isLocalizationKey = true
        this.text = "screen.news.cannotLoad"
        this.textAlign = Align.center
    }
    private val articleStage: ArticleStage = ArticleStage(stage.centreStage, stage.centreStage.camera)
    private val articleButtons: List<ArticleButton>

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

        val centreLoadingIcon = LoadingIcon(main.uiPalette, fetchingStage)
        fetchingStage.elements += centreLoadingIcon.apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.25f, screenY = 0.5f - 0.25f)
        }
        fetchingStage.elements += TextLabel(palette, fetchingStage, fetchingStage).apply {
            this.text = "screen.news.fetching"
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.location.set(screenY = centreLoadingIcon.location.screenY + centreLoadingIcon.location.screenHeight + 0.1f,
                              screenHeight = centreLoadingIcon.location.screenHeight / 2)
        }

        // Article button populating
        val padding = 0.1f
        articleButtons = (0 until 9).map { index ->
            val cellX = index % 3
            val cellY = index / 3

            ArticleButton(palette, articleListStage, articleListStage).apply {
                this.location.set(screenX = padding * (1 + cellX))
            }
        }
        articleListStage.elements.addAll(articleButtons)

        stage.centreStage.elements += fetchingStage
        stage.centreStage.elements += errorLabel
        stage.centreStage.elements += articleListStage
        stage.centreStage.elements += articleStage

        state = state // Change visibility
    }

    override fun renderUpdate() {
        super.renderUpdate()
    }

    override fun show() {
        super.show()

        if (state == ERROR) {
            state = FETCHING
            // TODO do fetch
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    inner class ArticleButton(palette: UIPalette, parent: UIElement<NewsScreen>,
                              stage: Stage<NewsScreen>) : Button<NewsScreen>(palette, parent, stage) {

    }

    inner class ArticleStage(parent: UIElement<NewsScreen>?, camera: OrthographicCamera)
        : Stage<NewsScreen>(parent, camera) {


    }

}