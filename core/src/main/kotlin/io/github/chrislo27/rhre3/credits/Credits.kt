package io.github.chrislo27.rhre3.credits

import io.github.chrislo27.rhre3.RHRE3


object Credits {

    val list: List<Credit> = listOf(
            "title" crediting RHRE3.GITHUB,
            "programming" crediting "chrislo27",
            "localization" crediting
                    """[LIGHT_GRAY]Français (French)[]
                |inkedsplat, Pengu123
                |
                |[LIGHT_GRAY]Español (Spanish)[]
                |GlitchyPSIX, quantic
                |
                |[LIGHT_GRAY](German)[]
                |Zenon
                |
                |[LIGHT_GRAY](Italian)[]
                |Zenon""".trimMargin(),
            "sfx" crediting "",
            "gfx" crediting "GlitchyPSIX",
            "specialThanks" crediting "baguette, GlitchyPSIX, Miracle22, Pengu123, TheRhythmKid, TheGarnet_, " +
                    "(◉.◉)☂, GrueKun, nerd, ChorusSquid, ArsenArsen, Clone5184, danthonywalker, jos",
            "rhre2" crediting
                    "(◉.◉)☂, ahemtoday, Altonotone, ArendAlphaEagle, Armodillomatt12, baguette, Chef May, " +
                            "Chocolate2890, ChorusSquid, Chowder, David Mismo, Dragoneteur, Eggman199, " +
                            "fartiliumstation, Gabgab2222, GlitchyPSIX, GuardedLolz, Haydorf, " +
                            "Huebird of Happiness, iRonnoc5, Kana, Killble, Lovestep, Mariofan5000, megaminerzero, " +
                            "Miracle22, mistuh_salmon, Pengu12345, quantic, F Yeah, Rhythm Heaven! Tumblr, " +
                            "Serena Strawberry, Strawzzboy64, The Golden Station, TheGarnet_, TheNewOrchestra, " +
                            "TheRhythmKid, TieSoul, ToonLucas22, Whistler_420" +
                            "\n(original) /r/rhythmheaven RH Discord Server",
            "resources" crediting
                    "Rhythm Heaven assets by Nintendo\n" +
                            "[#FF8900]Kotlin[] by JetBrains\n" +
                            "[DARK_GRAY]lib[][#E10000]GDX[] by Badlogic Games\n" +
                            "LWJGL\n" +
                            "KHttp\n" +
                            "Jackson\n" +
                            "JGit\n" +
                            "Apache Commons IO\n" +
                            "SLF4J",
            "you" crediting ""
                                   )

    private infix fun String.crediting(persons: String): Credit =
            Credit(this, persons)

    class Credit(val type: String, val persons: String) {

        val localization: String by lazy {
            "credits.title.$type"
        }

    }

}