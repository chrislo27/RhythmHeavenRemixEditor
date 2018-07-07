package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad


class TengokuBackground(id: String,
                        maxParticles: Int = 40,
                        val topColor: Color = Color.valueOf("4048e0"),
                        val bottomColor: Color = Color.valueOf("d020a0"),
                        var cycleSpeed: Float = 1f / 20f)
    : ParticleBasedBackground(id, maxParticles) {

    class Square(x: Float, y: Float,
                 size: Float = MathUtils.random(20f, 80f),
                 speedX: Float = MathUtils.random(0.075f, 0.2f),
                 speedY: Float = -MathUtils.random(0.075f, 0.2f),
                 rotSpeed: Float = MathUtils.random(90f, 200f) * MathUtils.randomSign(),
                 rotation: Float = MathUtils.random(360f))
        : Particle(x, y, size, size, speedX, speedY, rotSpeed, rotation, "menu_bg_square")

    private val hsv: FloatArray = FloatArray(3)

    override fun renderBackground(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        val ratioX = width / RHRE3.WIDTH
        val ratioY = height / RHRE3.HEIGHT

        if (cycleSpeed > 0f) {
            topColor.toHsv(hsv)
            hsv[0] = (hsv[0] - Gdx.graphics.deltaTime * cycleSpeed * 360f) % 360f
            topColor.fromHsv(hsv)
            bottomColor.toHsv(hsv)
            hsv[0] = (hsv[0] - Gdx.graphics.deltaTime * cycleSpeed * 360f) % 360f
            bottomColor.fromHsv(hsv)
        }

        batch.drawQuad(0f, 0f, bottomColor, width, 0f, bottomColor,
                       width, height, topColor, 0f, height, topColor)

        // Remove OoB squares
        particles.removeIf {
            it.x > 1f + (ratioX * it.sizeX) / width || it.y < -(ratioY * it.sizeY) / height
        }
    }

    override fun createParticle(initial: Boolean): Particle? {
        return if (!initial) {
            Square(-0.5f, 1f + MathUtils.random(1f))
        } else {
            Square(MathUtils.random(1f), MathUtils.random(1f))
        }
    }

}