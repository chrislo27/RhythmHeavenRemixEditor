package io.github.chrislo27.rhre3.entity.model

import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


abstract class ModelEntity<out M : Datamodel>(val datamodel: M) : Entity() {


}