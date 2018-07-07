package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad


class KarateManBackground(id: String, maxParticles: Int = 32,
                          val orangeTop: Color = Color.valueOf("CD3907"),
                          val orangeBottom: Color = Color.valueOf("FF9333"),
                          val blueTop: Color = Color.valueOf("27649A"),
                          val blueBottom: Color = Color.valueOf("72AED5"),
                          var cycleSpeed: Float = 1f / 10f)
    : ParticleBasedBackground(id, maxParticles) {

    class Snowflake(x: Float, y: Float,
                    size: Float = 54f,
                    speedX: Float = -MathUtils.random(0.075f, 0.25f),
                    speedY: Float = -MathUtils.random(0.05f, 0.1f))
        : Particle(x, y, size, size, speedX, speedY, 0f, 0f, "menu_snowflake")


    private val top = Color()
    private val bottom = Color()

    override fun renderBackground(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        val ratioX = width / RHRE3.WIDTH
        val ratioY = height / RHRE3.HEIGHT

        if (cycleSpeed > 0f) {
            val percentage = MathHelper.getBaseCosineWave(1f / cycleSpeed)
            top.set(blueTop)
            bottom.set(blueBottom)

            top.lerp(orangeTop, percentage)
            bottom.lerp(orangeBottom, percentage)
        }

        batch.drawQuad(0f, 0f, bottom, width, 0f, bottom,
                       width, height, top, 0f, height, top)

        // Remove OoB particles
        particles.removeIf {
            it.x < -(ratioX * it.sizeX) / width || it.y < -(ratioY * it.sizeY) / height
        }
    }

    override fun createParticle(initial: Boolean): Particle? {
        return if (!initial) {
            Snowflake(1.25f, 0.25f + MathUtils.random(1f))
        } else {
            Snowflake(MathUtils.random(1f), MathUtils.random(1f))
        }
    }

}