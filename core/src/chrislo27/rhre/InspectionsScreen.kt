package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ionium.registry.ScreenRegistry

class InspectionsScreen(m: Main) : NewUIScreen(m) {
	override var icon: String = "ui_biginspections"
	override var title: String = "inspections.title"
	override var bottomInstructions: String = "inspections.instructions"

	override fun render(delta: Float) {
		super.render(delta)
	}

	override fun renderUpdate() {
		super.renderUpdate()
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		}
	}
}
