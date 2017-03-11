package chrislo27.rhre.credits

import ionium.util.i18n.Localization


object Credits {

	val sections: Map<String, String> = mapOf(
			"title" to "",
			"programming" to "chrislo27",
			"databasing" to "ahemtoday, Huebird of Happiness, GuardedLolz, chrislo27",
			"localization" to """Español (Spanish): Killble, quantic, GlitchyPSIX, David Mismo
Français (French): Gabgab2222, Pengu12345, Lovestep, Dragoneteur
Italiano (Italian): Huebird of Happiness""",
			"sfx" to "F Yeah, Rhythm Heaven! Tumblr, ahemtoday, Haydorf, megaminerzero, Chocolate2890, Whistler_420, TieSoul, Huebird of Happiness, GuardedLolz, TheRhythmKid, Mariofan5000",
			"icons" to "ahemtoday, Whistler_420, Killble, TheNewOrchestra, Altonotone, Pengu12345, fartiliumstation, TheRhythmKid, Chowder",
			"uidesign" to "GlitchyPSIX",
			"misc" to "Pengu12345, ToonLucas22, Strawzzboy64",
			"creditsgame" to "ahemtoday, Serena Strawberry",
			"technologies" to """[DARK_GRAY]Lib[][#E10000]GDX[] by Badlogic Games, LWJGL
[#B07219]Java[] by Oracle,
[#FF8900]Kotlin[] by JetBrains
Rhythm Heaven assets by Nintendo""",
			"you" to ""
													 )

	val array: List<Pair<String, String>> = sections.toList()

	var concatSections: String = ""
		private set

	fun createConcatSections(): String {
		concatSections = sections.map {
			"[LIGHT_GRAY]" + Localization.get("info.credits." + it.key).toUpperCase(
					Localization.instance().currentBundle.locale.locale) + "[]\n" + it.value
		}.joinToString(separator = "\n\n")

		return concatSections
	}

}