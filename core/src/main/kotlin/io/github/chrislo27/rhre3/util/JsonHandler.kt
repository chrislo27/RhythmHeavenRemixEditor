package io.github.chrislo27.rhre3.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule


object JsonHandler {

    val OBJECT_MAPPER: ObjectMapper = createObjectMapper(failOnUnknown = true)
//	val GSON: Gson = createObjectMapper()

    @JvmStatic
    fun createObjectMapper(failOnUnknown: Boolean = true): ObjectMapper {
        val mapper = ObjectMapper()
                .enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(MapperFeature.USE_ANNOTATIONS)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .registerModule(AfterburnerModule())
                .registerModule(KotlinModule())

        if (!failOnUnknown) {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }

        return mapper
    }

    @JvmStatic
    fun setFailOnUnknown(fail: Boolean) {
        if (fail) {
            OBJECT_MAPPER.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        } else {
            OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: String): T {
        return OBJECT_MAPPER.readValue(json, T::class.java)
    }

    @JvmStatic
    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return OBJECT_MAPPER.readValue(json, clazz)
    }

    @JvmStatic
    fun toJson(obj: Any): String {
        return OBJECT_MAPPER.writeValueAsString(obj)
    }

    @JvmStatic
    fun <T> toJson(obj: Any, clazz: Class<T>): String {
        return OBJECT_MAPPER.writeValueAsString(clazz.cast(obj))
    }

}
