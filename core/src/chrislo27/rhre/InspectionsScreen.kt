package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import ionium.registry.ScreenRegistry
import ionium.util.render.StencilMaskUtil

class InspectionsScreen(m: Main) : NewUIScreen(m) {
	override var icon: String = "ui_biginspections"
	override var title: String = "inspections.title"
	override var bottomInstructions: String = "inspections.instructions"

	private var currentTab: Int = 0

	override fun render(delta: Float) {
		super.render(delta)

		main.batch.begin()

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

		// left right indicators TODO
		main.font.draw(main.batch, "◀ Languages", startX + 32, startY + BG_HEIGHT * 0.8f)
		main.font.draw(main.batch, "Other Stuff ▶", startX + BG_WIDTH - 32, startY + BG_HEIGHT * 0.8f, 0f, Align.right, false)
		main.font.draw(main.batch, "Current Stuff", startX + BG_WIDTH * 0.5f, startY + BG_HEIGHT * 0.8f, 0f, Align.center, false)

		// dots TODO
		main.font.draw(main.batch, "◦▪◦", startX + BG_WIDTH * 0.5f, startY + BG_HEIGHT * 0.8f - main.font.lineHeight, 0f, Align.center, false)

		main.batch.end()

		StencilMaskUtil.prepareMask()
		main.shapes.projectionMatrix = main.camera.combined
		main.shapes.begin(ShapeRenderer.ShapeType.Filled)

		val tabStartX = startX
		val tabStartY = startY + BG_HEIGHT * 0.15f
		val tabWidth = BG_WIDTH
		val tabHeight = BG_HEIGHT * 0.65f - main.font.lineHeight * 2

		main.shapes.rect(tabStartX, tabStartY, tabWidth, tabHeight)
		main.shapes.end()

		main.batch.begin()
		StencilMaskUtil.useMask()

		// render adjacent tabs TODO
		main.batch.setColor(1f, 1f, 1f, 1f)
		ionium.templates.Main.fillRect(main.batch, 0f, 0f, 2000f, 1000f)

		main.batch.flush()
		StencilMaskUtil.resetMask()

		main.batch.end()
	}

	override fun renderUpdate() {
		super.renderUpdate()
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		}
	}
}
