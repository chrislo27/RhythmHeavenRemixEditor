package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.NewsScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*


class NewsButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                 stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    private val plainRegion = TextureRegion(AssetRegistry.get<Texture>("ui_icon_news"))
    private val newRegion = TextureRegion(AssetRegistry.get<Texture>("ui_icon_news_indicator"))
    private val label = ImageLabel(palette, this, this.stage).apply {
        this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        this.image = plainRegion
    }
    private val newsScreen: NewsScreen by lazy { ScreenRegistry.getNonNullAsType<NewsScreen>("news") }

    init {
        addLabel(label)
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        label.image = if (newsScreen.hasNewNews) newRegion else plainRegion
        super.render(screen, batch, shapeRenderer)
    }

    override var tooltipText: String?
        set(_) {}
        get() {
            return (if (newsScreen.hasNewNews) "[RAINBOW]${Localization["screen.news.tooltip.new"]}[] " else "") + Localization["screen.news.title"]
        }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.main.screen = ScreenRegistry.getNonNull("news")
    }
}