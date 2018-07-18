package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.registry.AssetRegistry
import kotlin.math.roundToInt


abstract class ParticleBasedBackground(id: String, val maxParticles: Int)
    : Background(id) {

    protected val particles: MutableList<Particle> = mutableListOf()

    abstract fun renderBackground(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float)
    abstract fun createParticle(initial: Boolean): Particle?

    final override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        renderBackground(camera, batch, shapeRenderer, delta)
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        val ratioX = width / RHRE3.WIDTH
        val ratioY = height / RHRE3.HEIGHT

        if (maxParticles > 0 && particles.size < maxParticles) {
            val initial = particles.size == 0 && maxParticles > 1
            while (particles.size < (if (!initial) maxParticles else (maxParticles * 0.8f).roundToInt().coerceAtLeast(1))) {
                val particle = createParticle(initial) ?: break
                particles += particle
            }
        }

        // Render particles
        batch.setColor(1f, 1f, 1f, 0.65f)
        particles.forEach {
            it.x += it.speedX * delta
            it.y += it.speedY * delta
            it.rotation += it.rotSpeed * delta
            val tex = AssetRegistry.get<Texture>(it.tex)

            batch.draw(tex, it.x * width, it.y * width,
                       it.sizeX / 2, it.sizeY / 2, it.sizeX, it.sizeY, ratioX, ratioY, it.rotation,
                       0, 0, tex.width, tex.height, false, false)
        }
        batch.setColor(1f, 1f, 1f, 1f)
    }

}