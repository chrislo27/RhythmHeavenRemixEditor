package chrislo27.rhre.registry

import ionium.util.i18n.Localization


data class Game(val id: String, val name: String, val soundCues: List<SoundCue>,
				val patterns: List<Pattern>, val series: Series, val icon: String?,
				val iconIsRawPath: Boolean = false) {

	fun isCustom() = series == Series.CUSTOM

	fun getPattern(id: String): Pattern? {
		return patterns.find { it.id == "${this.id}_$id" }
	}

	fun getCue(id: String): SoundCue? {
		return soundCues.find { it.id == "${this.id}/$id" }
	}
}

enum class Series(val i10nKey: String) {

	UNKNOWN("other"), TENGOKU("tengoku"), DS("ds"),
	FEVER("fever"), MEGAMIX("megamix"), SIDE("side"),
	CUSTOM("custom");

	fun getLocalizedName(): String = Localization.get("series." + i10nKey)

}