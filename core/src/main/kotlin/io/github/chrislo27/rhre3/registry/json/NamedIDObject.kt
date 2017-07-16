package io.github.chrislo27.rhre3.registry.json


abstract class NamedIDObject : IdentifiableObject(), NamedObject {

    lateinit override var id: String
    lateinit override var name: String

}