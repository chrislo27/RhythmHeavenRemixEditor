package chrislo27.rhre.registry

import chrislo27.rhre.json.GameObject
import com.badlogic.gdx.files.FileHandle


object GeneratorHelpers {

	val map: Map<String, GeneratorHelper> = mapOf("manzaiBirds" to ManzaiBirdsGeneratorHelper())

}

abstract class GeneratorHelper {

	abstract fun process(gameFh: FileHandle, gameObject: GameObject, patterns: MutableList<Pattern>,
						 soundCues: MutableList<SoundCue>)

}

class ManzaiBirdsGeneratorHelper : GeneratorHelper() {

	val paths: String = """manzaiBirds/aichini_aichinna
manzaiBirds/amette_amena
manzaiBirds/chainani_nicchaina
manzaiBirds/denwari_denwa
manzaiBirds/domakiyo_otonokoi
manzaiBirds/futonga_futtonda
manzaiBirds/hiromega_hiromeida
manzaiBirds/houchaga_houchou
manzaiBirds/igakari_katta
manzaiBirds/ikugawa_ikura
manzaiBirds/kaero_burikaeru
manzaiBirds/karega_kare
manzaiBirds/kusaga_kusai
manzaiBirds/megaminiwa_megani
manzaiBirds/mikanga_mikannai
manzaiBirds/nekoga_nekoronda
manzaiBirds/okanewa_okkane
manzaiBirds/okurezu_kitte_okure
manzaiBirds/omochino_kimochi
manzaiBirds/omoino_hoka_omoi
manzaiBirds/puringa_tappurin
manzaiBirds/rakugawa_rakugana
manzaiBirds/roukada_katarouka
manzaiBirds/saiyo_minasai
manzaiBirds/sakana_kana_masakana
manzaiBirds/saruga_saru
manzaiBirds/shaiinni_nanari_nashain
manzaiBirds/suikawa_yasuika
manzaiBirds/taiga_tabetaina
manzaiBirds/tainini_kittai
manzaiBirds/taiyo_gami_taiyou
manzaiBirds/toireni_ittoire
manzaiBirds/torinikuga_torininkui
manzaiBirds/umette_umena"""

	override fun process(gameFh: FileHandle, gameObject: GameObject, patterns: MutableList<Pattern>,
						 soundCues: MutableList<SoundCue>) {
		val addedCues: MutableList<SoundCue> = mutableListOf()
		val lines = paths.lines()

		lines.forEach {
			addedCues.add(
					SoundCue(it, gameObject.gameID!!, "ogg", it.replace("manzaiBirds/", "").replace("_", " "), mutableListOf(), 2.5f, false,
							 0f, true, null, 95f, false, null))
		}

		addedCues.forEach {

			var patternCues = mutableListOf<Pattern.PatternCue>()

			patternCues.add(Pattern.PatternCue(it.id, gameObject.gameID!!, 0f, 0, 2.5f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/haihai1", gameObject.gameID!!, 2.5f, 0, 0f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/haihai1", gameObject.gameID!!, 3.0f, 0, 0f, 0))

			patterns.add(Pattern("manzaiBirds_" + it.id, gameObject.gameID!!, it.name, false, patternCues, false, mutableListOf()))

			// -----------------------------------------------------------

			patternCues = mutableListOf<Pattern.PatternCue>()

			patternCues.add(Pattern.PatternCue(it.id, gameObject.gameID!!, 0f, 0, 1.5f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/boing", gameObject.gameID!!, 1.5f, 0, 0f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/donaiyanen", gameObject.gameID!!, 2.5f, 0, 0f, 0))

			patterns.add(Pattern("manzaiBirds_" + it.id + "_boing", gameObject.gameID!!, it.name + " BOING!", false, patternCues, false, mutableListOf()))

		}

		soundCues.addAll(addedCues)
	}

}
