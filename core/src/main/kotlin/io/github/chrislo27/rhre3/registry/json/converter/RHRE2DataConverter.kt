package io.github.chrislo27.rhre3.registry.json.converter

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.chrislo27.rhre3.registry.json.impl.CueObject
import io.github.chrislo27.rhre3.registry.json.impl.DataObject
import io.github.chrislo27.rhre3.registry.json.impl.PatternObject
import io.github.chrislo27.rhre3.registry.json.impl.pointer.CuePointerObject
import java.io.File

fun main(args: Array<String>) {
    val inputDir: File = File(args.first())
    val outputDir: File = File(args[1])

    outputDir.mkdirs()

    val dirs: List<File> = inputDir.listFiles { dir ->
        println(dir.path)
        return@listFiles dir.isDirectory && dir.resolve("data.json").exists()
    }.toList()

    val objectMapper = ObjectMapper().enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
            .enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
            .enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(AfterburnerModule()).registerKotlinModule()

    println("Found ${dirs.size} dirs")

    dirs.forEach { dir ->
        println("Processing $dir")
        val outDir: File = outputDir.resolve(dir.nameWithoutExtension + "/")
        outDir.mkdir()

        val jsonFile = dir.resolve("data.json")
        val old: GameObject = objectMapper.readValue<GameObject>(jsonFile)
        val data: DataObject = DataObject()

        run {
            // do the conversion
            data.id = old.gameID!!
            data.deprecatedIDs = listOf()
            data.name = old.gameName!!
            data.requiresVersion = "v3.0.0"
            val objects = mutableListOf<Any>()

            old.cues!!.forEach { soundobj ->
                val cueObj = CueObject()
                cueObj.id = soundobj.id!!
                cueObj.name = soundobj.name ?: cueObj.id
                cueObj.duration = soundobj.duration
//                if (soundobj.fileExtension != "ogg") {
//                    cueObj.fileExtension = soundobj.fileExtension
//                }
                cueObj.repitchable = soundobj.canAlterPitch
                cueObj.stretchable = soundobj.canAlterDuration
                cueObj.deprecatedIDs = soundobj.deprecatedIDs?.toList() ?: listOf()
                objects += cueObj
            }

            old.patterns!!.forEach { pat ->
                val patternObj = PatternObject()
                patternObj.id = pat.id!!
                patternObj.name = pat.name!!
                patternObj.deprecatedIDs = pat.deprecatedIDs?.toList() ?: listOf()
                val cues = mutableListOf<CuePointerObject>()
                pat.cues!!.mapTo(cues) { patcue ->
                    val pointer = CuePointerObject()

                    pointer.id = patcue.id!!
                    pointer.beat = patcue.beat
                    pointer.duration = patcue.duration ?: 0f
                    pointer.semitone = patcue.semitone ?: 0
                    pointer.track = patcue.track

                    return@mapTo pointer
                }

                patternObj.cues = cues
                objects += patternObj
            }

            data.objects = objects
        }

        val newJsonFile = outDir.resolve("data.json")
        newJsonFile.createNewFile()
        newJsonFile.writeText(objectMapper.writeValueAsString(data))
    }

}

/**
 * dumb stuff
 */
private class GameObject {

    var gameID: String? = null
    var gameName: String? = null
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var series: String? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var priority: Int = 0

    var cues: Array<SoundObject>? = null

    var patterns: Array<PatternObject>? = null

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var usesGeneratorHelper = false
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var notRealGame: Boolean = false

    class SoundObject {

        var id: String? = null

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var fileExtension = "ogg"

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var deprecatedIDs: Array<String>? = null

        var name: String? = null

        var duration = 0.5f

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var canAlterPitch = false
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var canAlterDuration = false

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var introSound: String? = null

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var baseBpm = 0f

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var pan: Float = 0f

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var loops: Boolean? = null
    }

    class PatternObject {

        var id: String? = null
        var name: String? = null

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var deprecatedIDs: Array<String>? = null

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var isStretchable = false

        var cues: Array<CueObject>? = null

        class CueObject {

            var id: String? = null
            var beat: Float = 0.toFloat()

            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            var track: Int = 0

            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            var duration: Float? = 0f

            @JsonInclude(JsonInclude.Include.NON_DEFAULT)
            var semitone: Int? = 0
        }
    }

}
