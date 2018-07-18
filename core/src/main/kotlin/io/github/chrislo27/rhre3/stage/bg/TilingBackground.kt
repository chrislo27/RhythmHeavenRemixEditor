package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.util.MathHelper
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


open class TilingBackground(id: String, val period: Float, val speedX: Float = 1f, val speedY: Float = 1f, val widthCoeff: Float = 1f, val heightCoeff: Float = 1f,
                            val textureProvider: () -> Texture)
    : Background(id) {

    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        batch.setColor(1f, 1f, 1f, 1f)
        val tex: Texture = textureProvider()
        val start: Float = MathHelper.getSawtoothWave(period)
        val ratioX = camera.viewportWidth / RHRE3.WIDTH
        val ratioY = camera.viewportHeight / RHRE3.HEIGHT

        val w = (tex.width * widthCoeff).roundToInt()
        val h = (tex.height * heightCoeff).roundToInt()
        for (x in (start * w * speedX - (w * speedX.absoluteValue)).toInt()..RHRE3.WIDTH step w) {
            for (y in (start * h * speedY - (h * speedY.absoluteValue)).toInt()..RHRE3.HEIGHT step h) {
                batch.draw(tex, x.toFloat() * ratioX, y.toFloat() * ratioY, w * ratioX, h * ratioY)
            }
        }
    }

}