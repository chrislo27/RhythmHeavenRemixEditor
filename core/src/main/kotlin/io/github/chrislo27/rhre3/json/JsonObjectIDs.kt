package io.github.chrislo27.rhre3.json

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.entity.EndEntity
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.entity.model.cue.RandomCueEntity
import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.entity.model.multipart.KeepTheBeatEntity
import io.github.chrislo27.rhre3.entity.model.multipart.PatternEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.track.Remix
import kotlin.reflect.KClass


object JsonObjectIDs {

    abstract class Association<T : Entity>(val id: String, val klass: KClass<T>,
                                           val deprecated: List<String> = listOf()) {

        abstract fun createEntity(remix: Remix, node: ObjectNode): T

    }

    val idToClass: Map<String, Association<*>>
    val classToId: Map<KClass<*>, Association<*>>

    init {
        idToClass = mutableMapOf()
        classToId = mutableMapOf()

        fun put(association: Association<*>) {
            if (idToClass.containsKey(association.id)) {
                error("Map already contains key ${association.id}")
            }
            if (classToId.containsKey(association.klass)) {
                error("Map already contains key ${association.klass}")
            }
            idToClass[association.id] = association
            classToId[association.klass] = association

            association.deprecated.forEach {
                if (idToClass.containsKey(it)) {
                    error("Map already contains key $it")
                }
                idToClass[it] = association
            }
        }

        put(object : Association<EndEntity>("end", EndEntity::class) {
            override fun createEntity(remix: Remix, node: ObjectNode): EndEntity {
                return EndEntity(remix, GameRegistry.data.objectMap[node["id"].asText()] as EndRemix)
            }
        })
        put(object : Association<CueEntity>("cue", CueEntity::class) {
            override fun createEntity(remix: Remix, node: ObjectNode): CueEntity {
                return CueEntity(remix, GameRegistry.data.objectMap[node["id"].asText()] as Cue)
            }
        })
        put(object : Association<PatternEntity>("pattern", PatternEntity::class) {
            override fun createEntity(remix: Remix, node: ObjectNode): PatternEntity {
                return PatternEntity(remix, GameRegistry.data.objectMap[node["id"].asText()] as Pattern)
            }
        })
        put(object : Association<RandomCueEntity>("randomCue", RandomCueEntity::class) {
            override fun createEntity(remix: Remix, node: ObjectNode): RandomCueEntity {
                return RandomCueEntity(remix, GameRegistry.data.objectMap[node["id"].asText()] as RandomCue)
            }
        })
        put(object : Association<EquidistantEntity>("equidistant", EquidistantEntity::class) {
            override fun createEntity(remix: Remix, node: ObjectNode): EquidistantEntity {
                return EquidistantEntity(remix, GameRegistry.data.objectMap[node["id"].asText()] as Equidistant)
            }
        })
        put(object : Association<KeepTheBeatEntity>("keepTheBeat", KeepTheBeatEntity::class) {
            override fun createEntity(remix: Remix, node: ObjectNode): KeepTheBeatEntity {
                return KeepTheBeatEntity(remix, GameRegistry.data.objectMap[node["id"].asText()] as KeepTheBeat)
            }
        })

    }

}