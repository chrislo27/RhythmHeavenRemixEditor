package io.github.chrislo27.rhre3.patternstorage

import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.track.Remix
import java.util.*


data class StoredPattern(val uuid: UUID, val name: String, val data: String) {

    @Transient var filename: String? = null

    @delegate:Transient
    val datamodel: Datamodel by lazy {
        object : Datamodel(GameRegistry.data.gameMap[GameRegistry.SPECIAL_ENTITIES_GAME_ID]!!, uuid.toString(), listOf(), name) {
            override fun createEntity(remix: Remix, cuePointer: CuePointer?): ModelEntity<*> {
                error("NO-OP")
            }

            override fun dispose() {
            }
        }
    }

}