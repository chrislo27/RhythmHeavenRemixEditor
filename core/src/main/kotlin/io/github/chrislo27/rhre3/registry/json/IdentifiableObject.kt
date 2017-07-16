package io.github.chrislo27.rhre3.registry.json


abstract class IdentifiableObject {

    lateinit open var id: String

    lateinit open var deprecatedIDs: List<String>

}