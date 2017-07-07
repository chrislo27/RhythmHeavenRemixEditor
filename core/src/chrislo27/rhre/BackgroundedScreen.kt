package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.screen.Updateable
import ionium.util.MathHelper


open class BackgroundedScreen(m: Main) : Updateable<Main>(m) {
    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        main.batch.begin()

        val tex: Texture = AssetRegistry.getTexture("ui_bg")
        val start = MathHelper.getSawtoothWave(5f) - 1f
        for (x in (start * tex.width).toInt()..main.camera.viewportWidth.toInt() step tex.width) {
            for (y in (start * tex.height).toInt()..main.camera.viewportHeight.toInt() step tex.height) {
                main.batch.draw(tex, x.toFloat(), y.toFloat())
            }
        }

        main.batch.end()
    }

    override fun renderUpdate() {
    }

    override fun tickUpdate() {
    }

    override fun getDebugStrings(array: Array<String>?) {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun show() {
    }

    override fun hide() {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }
}