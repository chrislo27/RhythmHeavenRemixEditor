package chrislo27.rhre.registry


data class Game(val id: String, val name: String, val soundCues: List<SoundCue>, val patterns: List<Pattern>) {
}