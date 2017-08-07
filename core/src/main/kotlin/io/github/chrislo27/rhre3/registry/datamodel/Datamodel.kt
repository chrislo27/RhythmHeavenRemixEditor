package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.track.Remix


abstract class Datamodel(val game: Game, val id: String, val deprecatedIDs: List<String>, val name: String)
    : Disposable {

    abstract fun createEntity(remix: Remix): ModelEntity<*>

    var hidden: Boolean = false
    open val pickerName: String = name

}