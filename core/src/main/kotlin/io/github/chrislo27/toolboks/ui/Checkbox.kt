package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.toolboks.ToolboksScreen


abstract class Checkbox<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : Button<S>(palette, parent, stage) {

    open val checkLabelPortion: Float = 0.25f

    abstract val uncheckedTex: TextureRegion?
    abstract val checkedTex: TextureRegion?

    val checkLabel: ImageLabel<S> = ImageLabel(palette, this, stage).apply {
        this.location.set(screenWidth = checkLabelPortion)
        this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
    }
    val textLabel: TextLabel<S> = TextLabel(palette, this, stage).apply {
        this.location.set(screenX = checkLabelPortion, screenWidth = 1f - checkLabelPortion)
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
}