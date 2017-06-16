package chrislo27.rhre

import chrislo27.rhre.inspections.InspectionTab
import chrislo27.rhre.inspections.impl.InspTabLanguage
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import ionium.registry.ScreenRegistry
import ionium.util.i18n.Localization
import ionium.util.render.StencilMaskUtil

class InspectionsScreen(m: Main) : NewUIScreen(m) {
	override var icon: String = "ui_biginspections"
	override var title: String = "inspections.title"
	override var bottomInstructions: String = "inspections.instructions"

	private val remix: Remix
		get() = ScreenRegistry.get("editor", EditorScreen::class.java).editor.remix

	private var currentTab: Int = 0
		set(value) {
			field = value
			targetTab = MathUtils.clamp(targetTab, value - 1f, value + 1f)
		}
	private var targetTab: Float = currentTab.toFloat()
	private var dots: String = ""
	private val tabs: List<InspectionTab> =
			listOf(
					InspTabLanguage()
				  )
	private val vector: Vector3 = Vector3()

	override fun render(delta: Float) {
		super.render(delta)

		targetTab = MathUtils.lerp(targetTab, currentTab.toFloat(), (6.4f * Gdx.graphics.deltaTime).coerceAtMost(1f))

		main.batch.begin()

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f
		val tab: InspectionTab? = tabs.getOrNull(currentTab)

		// left right indicators
		if (currentTab > 0) {
			main.font.draw(main.batch, "◀ " + Localization.get(tabs.getOrNull(currentTab - 1)?.name), startX + 32,
						   startY + BG_HEIGHT * 0.8f)
		}
		if (currentTab < tabs.size - 1) {
			main.font.draw(main.batch, Localization.get(tabs.getOrNull(currentTab + 1)?.name) + " ▶",
						   startX + BG_WIDTH - 32, startY + BG_HEIGHT * 0.8f, 0f, Align.right,
						   false)
		}
		if (tab != null) {
			main.font.draw(main.batch, Localization.get(tab.name), startX + BG_WIDTH * 0.5f, startY + BG_HEIGHT * 0.8f,
						   0f,
						   Align.center, false)
		}

		// dots
		main.font.draw(main.batch, dots, startX + BG_WIDTH * 0.5f, startY + BG_HEIGHT * 0.8f - main.font.lineHeight,
					   0f, Align.center, false)

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

		// render adjacent tabs
		main.camera.unproject(vector.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
		main.batch.setColor(1f, 1f, 1f, 1f)
		for (i in Math.max(0, currentTab - 1)..Math.min(tabs.size - 1, currentTab + 1)) {
			val relativePosition: Int = i - currentTab
			val xOffset: Float = (currentTab - targetTab + relativePosition) * main.camera.viewportWidth

			tabs.getOrNull(i)?.render(main, main.batch, tabStartX + xOffset, tabStartY, tabWidth, tabHeight, vector.x,
									  vector.y)
		}

		main.batch.flush()
		StencilMaskUtil.resetMask()

		main.batch.end()
	}

	override fun renderUpdate() {
		super.renderUpdate()
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			if (currentTab > 0)
				currentTab--
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			if (currentTab < tabs.size - 1)
				currentTab++
		}
	}

	override fun show() {
		super.show()
		val remix = remix
		tabs.forEach {
			it.initialize(remix)
		}

		updateDots()
	}

	private fun updateDots() {
		val builder = StringBuilder()

		for (i in 0 until tabs.size) {
			builder.append(if (i == currentTab) "▪" else "◦")
		}

		dots = builder.toString()
	}
}
