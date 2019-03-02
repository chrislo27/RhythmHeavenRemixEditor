package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


open class Button<S : ToolboksScreen<*, *>>
    : UIElement<S>, Palettable, Backgrounded {

    override var palette: UIPalette

    constructor(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>) : super(parent, stage) {
        this.palette = palette
        this.labels = mutableListOf()
    }

    val labels: List<Label<S>>
    var enabled = true
    override var background: Boolean = true

    fun addLabel(l: Label<S>) {
        if (l.parent !== this) {
            throw IllegalArgumentException("Label parent must be this")
        }
        labels as MutableList
        if (l !in labels) {
            labels.add(l)
        }
    }

    fun addLabel(index: Int, l: Label<S>) {
        if (l.parent !== this) {
            throw IllegalArgumentException("Label parent must be this")
        }
        labels as MutableList
        if (l !in labels) {
            labels.add(index.coerceIn(0, labels.size), l)
        }
    }

    fun removeLabel(l: Label<S>) {
        if (l.parent !== this) {
            throw IllegalArgumentException("Label parent must be this")
        }
        labels as MutableList
        labels.remove(l)
    }

    override fun canBeClickedOn(): Boolean {
        return enabled
    }

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        if (background) {
            val oldBatchColor = batch.color

            if (wasClickedOn && enabled) {
                batch.color = palette.clickedBackColor
            } else if (isMouseOver() && enabled) {
                batch.color = palette.highlightedBackColor
            } else {
                batch.color = palette.backColor
            }

            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)

            batch.color = oldBatchColor
        }

        labels.forEach {
            if (it.visible) {
                it.render(screen, batch, shapeRenderer)
            }
        }

        if (!enabled) {
            val oldBatchColor = batch.packedColor

            batch.setColor(0.15f, 0.15f, 0.15f, 0.75f)

            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)

            batch.packedColor = oldBatchColor
        }
    }

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        labels.forEach {
            it.onResize(this.location.realWidth, this.location.realHeight)
        }
    }
}