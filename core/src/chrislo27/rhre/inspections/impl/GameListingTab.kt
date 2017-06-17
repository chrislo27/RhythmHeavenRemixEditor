package chrislo27.rhre.inspections.impl

import chrislo27.rhre.Main
import chrislo27.rhre.entity.HasGame
import chrislo27.rhre.inspections.InspectionTab
import chrislo27.rhre.registry.Game
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import ionium.util.i18n.Localization
import java.util.*

abstract class GameListingTab : InspectionTab() {
	override val name: String = "inspections.title.language"

	protected var map: LinkedHashMap<String, List<Game>> = linkedMapOf()

	protected abstract fun populateMap(remix: Remix, list: List<Game>): LinkedHashMap<String, List<Game>>

	override fun initialize(remix: Remix) {
		map = populateMap(remix, remix.entities.filterIsInstance(HasGame::class.java).map(HasGame::game).distinct())
	}

	override fun render(main: Main, batch: SpriteBatch, startX: Float, startY: Float, width: Float, height: Float,
						mouseXPx: Float, mouseYPx: Float) {
		if (map.isEmpty()) {
			main.font.draw(batch, Localization.get("inspections.language.none"), startX + 32, startY + height / 2 + main.font.lineHeight, width - 64, Align.center, true)
		} else {
			val line: Float = main.font.lineHeight * 1.5f
			var x: Float = startX + 64
			var y: Float = startY + height - 8
			map.forEach { element ->
				Main.drawCompressed(main.font, batch, Localization.get(element.key), x, y - main.font.capHeight / 2, 256f, Align.left)

				var iconX: Float = 0f
				element.value.forEachIndexed { i, it ->
					if (x + iconX + 8 > width) {
						iconX = 0f
						y -= line
					}

					batch.draw(it.iconTexture, x + iconX + 256, y - 32, 32f, 32f)
					iconX += 40f
				}

				y -= line
				x = startX + 64
			}
		}
	}

}
