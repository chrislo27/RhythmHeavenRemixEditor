package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class TestAffineScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, TestAffineScreen>(main) {

    override fun render(delta: Float) {
        val batch = main.batch
        batch.begin()
        
        batch.setColor(0.9f, 0.9f, 0.9f, 1f)
        batch.fillRect(0f, 0f, 1280f, 720f)
        batch.setColor(1f, 0f, 0f, 1f)
        batch.fillRect(512f, 0f, 1f, 720f)
        batch.setColor(0f, 0f, 1f, 1f)
        batch.fillRect(0f, 512f, 1280f, 1f)
        batch.setColor(1f, 1f, 1f, 1f)
        val tex: Texture = AssetRegistry["logo_256"]
        val width = 48f * -1f
        val height = 32f
        val x = 504f
        val y = 483f
        val originX = width / 2f
        val originY = height / 2f
        val scaleX = 1f
        val scaleY = 1f
        val rotation = -115.06123352050781f // 115.06123352050781
        val flipX = false
        val flipY = false
        
        // width * stretchX
        // rotation stays the same
        // gdx scaleX and scaleY must always be 1
        
        batch.draw(tex, x, y + height, originX, originY, width, height, scaleX, scaleY, rotation, 0, 0, 256, 256, flipX, flipY)

        batch.setColor(0f, 1f, 0f, 1f)
        batch.fillRect(x + originX - 2f, y + originY - 2f, 4f, 4f)
        batch.setColor(1f, 1f, 1f, 1f)
        batch.end()
        
        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            main.screen = ScreenRegistry["info"]
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}