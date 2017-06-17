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

class InspTabLanguage : InspectionTab() {
	override val name: String = "inspections.title.language"

	private var list: List<GamesUsed> = listOf()

	override fun initialize(remix: Remix) {
		list = remix.entities
				.filter { it is HasGame }
				.groupBy { it ->
					it as HasGame
					val def: LanguageDef = (if (it.game.id.length < 3)
						LanguageDefinitions["none"]
					else
						LanguageDefinitions.getStrict(it.game.id.substring(it.game.id.length - 2)))
							?: LanguageDefinitions["none"]!!

					return@groupBy def
				}
				.filter { it.key != LanguageDefinitions["none"] }
				.map {
					GamesUsed(it.key, it.value.map { (it as HasGame).game }.distinct())
				}
	}

	override fun render(main: Main, batch: SpriteBatch, startX: Float, startY: Float, width: Float, height: Float,
						mouseXPx: Float, mouseYPx: Float) {
		if (list.isEmpty()) {
			main.font.draw(batch, Localization.get("inspections.language.none"), startX + 32, startY + height / 2 + main.font.lineHeight, width - 64, Align.center, true)
		} else {
			list.forEachIndexed { i, element ->
				val x = startX + 64
				val y = startY + height - 8 - i * main.font.lineHeight * 1.5f
				Main.drawCompressed(main.font, batch, Localization.get(element.languageDef.name), x, y - main.font.capHeight / 2, 256f, Align.left)

				element.list.forEachIndexed { i, it ->
					batch.draw(it.iconTexture, x + 256 + i * 40, y - 32, 32f, 32f)
				}
			}
		}
	}

	internal data class GamesUsed(val languageDef: LanguageDef, val list: List<Game>)

}

object LanguageDefinitions {

	val list: Map<String, LanguageDef> by lazy {
		listOf(
				LanguageDef("none"),
				LanguageDef("en"),
				LanguageDef("ja"),
				LanguageDef("fr"),
				LanguageDef("de"),
				LanguageDef("it"),
				LanguageDef("ko"),
				LanguageDef("es")
			  ).associateBy { it.code }
	}

	operator fun get(str: String): LanguageDef? {
		return list[str]
	}

	fun getStrict(str: String): LanguageDef? {
		if (str[0].isUpperCase())
			return get(str.toLowerCase(Locale.ROOT))
		return null
	}

}

data class LanguageDef(val code: String, val name: String) {

	constructor(code: String) : this(code, "language.$code")

}
