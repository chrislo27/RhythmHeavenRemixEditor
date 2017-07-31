package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application


class Editor(val main: RHRE3Application, stageCamera: OrthographicCamera)
    : Disposable {

    companion object {
        const val ICON_SIZE: Float = 32f
        const val ICON_PADDING: Float = 8f
        const val ICON_COUNT_X: Int = 15
        const val ICON_COUNT_Y: Int = 3

        const val TRACK_COUNT: Int = 5

        const val MESSAGE_BAR_HEIGHT: Int = 14

        val TRANSLUCENT_BLACK: Color = Color(0f, 0f, 0f, 0.5f)
    }

    val camera: OrthographicCamera by lazy {
        val c = OrthographicCamera()
        c.setToOrtho(false, RHRE3.WIDTH.toFloat(), RHRE3.HEIGHT.toFloat())
        c.update()
        c
    }

    val stage: EditorStage = EditorStage(null, stageCamera, main)
    val batch: SpriteBatch
        get() = main.batch

    /**
     * Pre-stage render.
     */
    fun render() {
        Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()

        // entity camera
        batch.projectionMatrix = camera.combined
        batch.begin()

        batch.end()

    }

    fun postStageRender() {

    }

    fun renderUpdate() {

    }

    override fun dispose() {
    }

    fun onResize(width: Float, height: Float) {
        stage.onResize(width, height)
    }
}