package chrislo27.rhre.registry


data class Game(val id: String, val name: String, val soundCues: List<SoundCue>, val patterns: List<Pattern>, val series: Series) {
}

enum class Series(val shorthand: String) {

	UNKNOWN("Misc."), TENGOKU("TG"), DS("DS"), FEVER("FV"), MEGAMIX("MM");

}