package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import kotlin.math.max


class GlassEffect(val main: RHRE3Application, val editor: Editor) {

    companion object {
        private val VERT = """
attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
uniform mat4 u_projTrans;

varying vec4 vColor;
varying vec2 vTexCoord;
void main() {
	vColor = ${ShaderProgram.COLOR_ATTRIBUTE};
	vTexCoord = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
	gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}
"""

        private val FRAG =
                """
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif
varying LOWP vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D u_texture;
uniform float resolution;
uniform float radius;
uniform vec2 dir;

void main() {
    /* Slightly modified from https://github.com/Jam3/glsl-fast-gaussian-blur/blob/5dbb6e97aa43d4be9369bdd88e835f47023c5e2a/5.glsl */
    vec4 color = vec4(0.0);
    vec2 off1 = vec2(1.3333333333333333) * direction;
    color += texture2D(u_texture, vTexCoord) * 0.29411764705882354;
    color += texture2D(u_texture, vTexCoord + (off1 / resolution)) * 0.35294117647058826;
    color += texture2D(u_texture, vTexCoord - (off1 / resolution)) * 0.35294117647058826;
    gl_FragColor = vColor * vec4(color.rgb, 1.0);
}
"""

        @Suppress("FunctionName")
        private fun <T> Try(instantiator: () -> T): T? = try {
            instantiator()
        } catch (e: Exception) {
            null
        }
    }

    val shader: ShaderProgram = createShaderProgram()
    val shaderCamera: OrthographicCamera = OrthographicCamera(RHRE3.WIDTH.toFloat(), RHRE3.HEIGHT.toFloat()).apply {
        setToOrtho(false, viewportWidth, viewportHeight)
    }
    private val bufferA: FrameBuffer? = Try {
        FrameBuffer(Pixmap.Format.RGBA8888, RHRE3.WIDTH, RHRE3.HEIGHT, false, true).apply {
            this.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            main.addDisposeCall(Runnable(this::dispose))
        }
    }
    private val bufferB: FrameBuffer? = Try {
        FrameBuffer(Pixmap.Format.RGBA8888, RHRE3.WIDTH, RHRE3.HEIGHT, false, true).apply {
            this.colorBufferTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            main.addDisposeCall(Runnable(this::dispose))
        }
    }
    val fboSupported: Boolean = bufferA != null && bufferB != null
    val fboRegion: TextureRegion? = if (fboSupported && bufferB != null) TextureRegion(bufferB.colorBufferTexture).apply {
        flip(false, true)
    } else null

    private val batch: SpriteBatch get() = editor.batch

    fun createShaderProgram(): ShaderProgram {
        ShaderProgram.pedantic = false
        val shader = ShaderProgram(VERT, FRAG)
        if (!shader.isCompiled) {
            error("Failed to compile glass effect shader:\n${shader.log}")
        }
        return shader
    }

    fun renderBackground() {
        if (bufferA != null && bufferB != null && fboRegion != null) {
            shaderCamera.update()

            // Render normal background to buffer A
            bufferA.begin()
            batch.projectionMatrix = shaderCamera.combined
            main.shapeRenderer.projectionMatrix = shaderCamera.combined

            editor.renderBackground(batch, main.shapeRenderer, shaderCamera, false)

            batch.flush()
            bufferA.end()

            batch.shader = shader
            batch.setColor(1f, 1f, 1f, 1f)
            shader.apply {
                setUniformf("dir", 0f, 1f)
                setUniformf("resolution", max(shaderCamera.viewportHeight, shaderCamera.viewportWidth))
                setUniformf("radius", 3f)
            }

            // Render buffer A back to buffer A, with first blur pass
            bufferA.begin()
            fboRegion.texture = bufferA.colorBufferTexture
            batch.draw(fboRegion, 0f, 0f)
            batch.flush()
            bufferA.end()

            // Render buffer A to buffer B, with second blur pass
            bufferB.begin()
            shader.setUniformf("dir", 1f, 0f)
            fboRegion.texture = bufferA.colorBufferTexture
            batch.draw(fboRegion, 0f, 0f)
            batch.flush()
            bufferB.end()
            
            bufferA.begin()
            fboRegion.texture = bufferB.colorBufferTexture
            batch.draw(fboRegion, 0f, 0f)
            batch.flush()
            bufferA.end()

            bufferB.begin()
            shader.setUniformf("dir", 1f, 0f)
            fboRegion.texture = bufferA.colorBufferTexture
            batch.draw(fboRegion, 0f, 0f)
            batch.flush()
            bufferB.end()

            batch.shader = null
            fboRegion.texture = bufferB.colorBufferTexture

            batch.projectionMatrix = main.defaultCamera.combined
            main.shapeRenderer.projectionMatrix = main.defaultCamera.combined
        }
    }

}

