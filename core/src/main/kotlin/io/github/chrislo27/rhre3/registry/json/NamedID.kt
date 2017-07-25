package io.github.chrislo27.rhre3.registry.json

abstract class NamedID : Objectish {

    lateinit var id: String
    lateinit var deprecatedIDs: String
    lateinit var name: String

}
