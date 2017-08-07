package io.github.chrislo27.rhre3.entity.model

import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.track.Remix


abstract class ModelEntity<out M : Datamodel>(remix: Remix, val datamodel: M) : Entity(remix) {


}