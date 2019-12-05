package io.github.chrislo27.rhre3.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import java.util.*
import kotlin.math.roundToInt


class ColourPicker<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>, val hasAlpha: Boolean = false)
    : Stage<S>(parent, stage.camera, stage.pixelsWidth, stage.pixelsHeight) {

    data class HSVA(var hue: Float, var saturation: Float, var value: Float, var alpha: Int = 255)

    private val tmpArr = FloatArray(3)
    private val noSat = Color(1f, 1f, 1f, 1f)
    private val maxSat = Color(1f, 1f, 1f, 1f)
    val hsv: HSVA = HSVA(0f, 1f, 1f)
    val currentColour = Color(1f, 1f, 1f, 1f)

    val hex: TextField<S>
    val display: ColourPane<S>
    val hue: ImageLabel<S>
    val saturation: SatBar
    val value: ValueBar
    val alpha: AlphaBar
    val hueField: TextField<S>
    val satField: TextField<S>
    val valueField: TextField<S>
    val alphaField: TextField<S>
    val hueArrow: MovingArrow
    val satArrow: MovingArrow
    val valueArrow: MovingArrow
    val alphaArrow: MovingArrow

    val clearButton: Button<S>
    val copyButton: Button<S>

    var onColourChange: (color: Color) -> Unit = {}
    
    val textFieldsHaveFocus: Boolean
        get() = hex.hasFocus || hueField.hasFocus || satField.hasFocus || valueField.hasFocus || alphaField.hasFocus

    init {
        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.5f)
        }
        val labelWidth = 0.15f
        val labelHeight = if (hasAlpha) 0.2f else 0.25f
        // HSV labels
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "H: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = labelHeight, screenY = 1f - (labelHeight))
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "S: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = labelHeight, screenY = 1f - (labelHeight * 2))
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "V: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = labelHeight, screenY = 1f - (labelHeight * 3))
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "A: "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = labelHeight, screenY = 1f - (labelHeight * 4))
            this.visible = hasAlpha
        }
        elements += TextLabel(palette, this, this).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
            this.text = "# "
            this.textAlign = Align.right
            this.location.set(screenWidth = labelWidth, screenHeight = labelHeight, screenY = 0f)
        }

        hue = ImageLabel(palette, this, this).apply {
            this.renderType = ImageLabel.ImageRendering.RENDER_FULL
            this.image = TextureRegion(RHRE3Application.instance.hueBar)
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = labelHeight * 0.6f, screenY = 1f - (labelHeight) + 0.05f)
        }
        elements += hue
        saturation = SatBar(this).apply {
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = labelHeight * 0.6f, screenY = 1f - (labelHeight * 2) + 0.05f)
        }
        elements += saturation
        value = ValueBar(this).apply {
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = labelHeight * 0.6f, screenY = 1f - (labelHeight * 3) + 0.05f)
        }
        elements += value
        alpha = AlphaBar(this).apply {
            this.location.set(screenX = labelWidth, screenWidth = 1f - labelWidth * 3, screenHeight = labelHeight * 0.6f, screenY = 1f - (labelHeight * 4) + 0.05f)
            this.visible = hasAlpha
        }
        elements += alpha
        hueArrow = MovingArrow(this).apply {
            this.location.set(hue.location)
            this.onPercentageChange = {
                hsv.hue = it * 360f
                onHsvChange(true)
            }
        }
        elements += hueArrow
        satArrow = MovingArrow(this).apply {
            this.location.set(saturation.location)
            this.onPercentageChange = {
                hsv.saturation = it
                onHsvChange(true)
            }
        }
        elements += satArrow
        valueArrow = MovingArrow(this).apply {
            this.location.set(value.location)
            this.onPercentageChange = {
                hsv.value = it
                onHsvChange(true)
            }
        }
        elements += valueArrow
        alphaArrow = MovingArrow(this).apply {
            this.location.set(alpha.location)
            this.visible = hasAlpha
            this.onPercentageChange = {
                hsv.alpha = (it * 255f).roundToInt().coerceIn(0, 255)
                onHsvChange(true)
            }
        }
        elements += alphaArrow

        hueField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.hue = text.toIntOrNull()?.toFloat()?.coerceIn(0f, 360f) ?: 0f
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = labelHeight * 0.8f, screenY = 1f - (labelHeight) + 0.025f)
        }
        elements += hueField
        satField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.saturation = text.toIntOrNull()?.toFloat()?.coerceIn(0f, 100f)?.div(100f) ?: 0f
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = labelHeight * 0.8f, screenY = 1f - (labelHeight * 2) + 0.025f)
        }
        elements += satField
        valueField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.value = text.toIntOrNull()?.toFloat()?.coerceIn(0f, 100f)?.div(100f) ?: 0f
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = labelHeight * 0.8f, screenY = 1f - (labelHeight * 3) + 0.025f)
        }
        elements += valueField
        alphaField = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                hsv.alpha = text.toIntOrNull()?.coerceIn(0, 255) ?: 0
                onHsvChange(true)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = 3
            this.textAlign = Align.left
            this.visible = hasAlpha
            this.location.set(screenX = 1f - labelWidth * 2 + 0.025f, screenWidth = labelWidth * 2 - 0.05f, screenHeight = labelHeight * 0.8f, screenY = 1f - (labelHeight * 4) + 0.025f)
        }
        elements += alphaField

        hex = object : TextField<S>(palette, this@ColourPicker, this@ColourPicker) {
            override fun onEnterPressed(): Boolean {
                hasFocus = false
                val c = Color.valueOf(this.text.padEnd(this.characterLimit, 'f'))
                setColor(c)
                return true
            }
        }.apply {
            this.background = true
            this.canInputNewlines = false
            this.canPaste = true
            val acceptedChars = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F')
            this.canTypeText = { it in acceptedChars }
            this.characterLimit = if (hasAlpha) 8 else 6
            this.textAlign = Align.left
            this.location.set(screenX = labelWidth, screenWidth = 0.4f, screenHeight = labelHeight * 0.8f, screenY = 0.025f)
        }
        elements += hex
        copyButton = Button(palette, this@ColourPicker, this@ColourPicker).apply {
            this.location.set(screenX = hex.location.screenWidth + hex.location.screenX + 0.025f, screenWidth = labelWidth / 3,
                    screenHeight = labelHeight * 0.8f, screenY = 0.025f)
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "colourPicker.copy"
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_copy"))
            })
            this.leftClickAction = { _, _ ->
                Gdx.app.clipboard.contents = currentColour.toString().toUpperCase(Locale.ROOT)
            }
        }
        elements += copyButton
        clearButton = Button(palette, this@ColourPicker, this@ColourPicker).apply {
            this.location.set(screenX = hex.location.screenWidth + hex.location.screenX + 0.025f * 2 + labelWidth / 3, screenWidth = labelWidth / 3,
                    screenHeight = labelHeight * 0.8f, screenY = 0.025f)
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "colourPicker.clear"
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_x"))
                this.tint.set(1f, 0.25f, 0.25f, 1f)
            })
            this.leftClickAction = { _, _ ->
                hex.text = ""
                hex.hasFocus = true
            }
        }
        elements += clearButton

        display = ColourPane(this, this).apply {
            this.location.set(screenX = 0.725f, screenWidth = 0.25f, screenHeight = labelHeight * 0.8f, screenY = 0.025f)
        }
        elements += CheckerboardBacking(this).apply {
            this.location.set(display.location)
        }
        elements += display

        setColor(Color.WHITE)
    }

    fun setColor(color: Color, triggerListener: Boolean = true) {
        val arr = tmpArr
        color.toHsv(arr)
        hsv.hue = arr[0]
        hsv.saturation = arr[1]
        hsv.value = arr[2]
        hsv.alpha = (color.a * 255f).roundToInt().coerceIn(0, 255)
        onHsvChange(triggerListener)
    }

    private fun onHsvChange(triggerListener: Boolean) {
        currentColour.fromHsv(hsv.hue, hsv.saturation, hsv.value)
        currentColour.a = hsv.alpha / 255f
        hex.text = currentColour.toString().toUpperCase(Locale.ROOT).take(if (hasAlpha) 8 else 6)
        maxSat.fromHsv(hsv.hue, 1f, hsv.value)
        noSat.fromHsv(hsv.hue, 0f, hsv.value)
        display.colour.set(currentColour)
        hueField.text = hsv.hue.toInt().coerceIn(0, 360).toString()
        satField.text = (hsv.saturation * 100).roundToInt().coerceIn(0, 100).toString()
        valueField.text = (hsv.value * 100).roundToInt().coerceIn(0, 100).toString()
        alphaField.text = (hsv.alpha).coerceIn(0, 255).toString()
        // update sliders
        hueArrow.percentage = hsv.hue.coerceIn(0f, 360f) / 360f
        satArrow.percentage = hsv.saturation.coerceIn(0f, 1f)
        valueArrow.percentage = hsv.value.coerceIn(0f, 1f)
        alphaArrow.percentage = hsv.alpha.coerceIn(0, 255) / 255f

        if (triggerListener)
            onColourChange(currentColour)
    }

    inner class ValueBar(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        private val tmpColor: Color = Color(1f, 1f, 1f, 1f)
        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            tmpColor.set(currentColour)
            tmpColor.a = 1f
            batch.drawQuad(location.realX, location.realY, Color.BLACK, location.realX + location.realWidth, location.realY, tmpColor,
                    location.realX + location.realWidth, location.realY + location.realHeight, tmpColor,
                    location.realX, location.realY + location.realHeight, Color.BLACK)
        }
    }

    inner class SatBar(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            batch.drawQuad(location.realX, location.realY, noSat, location.realX + location.realWidth, location.realY, maxSat,
                    location.realX + location.realWidth, location.realY + location.realHeight, maxSat,
                    location.realX, location.realY + location.realHeight, noSat)
        }
    }

    inner class AlphaBar(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        private val noAlpha: Color = Color(1f, 1f, 1f, 1f)
        private val fullAlpha: Color = Color(1f, 1f, 1f, 1f)
        private val texRegion: TextureRegion by lazy {
            val tex = AssetRegistry.get<Texture>("ui_transparent_checkerboard")
            tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
            TextureRegion(tex, 0, 0, 16, 16)
        }

        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            noAlpha.set(currentColour)
            noAlpha.a = 0f
            fullAlpha.set(currentColour)
            fullAlpha.a = 1f

            batch.setColor(1f, 1f, 1f, 1f)
            texRegion.setRegion(0, 0, location.realWidth.toInt(), location.realHeight.toInt())
            batch.draw(texRegion, location.realX, location.realY, location.realWidth, location.realHeight)
            batch.drawQuad(location.realX, location.realY, noAlpha, location.realX + location.realWidth, location.realY, fullAlpha,
                    location.realX + location.realWidth, location.realY + location.realHeight, fullAlpha,
                    location.realX, location.realY + location.realHeight, noAlpha)
        }
    }

    inner class CheckerboardBacking(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        private val texRegion: TextureRegion by lazy {
            val tex = AssetRegistry.get<Texture>("ui_transparent_checkerboard")
            tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
            TextureRegion(tex, 0, 0, 16, 16)
        }

        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            batch.setColor(1f, 1f, 1f, 1f)
            texRegion.setRegion(0, 0, location.realWidth.toInt(), location.realHeight.toInt())
            batch.draw(texRegion, location.realX, location.realY, location.realWidth, location.realHeight)
        }
    }

    inner class MovingArrow(parent: ColourPicker<S>) : UIElement<S>(parent, parent) {
        var percentage = 0f

        var onPercentageChange: (value: Float) -> Unit = {}

        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            if (wasClickedOn && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                val old = percentage
                percentage = ((stage.camera.getInputX() - location.realX) / location.realWidth).coerceIn(0f, 1f)
                if (percentage != old) {
                    onPercentageChange(percentage)
                }
            }

            val tex = AssetRegistry.get<Texture>("ui_colour_picker_arrow")
            val height = location.realHeight / 2f
            batch.setColor(1f, 1f, 1f, 1f)
            batch.draw(tex, location.realX + location.realWidth * percentage - height / 2, location.realY,
                    height, height)
        }

        override fun canBeClickedOn(): Boolean {
            return true
        }
    }
}