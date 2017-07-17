package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.chrislo27.rhre3.registry.json.NamedIDObject
import io.github.chrislo27.rhre3.registry.json.Verifiable
import io.github.chrislo27.toolboks.version.Version

class DataObject : NamedIDObject(), Verifiable {

    lateinit var requiresVersion: String
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    lateinit var objects: List<Any>

    override fun verify(): String? {
        val builder = StringBuilder()

        Version.fromString(requiresVersion)

        return if (builder.isEmpty()) null else builder.toString()
    }
}