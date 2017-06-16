package chrislo27.rhre.inspections.impl

import chrislo27.rhre.Main
import chrislo27.rhre.inspections.InspectionTab
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class InspTabLanguage : InspectionTab() {
	override val name: String = "inspections.title.language"

	override fun initialize(remix: Remix) {
	}

	override fun render(main: Main, batch: SpriteBatch, startX: Float, startY: Float, width: Float, height: Float,
						mouseXPx: Float, mouseYPx: Float) {
		ionium.templates.Main.fillRect(batch, startX, startY, width, height)
	}

}
