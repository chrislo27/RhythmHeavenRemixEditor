package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class LaunchPartyBackground(id: String) : Background(id) {

    private val seed = Random().nextInt(255)
    private val texture: Texture by lazy { AssetRegistry.get<Texture>("bg_launchparty_objects") }

    private val star: TextureRegion by lazy { TextureRegion(texture, 666, 3, 91, 91) }
    private val starfield: TextureRegion by lazy { TextureRegion(texture, 762, 3, 259, 226) }
    private val galaxy: TextureRegion by lazy { TextureRegion(texture, 874, 235, 147, 138) }
    private val planet: TextureRegion by lazy { TextureRegion(texture, 1, 628, 846, 395) }
    private val lensFlare: TextureRegion by lazy { TextureRegion(texture, 3, 211, 402, 410) }

    private var first = true

    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        if (first) {
            first = false
            // Set texture filters to linear
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
        val width = camera.viewportWidth
        val height = camera.viewportHeight

        batch.setColor(0f, 0f, 0f, 1f)
        batch.fillRect(0f, 0f, width, height)
        batch.setColor(1f, 1f, 1f, 1f)

        batch.setColor(1f, 1f, 1f, 0.5f)
        batch.draw(galaxy, width * 0.75f, height * 0.75f, width * 0.125f, (width * 0.125f) * galaxy.regionHeight / galaxy.regionWidth)
        for (x in -1..3) {
            for (y in -1..3) {
                val seed = (x * 3 + y) + this.seed
                val colour = MathUtils.lerp(0.85f, 1f, MathHelper.getBaseCosineWave(System.currentTimeMillis() + seed * 345L, 2.5f))
                batch.setColor(colour, colour, 1f, 0.5f)
                batch.draw(starfield, (width * 0.25f) * x + (width * 0.05f * (if (seed % 3 == 0) 1 else -1)), (height * 0.45f) * y + (height * 0.05f * (if (seed % 2 == 0) 1 else -1)), width / 2, height / 2)
            }
        }
        batch.setColor(1f, 1f, 1f, 1f)

        batch.draw(planet, -2f, -2f, width, width * planet.regionHeight / planet.regionWidth + 4f)
        batch.draw(lensFlare, width * -0.005f - 2f, height * 0.065f - 2f, width / 2f, width / 2f * lensFlare.regionHeight / lensFlare.regionWidth)

        // Stars
        val starCount = 96
        for (i in 0 until starCount) {
            val alpha = i / starCount.toFloat()
            val size = Interpolation.circleIn.apply(64f, 300f, alpha) * 1.5f
            val rotation = MathHelper.getSawtoothWave(System.currentTimeMillis() + (273L * alpha * 2).roundToLong(), Interpolation.circleOut.apply(0.65f, 1.15f, alpha)) * (if (i % 2 == 0) -1 else 1)

            // position
            // the larger the star, the slower it falls in general
            val yInterval = Interpolation.circleOut.apply(8f, 5f, alpha)
            val yAlpha = 1f - MathHelper.getSawtoothWave(System.currentTimeMillis() + (562L * alpha * 2).roundToLong(), yInterval)
            val y = MathUtils.lerp(width * -0.1f, width * 1.1f, yAlpha)
            val x = (width * 1.41421356f * (i + 23) * (alpha + seed) + (yAlpha * yInterval).roundToInt()) % (width * 1.25f)

            drawStar(camera, batch, x - size / 2, y - size / 2, rotation * 360f, size)
        }
    }

    fun drawStar(camera: OrthographicCamera, batch: SpriteBatch, x: Float, y: Float, rot: Float, size: Float) {
        val width = size * (camera.viewportWidth / RHRE3.WIDTH)
        val height = size * (camera.viewportHeight / RHRE3.HEIGHT)
        batch.draw(star, x - width / 2, y - height / 2, width / 2, height / 2, width, height, 1f, 1f, rot)
    }

}