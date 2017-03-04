package chrislo27.rhre.registry

import chrislo27.rhre.Main
import ionium.util.i18n.Localization
import ionium.util.i18n.NamedLocale


data class Game(val id: String, val names: List<String>, val soundCues: List<SoundCue>,
				val patterns: List<Pattern>, val series: Series, val icon: String?,
				val iconIsRawPath: Boolean = false) {

	companion object {
		val LOCALIZATION_KEY = "registry.gameName."
	}

	init {
		names.forEach {
			if (it.indexOf('|') != -1){
				val lang = it.substringBefore('|')
				val namedLocale: NamedLocale? = Main.languages.find { it.locale.toString() == lang }

				if (namedLocale != null) {
					Localization.instance().addCustom(LOCALIZATION_KEY + id, it.substringAfter('|'), namedLocale)
				}
			} else {
				Localization.instance().addCustom(LOCALIZATION_KEY + id, it)
			}
		}
	}

	fun getName(): String = Localization.get(LOCALIZATION_KEY + id)

	fun isCustom() = series == Series.CUSTOM

	fun getPattern(id: String): Pattern? {
		return patterns.find { it.id == "${this.id}_$id" }
	}

	fun getCue(id: String): SoundCue? {
		return soundCues.find { it.id == "${this.id}/$id" }
	}
}

enum class Series(val shorthand: String, val properName: String) {

	UNKNOWN("Misc.", "Misc."), TENGOKU("TG", "Rhythm Tengoku"), DS("DS", "Rhythm Heaven"),
	FEVER("FV", "Rhythm Heaven Fever"), MEGAMIX("MM", "Rhythm Heaven Megamix"), SIDE("Side", "Side games"),
	CUSTOM("Custom", "Custom sounds");

}