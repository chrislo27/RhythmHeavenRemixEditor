package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class RetroBackground(id: String, var cycleSpeed: Float = 1f / 15f)
    : Background(id) {

    private val hsv: FloatArray = FloatArray(3)
    private val color: Color = Color()
    private val color2: Color = Color()
    private val hColor: Color = Color()

    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val width = camera.viewportWidth
        val height = camera.viewportHeight

        if (cycleSpeed > 0f) {
            val offset: Long = ((1000L / cycleSpeed) / 3).roundToLong()
            color.fromHsv(MathHelper.getSawtoothWave(System.currentTimeMillis(), 1f / cycleSpeed) * 360f, 0.9f, 0.9f)
            color2.fromHsv(MathHelper.getSawtoothWave(System.currentTimeMillis() + offset, 1f / cycleSpeed) * 360f, 0.9f, 0.9f)
        }

        batch.setColor(0f, 0f, 0f, 1f)
        batch.fillRect(0f, 0f, width, height)
        batch.setColor(1f, 1f, 1f, 1f)

        batch.end()
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // vertical
        repeat(32) {
            val alpha = it.toFloat() / 32f
            val x = Interpolation.exp5.apply(-width, width * 2, alpha)
            shapeRenderer.rectLine(x, -10f, Interpolation.linear.apply(-width * 0.125f, width * 1.125f, alpha), height * 0.5f, MathUtils.lerp(2f, 1f, alpha).roundToInt().toFloat(), color, color2)
        }
        repeat(32) {
            val alpha = it.toFloat() / 32f
            val x = Interpolation.exp5.apply(-width, width * 2, alpha)
            shapeRenderer.rectLine(x, height + 10f, Interpolation.linear.apply(-width * 0.125f, width * 1.125f, alpha), height * 0.5f, MathUtils.lerp(2f, 1f, alpha).roundToInt().toFloat(), color, color2)
        }

        val scrollY = MathHelper.getSawtoothWave(2f)

        // horizontal
        repeat(32) {
            val alpha = it.toFloat() / 32f
            val y = Interpolation.exp10Out.apply(16f, height * 0.5f, alpha - scrollY / 32f)

            shapeRenderer.color = hColor.set(color).lerp(color2, y / (height * 0.5f + 10f))
            shapeRenderer.rectLine(-10f, y, width + 10f, y, MathUtils.lerp(2f, 1f, alpha - scrollY / 32f).roundToInt().toFloat())
        }
        repeat(32) {
            val alpha = it.toFloat() / 32f
            val y = Interpolation.exp10Out.apply(height - 16f, height * 0.5f, alpha - scrollY / 32f)

            shapeRenderer.color = hColor.set(color2).lerp(color, (y - height * 0.5f) / (height * 0.5f + 10f))
            shapeRenderer.rectLine(-10f, y, width + 10f, y, MathUtils.lerp(2f, 1f, alpha - scrollY / 32f).roundToInt().toFloat())
        }

        shapeRenderer.setColor(1f, 1f, 1f, 1f)
        shapeRenderer.end()
        batch.begin()
//        batch.setColor(0f, 0f, 0f, 1f)
//        batch.fillRect(0f, height, width, -(height - height * 0.6f))
//        batch.setColor(1f, 1f, 1f, 1f)
    }

}