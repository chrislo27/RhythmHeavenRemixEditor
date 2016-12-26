package chrislo27.rhre.registry


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

enum class Series(val shorthand: String, val properName: String) {

	UNKNOWN("Misc.", "Misc."), TENGOKU("TG", "Rhythm Tengoku"), DS("DS", "Rhythm Heaven"),
	FEVER("FV", "Rhythm Heaven Fever"), MEGAMIX("MM", "Rhythm Heaven Megamix"), SIDE("Side", "Side games"),
	CUSTOM("Custom", "Custom sounds");

}