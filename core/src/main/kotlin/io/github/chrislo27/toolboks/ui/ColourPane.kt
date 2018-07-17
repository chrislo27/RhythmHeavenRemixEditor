package io.github.chrislo27.toolboks.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


open class ColourPane<S : ToolboksScreen<*, *>>(parent: UIElement<S>, parameterStage: Stage<S>)
    : UIElement<S>(parent, parameterStage), Backgrounded {

    override var background: Boolean = true
    open val colour: Color = Color(1f, 1f, 1f, 1f)

    override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (background) {
            val oldColour = batch.packedColor

            batch.setColor(colour.toFloatBits())
            batch.fillRect(location.realX, location.realY, location.realWidth, location.realHeight)

            batch.setColor(oldColour)
        }
    }

}