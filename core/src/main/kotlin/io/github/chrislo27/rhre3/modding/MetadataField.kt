package io.github.chrislo27.rhre3.modding

import java.util.*


data class MetadataField(val jsonField: String, val name: String, val idTypes: EnumSet<IDType>, val unknown: Boolean = false) {
    companion object {
        val GLOBAL_FIELDS: Map<String, MetadataField> =
                linkedMapOf(MetadataField("note", "Note", EnumSet.allOf(IDType::class.java)).toPair())
        val GAME_FIELDS: Map<ModdingGame, Map<String, MetadataField>> =
                mapOf(ModdingGame.MEGAMIX_NA to linkedMapOf(
                        MetadataField("sub", "Sub", EnumSet.of(IDType.DATAMODEL)).toPair(),
                        MetadataField("name", "Name", EnumSet.of(IDType.GAME)).toPair(),
                        MetadataField("engine", "Engine", EnumSet.of(IDType.GAME)).toPair(),
                        MetadataField("tempoFile", "Tempo File", EnumSet.of(IDType.GAME)).toPair(),
                        MetadataField("index", "Index", EnumSet.of(IDType.GAME)).toPair()
                                                           ),
                      ModdingGame.DS_NA to linkedMapOf(
                              MetadataField("sub", "Sub", EnumSet.of(IDType.DATAMODEL)).toPair(),
                              MetadataField("cue", "Cue", EnumSet.of(IDType.DATAMODEL)).toPair(),
                              MetadataField("id", "ID", EnumSet.of(IDType.GAME)).toPair(),
                              MetadataField("ftc", "ftc", EnumSet.of(IDType.GAME)).toPair()
                                                      )
                     )
    }

    private fun toPair(): Pair<String, MetadataField> = jsonField to this
}
