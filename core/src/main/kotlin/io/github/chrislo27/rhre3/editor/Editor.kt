package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.RHRE3Application


class Editor(val main: RHRE3Application, val camera: OrthographicCamera)
    : Disposable {

    val stage: EditorStage = EditorStage(null, camera)
    val batch: SpriteBatch
        get() = main.batch

    /**
     * Pre-stage render.
     */
    fun render() {

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