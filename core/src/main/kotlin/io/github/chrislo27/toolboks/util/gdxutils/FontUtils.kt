package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Pool


private val glyphLayoutPool: Pool<GlyphLayout> = object : Pool<GlyphLayout>(32) {
    override fun newObject(): GlyphLayout {
        return GlyphLayout()
    }
}

fun BitmapFont.getTextWidth(text: String): Float {
    val glyphLayout = glyphLayoutPool.obtain()
    glyphLayout.setText(this, text)
    val width = glyphLayout.width
    glyphLayoutPool.free(glyphLayout)
    return width
}

fun BitmapFont.getTextHeight(text: String): Float {
    val glyphLayout = glyphLayoutPool.obtain()
    glyphLayout.setText(this, text)
    val height = glyphLayout.height
    glyphLayoutPool.free(glyphLayout)
    return height
}

fun BitmapFont.getTextWidth(text: String, width: Float, wrap: Boolean): Float {
    val glyphLayout = glyphLayoutPool.obtain()
    glyphLayout.setText(this, text, Color.WHITE, width, Align.left, wrap)
    val w = glyphLayout.width
    glyphLayoutPool.free(glyphLayout)
    return w
}

fun BitmapFont.getTextHeight(text: String, width: Float, wrap: Boolean): Float {
    val glyphLayout = glyphLayoutPool.obtain()
    glyphLayout.setText(this, text, Color.WHITE, width, Align.left, wrap)
    val height = glyphLayout.height
    glyphLayoutPool.free(glyphLayout)
    return height
}

/**
 * Multiplies the current scale by x and y
 */
fun BitmapFont.scaleMul(x: Float, y: Float) {
    this.data.setScale(this.scaleX * x, this.scaleY * y)
}

fun BitmapFont.scaleMul(coefficient: Float) {
    this.scaleMul(coefficient, coefficient)
}

fun BitmapFont.drawCompressed(batch: SpriteBatch, text: String, x: Float, y: Float, width: Float,
                              align: Int): GlyphLayout {
    val font = this
    val textWidth = this.getTextWidth(text)
    val oldScaleX = font.data.scaleX
    var newScaleX = oldScaleX
    
    if (textWidth > width) {
        newScaleX = (width / textWidth) * oldScaleX
    }
    
    font.data.setScale(newScaleX, font.data.scaleY)
    val layout = font.draw(batch, text, x, y, width, align, false)
    font.data.setScale(oldScaleX, font.data.scaleY)
    
    return layout
}

fun BitmapFont.drawConstrained(batch: SpriteBatch, text: String, x: Float, y: Float, width: Float, height: Float,
                               align: Int): GlyphLayout {
    val font = this
    val textWidth = this.getTextWidth(text)
    val textHeight = this.getTextHeight(text, width, true)
    val oldScaleX = font.data.scaleX
    val oldScaleY = font.data.scaleY
    var newScaleX = oldScaleX
    var newScaleY = oldScaleY
    
    if (textWidth > width) {
        newScaleX = (width / textWidth) * oldScaleX
    }
    
    if (textHeight > height) {
        newScaleY = (height / textHeight) * oldScaleY
    }

    font.data.setScale(newScaleX, newScaleY)
    val layout = font.draw(batch, text, x, y, width, align, true)
    font.data.setScale(oldScaleX, oldScaleY)
    
    return layout
}

fun FreeTypeFontGenerator.FreeTypeFontParameter.copy(): FreeTypeFontGenerator.FreeTypeFontParameter =
        FreeTypeFontGenerator.FreeTypeFontParameter().also {
            it.size = this.size
            it.mono = this.mono
            it.hinting = this.hinting
            it.color = this.color.cpy()
            it.gamma = this.gamma
            it.renderCount = this.renderCount
            it.borderColor = this.borderColor.cpy()
            it.borderGamma = this.borderGamma
            it.borderStraight = this.borderStraight
            it.borderWidth = this.borderWidth
            it.characters = this.characters
            it.shadowColor = this.shadowColor.cpy()
            it.shadowOffsetX = this.shadowOffsetX
            it.shadowOffsetY = this.shadowOffsetY
            it.spaceX = this.spaceX
            it.spaceY = this.spaceY
            it.kerning = this.kerning
            it.flip = this.flip
            it.genMipMaps = this.genMipMaps
            it.incremental = this.incremental
            it.minFilter = this.minFilter
            it.magFilter = this.magFilter
            it.padTop = this.padTop
            it.padBottom = this.padBottom
            it.padLeft = this.padLeft
            it.padRight = this.padRight
        }
