package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.util.MathHelper


open class TilingBackground(id: String, val period: Float, val textureProvider: () -> Texture)
    : Background(id) {

    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        batch.setColor(1f, 1f, 1f, 1f)
        val tex: Texture = textureProvider()
        val start: Float = MathHelper.getSawtoothWave(period) - 1f
        val ratioX = camera.viewportWidth / RHRE3.WIDTH
        val ratioY = camera.viewportHeight / RHRE3.HEIGHT
        for (x in (start * tex.width).toInt()..RHRE3.WIDTH step tex.width) {
            for (y in (start * tex.height).toInt()..RHRE3.HEIGHT step tex.height) {
                batch.draw(tex, x.toFloat() * ratioX, y.toFloat() * ratioY, tex.width * ratioX, tex.height * ratioY)
            }
        }
    }

}