package io.github.chrislo27.rhre3.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.MathHelper


open class SpinningWheel<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : ImageLabel<S>(palette, parent, stage) {

    var speed = 2f

    override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (image == null) {
            image = TextureRegion(AssetRegistry.get<Texture>("ui_spinning_circle"))
        }

        rotation = MathHelper.getSawtoothWave(Math.abs(speed)) * -360f * Math.signum(speed)
        super.render(screen, batch, shapeRenderer)
    }

}
