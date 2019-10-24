package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class SeesawBackground(id: String) : Background(id) {
    
    private val background: Color = Color.valueOf("FCAAF4FF")
    private val floor: Color = Color.valueOf("0B0F3EFF")
    
    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        batch.color = background
        batch.fillRect(0f, 0f, width, height)
        batch.color = floor
        batch.fillRect(0f, 0f, width, height * 0.2f)
        batch.setColor(1f, 1f, 1f, 1f)
    }
    
}