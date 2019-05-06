package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth


/**
 * A stage is the main root container for most [UIElement]s.
 */
open class Stage<S : ToolboksScreen<*, *>>(parent: UIElement<S>?, val camera: OrthographicCamera,
                                           val pixelsWidth: Float = -1f, val pixelsHeight: Float = -1f)
    : UIElement<S>(parent, null), InputProcessor {

    companion object {
        private val TMP_MATRIX = Matrix4()
    }

    private inner class TooltipData(var tooltip: String, var element: UIElement<S>?)

    override val stage: Stage<S>
        get() = this
    val elements: MutableList<UIElement<S>> = mutableListOf()
    private val tooltipData: TooltipData = TooltipData("", null)
    override var tooltipText: String? = null
    var tooltipElement: TextLabel<S>? = null
        set(value) {
            val old = field
            if (old != null) {
                removeChild(old)
            }
            if (value != null && value !in elements) {
                elements += value
            }
            field = value
        }

    @Volatile
    private var calledFromUpdatePositions: Boolean = false

    init {
        this.location.set(screenWidth = 1f, screenHeight = 1f)
        this.updatePositions()
    }

    override fun removeChild(element: UIElement<S>): Boolean {
        return elements.remove(element)
    }

    // Recursive
    private fun findTooltip(root: Stage<S>): TooltipData? {
        tooltipData.apply {
            tooltip = ""
            element = null
        }
        for (e in root.elements) {
            if (e is Stage<S>) {
                return findTooltip(e) ?: continue
            }
        }
        for (e in root.elements) {
            if (e !is Stage<S> && e.isMouseOver()) {
                val t = e.tooltipText
                if (t != null) {
                    return tooltipData.apply {
                        tooltip = if (e.tooltipTextIsLocalizationKey) Localization[t] else t
                        element = e
                    }
                }
            }
        }
        return null
    }

    override fun render(screen: S, batch: SpriteBatch,
                        shapeRenderer: ShapeRenderer) {
        camera.update()
        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = camera.combined

        // tooltip
        val tooltip = findTooltip(this)
        val tooltipLabel = this.tooltipElement
        if (tooltipLabel != null) {
            if (tooltip != null) {
                tooltipLabel.visible = true
                tooltipLabel.isLocalizationKey = false
                tooltipLabel.text = tooltip.tooltip
                // Positioning
                val font = tooltipLabel.getFont()
                font.data.setScale(tooltipLabel.palette.fontScale * tooltipLabel.fontScaleMultiplier)
                val loc = tooltipLabel.location
                // Initial set
                loc.set(screenWidth = 0f, screenHeight = 0f, screenX = 0f, screenY = 0f,
                        pixelX = camera.getInputX(), pixelY = camera.getInputY() + 2,
                        pixelWidth = font.getTextWidth(tooltipLabel.text) + 6,
                        pixelHeight = font.getTextHeight(tooltipLabel.text) + font.capHeight)
                // Clamp Y
                val yLimit = camera.viewportHeight
                val top = loc.pixelY + loc.pixelHeight
                if (top > yLimit) {
                    val height = loc.pixelHeight
                    loc.set(pixelY = yLimit - height, pixelX = loc.pixelX + ((top - yLimit) / height).coerceAtMost(1f) * height)
                }
                // Clamp X, flip to left side if necessary
                val xLimit = camera.viewportWidth - loc.pixelWidth
                if (loc.pixelX > xLimit) {
                    loc.set(pixelX = camera.getInputX() - loc.pixelWidth)
                }
                font.data.setScale(1f)
                // Resize/update real position
                val w = parent?.location?.realWidth ?: camera.viewportWidth
                val h = parent?.location?.realHeight ?: camera.viewportHeight
                val pxW = if (pixelsWidth > 0f) w / pixelsWidth else 1f
                val pxH = if (pixelsHeight > 0f) h / pixelsHeight else 1f
                tooltipLabel.onResize(this.location.realWidth, this.location.realHeight, pxW, pxH)
                if (Toolboks.debugMode) {
                    println(tooltipLabel.location)
                }
            } else {
                tooltipLabel.visible = false
            }
        }

        elements.filter(UIElement<S>::visible).forEach {
            if (it !== tooltipLabel) {
                it.render(screen, batch, shapeRenderer)
            }
        }
        tooltipLabel?.takeIf { it.visible }?.render(screen, batch, shapeRenderer)
        batch.setColor(1f, 1f, 1f, 1f)

        batch.projectionMatrix = TMP_MATRIX
    }

    override fun drawOutline(batch: SpriteBatch, camera: OrthographicCamera, lineThickness: Float, onlyVisible: Boolean) {
        if (!onlyVisible || this.visible) {
            if (camera !== this.camera)
                error("Camera passed in wasn't the stage's camera")
            val old = batch.packedColor
            batch.color = Color.ORANGE
            super.drawOutline(batch, camera, lineThickness, onlyVisible)
            batch.packedColor = old
            elements.forEach {
                it.drawOutline(batch, this.camera, lineThickness, onlyVisible)
            }
        }
    }

    override fun frameUpdate(screen: S) {
        elements.forEach {
            it.frameUpdate(screen)
        }
    }

    override fun tickUpdate(screen: S) {
        super.tickUpdate(screen)
        elements.forEach {
            it.tickUpdate(screen)
        }
    }

    fun updatePositions() {
        if (calledFromUpdatePositions) {
            error("Unthread-safe use of updatePositions")
        }
        calledFromUpdatePositions = true
        val w = parent?.location?.realWidth ?: camera.viewportWidth
        val h = parent?.location?.realHeight ?: camera.viewportHeight
        val pxW = if (pixelsWidth > 0f) w / pixelsWidth else 1f
        val pxH = if (pixelsHeight > 0f) h / pixelsHeight else 1f
        onResize(w, h, pxW, pxH)
    }

    override fun onResize(width: Float, height: Float, pixelUnitX: Float, pixelUnitY: Float) {
        val calledFromUpdatePositions = calledFromUpdatePositions
        this.calledFromUpdatePositions = false
        if (parent == null && !calledFromUpdatePositions) {
            error("onResize cannot be called without a parent. Use updatePositions instead.")
        }
        super.onResize(width, height, pixelUnitX, pixelUnitY)
        if (elements.any { it.parent !== this }) {
            error("Elements ${elements.filter { it.parent !== this }.map { "[$it, parent=${it.parent}]" }} do not have this as their parent")
        }
        elements.forEach { it.onResize(this.location.realWidth, this.location.realHeight, pixelUnitX, pixelUnitY) }
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.touchUp(screenX, screenY, pointer, button) }.any()
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.mouseMoved(screenX, screenY) }.any()
    }

    override fun keyTyped(character: Char): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.keyTyped(character) }.any()
    }

    override fun scrolled(amount: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.scrolled(amount) }.any()
    }

    override fun keyUp(keycode: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.keyUp(keycode) }.any()
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.touchDragged(screenX, screenY, pointer) }.any()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.keyDown(keycode) }.any()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!visible)
            return false
        return elements.filter { it.visible && it.touchDown(screenX, screenY, pointer, button) }.any()
    }
}