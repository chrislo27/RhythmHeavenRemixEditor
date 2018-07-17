package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import io.github.chrislo27.toolboks.font.FreeTypeFont


data class UIPalette(var ftfont: FreeTypeFont, var ftTitleFont: FreeTypeFont, var fontScale: Float,
                var textColor: Color,
                var backColor: Color, var highlightedBackColor: Color, var clickedBackColor: Color) {

    val font: BitmapFont
        get() = ftfont.font!!

    val titleFont: BitmapFont
        get() = ftTitleFont.font!!

}