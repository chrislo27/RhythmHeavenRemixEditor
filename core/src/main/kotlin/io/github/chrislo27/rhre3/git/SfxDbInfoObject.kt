package io.github.chrislo27.rhre3.git

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.chrislo27.rhre3.RHRE3


class SfxDbInfoObject {

    @JsonProperty("v")
    var version: Int = -1

    @JsonProperty("editor")
    var requiresVersion: String = RHRE3.VERSION.toString()

}