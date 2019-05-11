package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ToolboksScreen


abstract class Checkbox<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : Button<S>(palette, parent, stage) {

    abstract val uncheckedTex: TextureRegion?
    abstract val checkedTex: TextureRegion?

    val checkLabel: ImageLabel<S> = ImageLabel(palette, this, stage).apply {
        this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
    }
    val textLabel: TextLabel<S> = TextLabel(palette, this, stage).apply {
        this.textAlign = Align.left
    }

    var checked: Boolean = false
        set(value) {
            field = value
            checkLabel.image = if (field) checkedTex else uncheckedTex
        }

    init {
        addLabel(checkLabel)
        addLabel(textLabel)

        checked = false
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (enabled) {
            checked = !checked
        }
    }

    open fun computeCheckWidth(): Float {
        val realWidth = this.location.realWidth
        val realHeight = this.location.realHeight
        return realHeight / realWidth
    }

    open fun computeTextX(): Float {
        return computeCheckWidth() * 1.25f
    }

    override fun onResize(width: Float, height: Float, pixelUnitX: Float, pixelUnitY: Float) {
        super.onResize(width, height, pixelUnitX, pixelUnitY)
        // Change checkLabel and textLabel size
        checkLabel.location.set(0f, 0f, computeCheckWidth(), 1f, 0f, 0f, 0f, 0f)
        val textX = computeTextX()
        textLabel.location.set(textX, 0f, 1f - textX, 1f, 0f, 0f, 0f, 0f)
        labels.forEach {
            it.onResize(this.location.realWidth, this.location.realHeight, pixelUnitX, pixelUnitY)
        }
    }
}