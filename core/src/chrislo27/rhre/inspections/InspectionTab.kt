package chrislo27.rhre.inspections

import chrislo27.rhre.Main
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.graphics.g2d.SpriteBatch


abstract class InspectionTab {

    abstract val name: String

    abstract fun initialize(remix: Remix)

    abstract fun render(main: Main, batch: SpriteBatch, startX: Float, startY: Float, width: Float, height: Float,
                        mouseXPx: Float, mouseYPx: Float)

}