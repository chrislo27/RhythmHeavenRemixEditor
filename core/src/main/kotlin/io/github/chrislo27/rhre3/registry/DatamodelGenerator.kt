package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.json.DataObject
import java.util.*


abstract class DatamodelGenerator {

    companion object {

        val generators: Map<String, DatamodelGenerator> =
                mapOf(
                        "flipperFlopEn" to FlipperFlopGenerator("flipperFlopEn"),
                        "bossaNovaEn" to BossaNovaGenerator("bossaNovaEn"),
                        "manzaiBirds" to ManzaiBirdsGenerator()
                     )

    }

    abstract fun process(folder: FileHandle, dataObject: DataObject, game: Game)

}

class FlipperFlopGenerator(val id: String) : DatamodelGenerator() {

    override fun process(folder: FileHandle, dataObject: DataObject, game: Game) {
        game.objects as MutableList

        listOf(
                "$id/ding" to "ding",
                "$id/flip" to "flip",
                "$id/flipB1" to "flip - back - 1",
                "$id/flipB2" to "flip - back - 2",
                "$id/roll" to "roll",
                "$id/uh1" to "uh - 1",
                "$id/uh2" to "uh - 2",
                "$id/uh3" to "uh - 3"
              )
                .mapTo(game.objects) {
                    Cue(game, it.first, mutableListOf(), it.second,
                        0.5f, false, false,
                        GameRegistry.SFX_FOLDER.child(
                                it.first + ".ogg"),
                        null, null, mutableListOf(), 0f, false)
                }

        listOf(
                "$id/appreciation/good" to "appreciation - good",
                "$id/appreciation/goodjob" to "appreciation - good job",
                "$id/appreciation/nice" to "appreciation - nice",
                "$id/appreciation/welldone" to "appreciation - well done!",
                "$id/appreciation/yes" to "appreciation - yes"
              )
                .mapTo(game.objects) {
                    Cue(game, it.first, mutableListOf(), it.second,
                        1f, false, false,
                        GameRegistry.SFX_FOLDER.child(
                                it.first + ".ogg"),
                        null, null, mutableListOf(), 0f, false)
                }

        listOf(
                "$id/appreciation/thatsit1" to "that's it - that's",
                "$id/appreciation/thatsit2" to "that's it - it!"
              )
                .mapTo(game.objects) {
                    Cue(game, it.first, mutableListOf(), it.second,
                        0.5f, false, false,
                        GameRegistry.SFX_FOLDER.child(
                                it.first + ".ogg"),
                        null, null, mutableListOf(), 0f, false)
                }

        game.objects +=
                KeepTheBeat(game, "${id}_flippingA", mutableListOf(),
                            "flipping", 2f,
                            mutableListOf(
                                    CuePointer(
                                            "$id/flip", 0f),
                                    CuePointer(
                                            "$id/flip", 1f)
                                         ))
        game.objects +=
                KeepTheBeat(game, "${id}_flippingB", mutableListOf(),
                            "flipping - back", 2f,
                            mutableListOf(
                                    CuePointer(
                                            "$id/flipB1", 0f),
                                    CuePointer(
                                            "$id/flipB2", 1f)
                                         ))
        game.objects +=
                Pattern(game, "${id}_thatsit", mutableListOf(),
                        "that's it!",
                        mutableListOf(
                                CuePointer(
                                        "$id/appreciation/thatsit1",
                                        0f),
                                CuePointer(
                                        "$id/appreciation/thatsit2",
                                        0.5f)
                                     ), false)

        game.objects +=
                Pattern(game, "${id}_triple", mutableListOf(),
                        "triple",
                        mutableListOf(
                                CuePointer(
                                        "$id/ding", 0f),
                                CuePointer(
                                        "$id/ding", 0.5f),
                                CuePointer(
                                        "$id/ding", 1f),
                                CuePointer(
                                        "$id/flip", 2f),
                                CuePointer(
                                        "$id/flip", 2.5f),
                                CuePointer(
                                        "$id/flip", 3f)
                                     ), false)
        game.objects +=
                Pattern(game, "${id}_tripleBack", mutableListOf(),
                        "triple - back",
                        mutableListOf(
                                CuePointer(
                                        "$id/ding", 0f),
                                CuePointer(
                                        "$id/ding", 0.5f),
                                CuePointer(
                                        "$id/ding", 1f),
                                CuePointer(
                                        "$id/flipB1", 2f),
                                CuePointer(
                                        "$id/flipB2", 2.5f),
                                CuePointer(
                                        "$id/flipB1", 3f)
                                     ), false)

        run attention@ {
            (1..8).mapTo(game.objects) {
                Cue(game, "$id/attention/attention$it",
                    mutableListOf(),
                    "attention - part $it",
                    if (it in 2..3 || it == 7) 0.5f else if (it == 4 || it == 8) 1f else 0.25f,
                    false, false,
                    GameRegistry.SFX_FOLDER.child("$it.ogg"),
                    null, null, mutableListOf(), 0f, false)
            }

            game.objects +=
                    Pattern(game, "${id}_attention", mutableListOf(),
                            "attention... company!",
                            mutableListOf(
                                    CuePointer(
                                            "$id/attention/1", 0.0f),
                                    CuePointer(
                                            "$id/attention/2", 0.25f),
                                    CuePointer(
                                            "$id/attention/3", 0.75f),
                                    CuePointer(
                                            "$id/attention/4", 1.25f),
                                    CuePointer(
                                            "$id/attention/5", 2.25f),
                                    CuePointer(
                                            "$id/attention/6", 2.5f),
                                    CuePointer(
                                            "$id/attention/7", 2.75f),
                                    CuePointer(
                                            "$id/attention/8", 3.25f)
                                         ), false)
        }

        run count@ {
            val flipperRollCounts: String = """$id/count/flipperRollCount1
$id/count/flipperRollCount2
$id/count/flipperRollCount3
$id/count/flipperRollCount4
$id/count/flipperRollCount5
$id/count/flipperRollCount6
$id/count/flipperRollCount7
$id/count/flipperRollCount8
$id/count/flipperRollCount9
$id/count/flipperRollCount10
$id/count/flipperRollCountNow
$id/count/flipperRollCountA
$id/count/flipperRollCountB
$id/count/flipperRollCountC
$id/count/flipperRollCountS"""

            flipperRollCounts.lines().forEach {
                game.objects +=
                        Cue(game, it, mutableListOf(),
                            "count ${it.replace(
                                    "$id/count/flipperRollCount",
                                    "")}",
                            0.25f, false, false,
                            GameRegistry.SFX_FOLDER.child("$id.ogg"),
                            null, null, mutableListOf(), 0f, false)
            }
            """$id/count/flopCount1
$id/count/flopCount1B
$id/count/flopCount2
$id/count/flopCount2B
$id/count/flopCount3
$id/count/flopCount3B
$id/count/flopCount4
$id/count/flopCount4B""".lines().forEach {
                game.objects +=
                        Cue(game, it, mutableListOf(),
                            "flopping count ${it.replace(
                                    "$id/count/flopCount", "")}",
                            1f, false, false,
                            GameRegistry.SFX_FOLDER.child("$id.ogg"),
                            null, null, mutableListOf(), 0f, false)
            }

            flipperRollCounts.lines().takeWhile { !it.endsWith("A") }.forEach {
                val type: String = it.replace("$id/count/flipperRollCount", "").toLowerCase(Locale.ROOT)
                val count: Int = if (type == "now") 1 else Integer.parseInt(type)
                val pluralize: Boolean = count != 1
                val patternCues = mutableListOf<CuePointer>()

                patternCues +=
                        CuePointer(it, 0f)

                patternCues +=
                        CuePointer("$id/count/flipperRollCountA",
                                   0.5f)
                patternCues +=
                        CuePointer("$id/count/flipperRollCountB",
                                   0.75f)
                patternCues +=
                        CuePointer("$id/count/flipperRollCountC", 1f)

                if (pluralize) {
                    patternCues +=
                            CuePointer("$id/count/flipperRollCountS",
                                       1.25f)
                }

                for (i in 1..count) {
                    val num: String = if (i % 4 == 0) "4" else "${i % 4}"
                    patternCues +=
                            CuePointer(
                                    "$id/count/flopCount$num${if (i > 4) "B" else ""}", 1f + i)
                }

                game.objects +=
                        Pattern(game, "${id}_count_$type",
                                mutableListOf(),
                                "$type flipper roll${if (pluralize) "s" else ""}!",
                                patternCues, false)
            }
        }
    }

}

class BossaNovaGenerator(val id: String) : DatamodelGenerator() {

    private val allCues: String = """$id/ball
$id/ball_echo
$id/ball_hard
$id/ball_low
$id/ball_pop
$id/click
$id/cloud1
$id/cloud2
$id/cube
$id/cube_echo
$id/cube_heavy
$id/female_bom1
$id/female_bom2
$id/female_bom3
$id/female_bom4
$id/female_bom5
$id/female_bom6
$id/female_bom7
$id/female_bom8
$id/female_bom9
$id/female_bom10
$id/female_bom_laugh
$id/female_cough1
$id/female_giggle1
$id/female_giggle2
$id/female_giggle3
$id/female_mmm
$id/female_pin1
$id/female_pin2
$id/female_pin3
$id/female_ss1
$id/female_ss2
$id/female_ss3
$id/female_yelp
$id/hack
$id/male_ahh1
$id/male_cough1
$id/male_d1
$id/male_d2
$id/male_d3
$id/male_don1
$id/male_don2
$id/male_don3
$id/male_don4
$id/male_don5
$id/male_don6
$id/male_don7
$id/male_don8
$id/male_don9
$id/male_don10
$id/male_don11
$id/male_don12
$id/male_grunt1
$id/male_mmm
$id/male_ooh1
$id/male_ss1
$id/male_ss2
$id/male_ss3
$id/male_t
$id/male_tt
$id/male_turn1
$id/male_turn2
$id/male_turn3
$id/male_turn4
$id/male_turn5
$id/male_turn6
$id/sniff
$id/ss
$id/thwack"""

    override fun process(folder: FileHandle, dataObject: DataObject, game: Game) {
        val lines = allCues.lines()

        game.objects as MutableList

        lines.forEach {
            game.objects +=
                    Cue(game, it, mutableListOf(),
                        it.replace("$id/", "").replace("_", " "),
                        if (it.endsWith("cloud1")) {
                            1f
                        } else if (it.endsWith("cloud2")) {
                            2f
                        } else if (it.contains("bom") || it.contains(
                                "don")) {
                            1.5f
                        } else if (it.contains("turn") || it.contains(
                                "spin")) {
                            1f
                        } else {
                            0.5f
                        },
                        false, false,
                        GameRegistry.SFX_FOLDER.child("$it.ogg"),
                        null, null, mutableListOf(), 0f, false)
        }

        game.objects +=
                Pattern(game, "${id}_startPattern", mutableListOf(),
                        "starting pattern",
                        mutableListOf(
                                CuePointer(
                                        "$id/ball", 0f),
                                CuePointer(
                                        "$id/cube", 1f),
                                CuePointer(
                                        "$id/ball", 1.5f),
                                CuePointer(
                                        "$id/ball", 2f),
                                CuePointer(
                                        "$id/cube", 2.5f)
                                     ), false)
        game.objects +=
                Pattern(game, "${id}_pattern", mutableListOf(),
                        "pattern",
                        mutableListOf(
                                CuePointer(
                                        "$id/ball", 0f),
                                CuePointer(
                                        "$id/ball", 0.5f),
                                CuePointer(
                                        "$id/cube", 1.5f),
                                CuePointer(
                                        "$id/ball", 2f),
                                CuePointer(
                                        "$id/ball", 2.5f),
                                CuePointer(
                                        "$id/cube", 3f)
                                     ), false)
        game.objects +=
                Pattern(game, "${id}_cloudSpin", mutableListOf(),
                        "cloud spin",
                        mutableListOf(
                                CuePointer(
                                        "$id/cloud1", 0f),
                                CuePointer(
                                        "$id/cloud2", 1f)
                                     ), false)
    }

}

class ManzaiBirdsGenerator : DatamodelGenerator() {

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

    override fun process(folder: FileHandle, dataObject: DataObject, game: Game) {
        val lines = paths.lines()
        val addedCues: MutableList<Cue> = mutableListOf()

        game.objects as MutableList

        lines.mapTo(addedCues) {
            Cue(game, it, mutableListOf(),
                it.replace("manzaiBirds/", "").replace("_", " "),
                2.5f, false, false,
                GameRegistry.SFX_FOLDER.child("$it.ogg"),
                null, null, mutableListOf(),
                95f, false)
        }

        game.objects.addAll(addedCues)

        val patterns = addedCues.map {
            Pattern(game, "manzaiBirds_${it.id}",
                    mutableListOf(), it.name,
                    mutableListOf(
                            CuePointer(
                                    it.id, 0f, 2.5f),
                            CuePointer(
                                    "manzaiBirds/haihai1",
                                    2.5f),
                            CuePointer(
                                    "manzaiBirds/haihai1",
                                    3f)
                                 ), false)
        }
        val boings = addedCues.map {
            Pattern(game, "manzaiBirds_${it.id}_boing",
                    mutableListOf(), "${it.name} BOING!",
                    mutableListOf(
                            CuePointer(
                                    it.id, 0f, 1.5f),
                            CuePointer(
                                    "manzaiBirds/boing",
                                    1.5f),
                            CuePointer(
                                    "manzaiBirds/donaiyanen",
                                    2.5f)
                                 ), false)
        }

        game.objects += RandomCue(game, "manzaiBirds_randomNormal",
                                  listOf(), "random \"hai hai!\" phrase",
                                  patterns.map { CuePointer(it.id, 0f) },
                                  listOf())
        game.objects += RandomCue(game, "manzaiBirds_randomBoing",
                                  listOf(), "random boing!",
                                  boings.map { CuePointer(it.id, 0f) },
                                  listOf())

        patterns.zip(boings).forEach {
            game.objects += it.first
            game.objects += it.second
        }
    }

}
