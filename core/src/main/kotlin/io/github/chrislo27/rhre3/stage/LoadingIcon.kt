package io.github.chrislo27.rhre3.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.MathHelper


open class LoadingIcon<S : ToolboksScreen<*, *>>(private val palette: UIPalette, stage: Stage<S>)
    : Stage<S>(stage, stage.camera) {

    companion object {
        private const val VARIATIONS = 7
        private const val FRAMES = 5
        private const val SECONDS_PER_VARIATION = 1.25f
        private const val REGION_SIZE = 32
    }

    var speed = 1f
    private var inited = false
    private val mainImage: ImageLabel<S> = ImageLabel(palette, this, this)
    private val penImage: ImageLabel<S> = ImageLabel(palette, this, this)

    var renderType: ImageLabel.ImageRendering
        get() = mainImage.renderType
        set(value) {
            mainImage.renderType = value
            penImage.renderType = value
        }

    init {
        elements += mainImage
        elements += penImage
    }

    override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (!inited) {
            mainImage.image = TextureRegion(AssetRegistry.get<Texture>("ui_loading_icon"))
            penImage.image = TextureRegion(AssetRegistry.get<Texture>("ui_loading_icon"))
            inited = true
        }

        val currentVariation: Int = ((MathHelper.getSawtoothWave(
                SECONDS_PER_VARIATION * VARIATIONS) * VARIATIONS / speed).toInt() + 1).coerceIn(1, VARIATIONS)
        val currentFrame: Int = (MathHelper.getSawtoothWave(SECONDS_PER_VARIATION / speed) * FRAMES).toInt().coerceIn(0,
                                                                                                                      FRAMES - 1)
        mainImage.image?.also { img ->
            img.setRegion(REGION_SIZE * (currentFrame + 1), REGION_SIZE * (currentVariation), REGION_SIZE, REGION_SIZE)
        }
        penImage.image?.also { img ->
            img.setRegion(REGION_SIZE * (currentFrame + 1), 0, REGION_SIZE, REGION_SIZE)
        }

        super.render(screen, batch, shapeRenderer)
    }

}
