package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


open class GameDisplayStage(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>?, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    companion object {
        const val WIDTH_MULTIPLICATION = 12f
        const val FONT_SCALE = 0.8f
    }

    private val icon: ImageLabel<EditorScreen>
    private val label: TextLabel<EditorScreen>
    private val textureRegion = TextureRegion()

    open fun getFont(): BitmapFont = editor.main.defaultBorderedFont

    var gameGroup: Game? = null
        set(value) {
            if (field != value) {
                field = value

                icon.image = if (value == null) null else textureRegion.apply { this.setRegion(value.icon) }
                label.text = GameRegistry.data.gameGroupsMap[value?.group]?.name ?: (value?.name) ?: ""
            }
        }

    init {
        icon = ImageLabel(palette, this, this).apply {
            this.location.set(screenX = 0f,
                              screenY = 0f,
                              screenHeight = 1f,
                              screenWidth = 1f / WIDTH_MULTIPLICATION)
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        }
        label = object : TextLabel<EditorScreen>(palette, this, this){
            override fun getFont(): BitmapFont {
                return this@GameDisplayStage.getFont()
            }
        }.apply {
            this.location.set(screenX = (1f / WIDTH_MULTIPLICATION) * 1.25f,
                              screenY = 0f,
                              screenHeight = 1f)
            this.location.set(screenWidth = 1f - location.screenX)
            this.isLocalizationKey = false
            this.text = ""
            this.textAlign = Align.left
            this.textWrapping = false
            this.fontScaleMultiplier = FONT_SCALE
        }

        elements.apply {
            add(icon)
            add(label)
        }
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        gameGroup = editor.remix.currentGameGroup
        super.render(screen, batch, shapeRenderer)
    }
}