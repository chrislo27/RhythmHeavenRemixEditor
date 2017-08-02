package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.entity.ModelEntity


abstract class Datamodel(val game: Game, val id: String, val deprecatedIDs: List<String>, val name: String)
    : Disposable {

    abstract fun createEntity(): ModelEntity<*>

    var hidden: Boolean = false

}