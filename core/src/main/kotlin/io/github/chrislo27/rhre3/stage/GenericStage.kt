package io.github.chrislo27.rhre3.stage

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class GenericStage<S : ToolboksScreen<*, *>>(override var palette: UIPalette, parent: UIElement<S>?,
                                             camera: OrthographicCamera) : Stage<S>(parent, camera), Palettable {

    companion object {
        const val SCREEN_WIDTH = 0.85f
        const val SCREEN_HEIGHT = 0.9f
        val AREA = (RHRE3.WIDTH * SCREEN_WIDTH) to (RHRE3.HEIGHT * SCREEN_HEIGHT)

        const val PADDING: Float = 32f
        const val ICON_SIZE: Float = 128f
        const val BOTTOM_SIZE: Float = 96f

        val PADDING_RATIO = (PADDING / AREA.first) to (PADDING / AREA.second)
        val ICON_SIZE_RATIO = (ICON_SIZE / AREA.first) to (ICON_SIZE / AREA.second)
        val BOTTOM_RATIO = (BOTTOM_SIZE / AREA.first) to (BOTTOM_SIZE / AREA.second)
    }

    var titleIcon: ImageLabel<S> = ImageLabel(palette, this, this).apply {
        this.alignment = Align.topLeft
        this.location.set(0f, 0f,
                          ICON_SIZE_RATIO.first, ICON_SIZE_RATIO.second,
                          0f, 0f, 0f, 0f)
        this.location.screenY += this.location.screenHeight
    }
    var titleLabel: TextLabel<S> = object : TextLabel<S>(palette, this, this) {
        override fun getFont(): BitmapFont {
            return palette.titleFont
        }
    }.apply {
        this.alignment = Align.topLeft
        this.location.set(PADDING_RATIO.first * 0.5f + ICON_SIZE_RATIO.first,
                          ICON_SIZE_RATIO.second)
        this.location.set(screenWidth = 1f - this.location.screenX, screenHeight = ICON_SIZE_RATIO.second)

        this.textAlign = Align.left + Align.center
        this.textWrapping = false
        this.background = false
    }

    var centreStage: Stage<S> = Stage(this, camera).apply {
        this.alignment = Align.bottomLeft
        this.location.set(0f, BOTTOM_RATIO.second + PADDING_RATIO.second / 2f, 1f,
                          1f - (PADDING_RATIO.second / 2f + ICON_SIZE_RATIO.second) - (PADDING_RATIO.second / 2f + BOTTOM_RATIO.second))
    }

    var bottomStage: Stage<S> = Stage(this, camera).apply {
        this.alignment = Align.bottomLeft
        this.location.set(0f, 0f, 1f, BOTTOM_RATIO.second)
    }

    init {
        this.location.screenWidth = SCREEN_WIDTH - PADDING_RATIO.first * 2
        this.location.screenHeight = SCREEN_HEIGHT - PADDING_RATIO.second * 2
        this.location.screenX = 0.5f - this.location.screenWidth / 2f
        this.location.screenY = 0.5f - this.location.screenHeight / 2f

        elements.apply {
            add(titleIcon)
            add(titleLabel)
            add(bottomStage)
            add(centreStage)
        }

        this.updatePositions()
    }

    override fun render(screen: S, batch: SpriteBatch) {
        val oldColor: Float = batch.packedColor
        batch.setColor(0f, 0f, 0f, 0.65f)
        batch.fillRect(location.realX - PADDING_RATIO.first * camera.viewportWidth,
                       location.realY - PADDING_RATIO.second * camera.viewportHeight,
                       location.realWidth + PADDING_RATIO.first * camera.viewportWidth * 2,
                       location.realHeight + PADDING_RATIO.second * camera.viewportHeight * 2)
        batch.setColor(1f, 1f, 1f, 1f)
        val height = (8f / RHRE3.WIDTH) * camera.viewportHeight
        batch.fillRect(location.realX + height,
                       centreStage.location.realY - PADDING_RATIO.second * camera.viewportHeight / 4f,
                       location.realWidth - height * 2,
                       height)

        batch.fillRect(location.realX + height,
                       titleLabel.location.realY - PADDING_RATIO.second * camera.viewportHeight / 4f,
                       location.realWidth - height * 2,
                       height)
        batch.setColor(oldColor)
        super.render(screen, batch)
    }
}