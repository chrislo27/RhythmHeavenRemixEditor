package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class KittiesBackground(id: String) : Background(id) {
    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        batch.setColor(1f, 1f, 1f, 1f)
        batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
    }

}