package chrislo27.rhre.registry


data class Game(val id: String, val name: String, val soundCues: List<SoundCue>,
				val patterns: List<Pattern>, val series: Series, val icon: String?) {

	fun getPattern(id: String): Pattern? {
		return patterns.find { it.id == "${this.id}_$id" }
	}

	fun getCue(id: String): SoundCue? {
		return soundCues.find { it.id == "${this.id}/$id" }
	}
}

enum class Series(val shorthand: String) {

	UNKNOWN("Misc."), TENGOKU("TG"), DS("DS"), FEVER("FV"), MEGAMIX("MM");

}