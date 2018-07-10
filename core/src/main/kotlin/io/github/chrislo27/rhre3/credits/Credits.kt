package io.github.chrislo27.rhre3.credits

import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.i18n.Localization


object Credits {

    val list: List<Credit> = listOf(
            "title" crediting RHRE3.GITHUB,
            "programming" crediting "chrislo27",
            "localization" crediting
                    """[LIGHT_GRAY]Français (French)[]
                |Pengu123, minenice55, inkedsplat
                |
                |[LIGHT_GRAY]Español (Spanish)[]
                |GlitchyPSIX, quantic, SJGarnet, (◉.◉)☂
                |
                |[LIGHT_GRAY]Deutsch (German)[]
                |Zenon""".trimMargin(),
            "sfx" crediting "baguette, Huebird, Miracle22, Eggman199, Mariofan5000, The Golden Station, GuardedLolz, GlitchyPSIX, sp00pster, Lvl100Feraligatr",
            "gfx" crediting "GlitchyPSIX, lilbitdun, Steppy",
            "specialThanks" crediting "baguette, GlitchyPSIX, Miracle22, Pengu123, TheRhythmKid, TheGarnet_, " +
                    "(◉.◉)☂, GrueKun, nerd, ChorusSquid, ArsenArsen, Clone5184, danthonywalker, jos, susmobile, " +
                    "Lvl100Feraligatr, SuicuneWiFi, Dracobot, AngryTapper, Zenon, inkedsplat, RobSetback, Mixelz, " +
                    "iRonnoc5, sp00pster, Alchemyking, SportaDerp9000, RHModding Discord server",
            "rhre2" crediting
                    "(◉.◉)☂, ahemtoday, Altonotone, ArendAlphaEagle, Armodillomatt12, baguette, Chef May, " +
                    "ChocolateJake, ChorusSquid, Chowder, David Mismo, Dragoneteur, Eggman199, " +
                    "fartiliumstation, Gabgab2222, GlitchyPSIX, GuardedLolz, Haydorf, " +
                    "Huebird of Happiness, iRonnoc5, Kana, Killble, Locorito, Lovestep, Mariofan5000, megaminerzero, " +
                    "Miracle22, mistuh_salmon, Pengu123, quantic, F Yeah, Rhythm Heaven! Tumblr, " +
                    "Serena Strawberry, Strawzzboy64, The Golden Station, TheGarnet_, TheNewOrchestra, " +
                    "TheRhythmKid, TieSoul, ToonLucas22, Whistler_420" +
                    "\nRhythm Heaven and /r/RhythmHeaven Discord servers",
            "resources" crediting
                    "Rhythm Heaven assets by Nintendo\n" +
                    "[#FF8900]Kotlin[] by JetBrains\n" +
                    "[DARK_GRAY]lib[][#E10000]GDX[] by Badlogic Games\n" +
                    "LWJGL\n" +
                    "Beads\n" +
                    "Async HTTP Client\n" +
                    "Jackson\n" +
                    "JGit\n" +
                    "Apache Commons IO\n" +
                    "SLF4J\n" +
                    "OSHI\n" +
                    "jump3r\n" +
                    "musique (forked)\n" +
                    "Segment\n" +
                    "java-discord-rpc\n" +
                    "bccad-editor",
            "donators" crediting "",
            "you" crediting ""
                                   )

    private infix fun String.crediting(persons: String): Credit =
            Credit(this, persons)

    data class Credit(val type: String, val persons: String) {

        private val localization: String by lazy {
            "credits.title.$type"
        }
        private val isTitle by lazy { type == "title" }

        val text: String
            get() = if (isTitle) RHRE3.TITLE else Localization[localization]

    }

}