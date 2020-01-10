package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class AssetRegistryLoadingScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, AssetRegistryLoadingScreen>(main) {

    private val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, RHRE3.WIDTH * 1f, RHRE3.HEIGHT * 1f)
    }
    private var nextScreen: (() -> ToolboksScreen<*, *>?)? = null
    private var logoTex: Texture? = null
    private var firstFrame = true

    override fun render(delta: Float) {
        
        if (logoTex == null) {
            logoTex = Texture(Gdx.files.internal("images/logo_with_name.png")).apply {
                setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
        }
        val logo: Texture? = logoTex
        val progress = if (firstFrame) run {
            firstFrame = false
            0f
        } else AssetRegistry.load(delta)

        val cam = camera
        val batch = main.batch
        batch.projectionMatrix = cam.combined

        batch.setColor(1f, 1f, 1f, 1f)

        val viewportWidth = cam.viewportWidth
        val viewportHeight = cam.viewportHeight
        val width = viewportWidth * 0.75f
        val height = viewportHeight * 0.05f
        val offsetY = -220f
        val line = height / 8f

        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)
        
        if (logo != null) {
            val logoSize = 0.9f
            batch.setColor(1f, 1f, 1f, 1f)
            batch.draw(logo, viewportWidth * 0.5f - (logo.width * logoSize) * 0.5f, viewportHeight * 0.35f, logo.width * logoSize, logo.height * logoSize)
        }

        batch.fillRect(viewportWidth * 0.5f - width * 0.5f,
                       viewportHeight * 0.5f - (height) * 0.5f + offsetY,
                       width * progress, height)
        batch.drawRect(viewportWidth * 0.5f - width * 0.5f - line * 2,
                       viewportHeight * 0.5f - (height) * 0.5f - line * 2 + offsetY,
                       width + (line * 4), height + (line * 4),
                       line)

        val step = (MathHelper.getSawtoothWave(0.25f) * 4).toInt()
        val playYanWidth = width - 26f + 2f
        batch.draw(AssetRegistry.get<Texture>("playyan_walking"), viewportWidth * 0.5f - width * 0.5f + playYanWidth * progress,
                   viewportHeight * 0.5f + height * 0.5f + line * 2 + offsetY,
                   26f, 35f,
                   step * 26, 0, 26, 35, false, false)

        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
        
        super.render(delta)

        if (progress >= 1f) {
            main.screen = nextScreen?.invoke()
        }
    }

    fun setNextScreen(next: (() -> ToolboksScreen<*, *>?)?): AssetRegistryLoadingScreen {
        nextScreen = next
        return this
    }

    override fun hide() {
        super.hide()
        Gdx.app.postRunnable {
            logoTex?.dispose()
            logoTex = null
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        logoTex?.dispose()
        logoTex = null
    }
}