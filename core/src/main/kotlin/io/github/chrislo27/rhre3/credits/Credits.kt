package io.github.chrislo27.rhre3.credits

import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.toolboks.i18n.Localization
import java.util.*


object Credits {

    private val sfxCreditsFallback: String = listOf("Lvl100Feraligatr", "GenericArrangements", "Draster", "NP", "Eggman199", "Huebird", "oofie", "Miracle22", "MF5K", "The Golden Station", "GuardedLolz", "GlitchyPSIX", "sp00pster", "Maxanum").sortedBy { it.toLowerCase(Locale.ROOT) }.joinToString(separator = ", ")

    fun generateList(): List<Credit> {
        return listOf(
                "title" crediting RHRE3.GITHUB,
                "programming" crediting "chrislo27\n${Localization["credits.title.programming.contributions", "Kamayana"]}",
                "localization" crediting
                        """[LIGHT_GRAY]Français (French)[]
                |inkedsplat, minenice55, Pengu123
                |
                |[LIGHT_GRAY]Español (Spanish)[]
                |chipdamage, Cosmicfab, (◉.◉)☂, GlitchyPSIX, Killble, meuol, quantic, SJGarnet, Suwa-ko
                |
                |[LIGHT_GRAY]Deutsch (German)[]
                |Zenon""".trimMargin(),
                "sfx" crediting (SFXDatabase.let { if (!it.isDataLoading()) it.data.sfxCredits.sortedBy { it.toLowerCase(Locale.ROOT) }.joinToString(separator = ", ") else null } ?: sfxCreditsFallback),
                "gfx" crediting "GlitchyPSIX, lilbitdun, Steppy, Tickflow",
                "extras" crediting "Malalaika, The Drummer",
                "specialThanks" crediting """Alchemyking, AngryTapper, ArsenArsen, baguette, bin5s5, Chillius, ChorusSquid, Clone5184, danthonywalker, Dracobot, Draster, Dream Top, EBPB2K, flyance, Fringession, garbo, GenericArrangements, (◉.◉)☂, GinoTitan, GlitchyPSIX, GrueKun, inkedsplat, iRonnoc5, jos, Lvl100Feraligatr, Malalaika, Mezian, minenice55, Miracle22, Mixelz, nave, nerd, oofie, Pengu123, PikaMasterJesi, Rabbidking, RobSetback, SJGarnet, sp00pster, SuicuneWiFi, susmobile, TheRhythmKid, Zenon, RHModding and Custom Remix Tourney Discord servers""",
                "resources" crediting
                        """Rhythm Heaven assets by Nintendo
[#FF8900]Kotlin[]
[DARK_GRAY]lib[][#E10000]GDX[]
LWJGL
Toolboks
Beads
Async HTTP Client
Jackson
JGit
Apache Commons IO
SLF4J
OSHI
jump3r
musique (forked)
Segment
java-discord-rpc
rhmodding/bread
JCommander
SoundStretch and SoundTouch
zip4j
Jam3/glsl-fast-gaussian-blur""",
                "donators" crediting "",
                "you" crediting ""
                     )
    }

    private infix fun String.crediting(persons: String): Credit =
            Credit(this, persons)

    data class Credit(val type: String, val persons: String) {

        private val localization: String by lazy {
            "credits.title.$type"
        }
        private val isTitle by lazy { type == "title" }

        val text: String = if (isTitle) RHRE3.TITLE_3 else Localization[localization]

    }

}