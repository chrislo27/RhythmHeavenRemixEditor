package io.github.chrislo27.rhre3.screen

import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawRect
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class AssetRegistryLoadingScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, AssetRegistryLoadingScreen>(main) {

    private var nextScreen: (() -> ToolboksScreen<*, *>?)? = null

    override fun render(delta: Float) {
        super.render(delta)
        val progress = AssetRegistry.load(delta)

        val batch = main.batch

        batch.setColor(1f, 1f, 1f, 1f)

        val width = main.defaultCamera.viewportWidth * 0.75f
        val height = main.defaultCamera.viewportHeight * 0.05f
        val line = height / 8f

        batch.begin()

        batch.fillRect(main.defaultCamera.viewportWidth * 0.5f - width * 0.5f,
                       main.defaultCamera.viewportHeight * 0.5f - (height) * 0.5f,
                       width * progress, height)
        batch.drawRect(main.defaultCamera.viewportWidth * 0.5f - width * 0.5f - line * 2,
                       main.defaultCamera.viewportHeight * 0.5f - (height) * 0.5f - line * 2,
                       width + (line * 4), height + (line * 4),
                       line)

        batch.end()

        if (progress >= 1f) {
            main.screen = nextScreen?.invoke()
        }
    }

    fun setNextScreen(next: (() -> ToolboksScreen<*, *>?)?): AssetRegistryLoadingScreen {
        nextScreen = next
        return this
    }

    override fun show() {
        super.show()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}