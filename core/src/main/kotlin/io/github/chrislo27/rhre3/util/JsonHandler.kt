package io.github.chrislo27.rhre3.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.KotlinModule


object JsonHandler {

    val OBJECT_MAPPER: ObjectMapper = createObjectMapper()
//	val GSON: Gson = createObjectMapper()

    @JvmStatic
    fun createObjectMapper(): ObjectMapper {
        return ObjectMapper()
                .enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
                .enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .registerModule(AfterburnerModule())
                .registerModule(KotlinModule())
    }

//	@JvmStatic
//	fun createObjectMapper(): Gson {
//		return GsonBuilder().setPrettyPrinting().create()
//	}

    @JvmStatic
    inline fun <reified T> fromJson(json: String): T {
        return OBJECT_MAPPER.readValue(json, T::class.java)
//		return GSON.fromJson(json, T::class.java)
    }

    @JvmStatic
    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return OBJECT_MAPPER.readValue(json, clazz)
//		return GSON.fromJson(json, clazz)
    }

//	@JvmStatic
//	inline fun <reified T> fromJson(json: ByteArray): T {
//		return GSON.fromJson(json, T::class.java)
//	}
//
//	@JvmStatic
//	fun <T> fromJson(json: ByteArray, clazz: Class<T>): T {
//		return GSON.fromJson(json, clazz)
//	}

    @JvmStatic
    fun toJson(obj: Any): String {
        return OBJECT_MAPPER.writeValueAsString(obj)
//		return GSON.toJson(obj)
    }

    @JvmStatic
    fun <T> toJson(obj: Any, clazz: Class<T>): String {
        return OBJECT_MAPPER.writeValueAsString(clazz.cast(obj))
//		return GSON.toJson(obj, clazz)
    }

}
