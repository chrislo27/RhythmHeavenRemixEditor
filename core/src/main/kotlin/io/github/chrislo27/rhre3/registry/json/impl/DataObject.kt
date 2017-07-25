package io.github.chrislo27.rhre3.registry.json.impl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.chrislo27.rhre3.registry.json.NamedID
import io.github.chrislo27.rhre3.registry.json.Objectish

class DataObject : NamedID() {

    lateinit var version: String

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var group: String? = null

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    lateinit var objects: List<Objectish>

}