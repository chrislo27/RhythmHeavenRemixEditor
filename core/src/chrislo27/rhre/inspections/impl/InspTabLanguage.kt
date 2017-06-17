package chrislo27.rhre.inspections.impl

import chrislo27.rhre.registry.Game
import chrislo27.rhre.track.Remix
import java.util.*

class InspTabLanguage : GameListingTab() {
	override val name: String = "inspections.language.title"
	override val noneText: String = "inspections.language.none"

	override fun populateMap(remix: Remix, list: List<Game>): LinkedHashMap<String, List<Game>> {
		return list
				.groupBy { it ->
					val def: LanguageDef = (if (it.id.length < 3)
						LanguageDefinitions["none"]
					else
						LanguageDefinitions.getStrict(it.id.substring(it.id.length - 2)))
							?: LanguageDefinitions["none"]!!

					return@groupBy def
				}
				.filter { it.key != LanguageDefinitions["none"] }
				.mapKeysTo(linkedMapOf()) { entry ->
					entry.key.name
				}
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

}
