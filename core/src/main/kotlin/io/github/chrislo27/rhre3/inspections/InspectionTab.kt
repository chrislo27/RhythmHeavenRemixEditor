package io.github.chrislo27.rhre3.inspections

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.track.Remix


abstract class InspectionTab(val remix: Remix, val nameKey: String) {

    abstract fun render(main: RHRE3Application, batch: SpriteBatch, width: Float, height: Float)

}