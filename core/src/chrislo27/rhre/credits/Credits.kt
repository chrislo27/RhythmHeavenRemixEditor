package chrislo27.rhre.credits

import ionium.util.i18n.Localization


object Credits {

	val list: List<Pair<String, String>> = listOf(
			"title" to "",
			"programming" to "chrislo27",
			"databasing" to "ahemtoday, Huebird of Happiness, GuardedLolz, chrislo27",
			"localization" to """[RAINBOW]Español (Spanish)[]: Killble, quantic, GlitchyPSIX, David Mismo
[RAINBOW]Français (French)[]: Gabgab2222, Pengu12345, Lovestep, Dragoneteur, chrislo27""",
			"sfx" to "F Yeah, Rhythm Heaven! Tumblr, ahemtoday, Haydorf, megaminerzero, Chocolate2890, Whistler_420, TieSoul, Huebird of Happiness, GuardedLolz, TheRhythmKid, Kana, GlitchyPSIX, Mariofan5000",
			"icons" to "ahemtoday, Whistler_420, Killble, TheNewOrchestra, Altonotone, Pengu12345, fartiliumstation, TheRhythmKid, Chowder, GlitchyPSIX",
			"uidesign" to "GlitchyPSIX",
			"misc" to "Pengu12345, ToonLucas22, Strawzzboy64, Chef May",
			"creditsgame" to "ahemtoday, Serena Strawberry, mistuh_salmon. chrislo27",
			"technologies" to """[DARK_GRAY]lib[][#E10000]GDX[] by Badlogic Games, LWJGL
KHttp
Gson
LuaJ
[#FF8900]Kotlin[] by JetBrains
[#B07219]Java[] by Oracle
[#000007F]Lua[]
Rhythm Heaven assets by Nintendo""",
			"you" to ""
												 )
	val sections: Map<String, String> = list.associate { it }

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