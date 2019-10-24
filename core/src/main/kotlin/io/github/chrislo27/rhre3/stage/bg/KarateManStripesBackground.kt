package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class KarateManStripesBackground(id: String, val stripe1: Color = Color.valueOf("F1ADFAFF"),
                                 val stripe2: Color = Color.valueOf("E785F2FF"),
                                 var cycle: Float = 2f)
    : Background(id) {
    
    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        
        val cycle = MathHelper.getSawtoothWave(cycle)
        val stripeColor = if (cycle > 0.5f) stripe1 else stripe2
        val bgColor = if (cycle > 0.5f) stripe2 else stripe1
        
        batch.color = bgColor
        batch.fillRect(0f, 0f, width, height)
        batch.setColor(1f, 1f, 1f, 1f)
        
        batch.end()
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        
        val faroffPointX = width * -0.75f
        val faroffPointY = height / 2
        val numStripes = 17
        val maxAngle = 30f
        shapeRenderer.color = stripeColor
        
        for (i in 0 until (numStripes + 2) step 2) {
            val angle = MathUtils.lerp(maxAngle, -maxAngle, i.toFloat() / numStripes)
            val targetX = faroffPointX + width * 2f * MathUtils.cosDeg(angle)
            val targetY = faroffPointY + height * 2f * MathUtils.sinDeg(angle)
            val fatness = 42f
            shapeRenderer.triangle(faroffPointX, faroffPointY, targetX, targetY - fatness, targetX, targetY + fatness)
        }
        
        shapeRenderer.setColor(1f, 1f, 1f, 1f)
        shapeRenderer.end()
        batch.begin()
    }
    
}