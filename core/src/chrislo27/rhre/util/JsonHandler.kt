package chrislo27.rhre.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.afterburner.AfterburnerModule


object JsonHandler {

	val OBJECT_MAPPER = createObjectMapper()

	@JvmStatic
	fun createObjectMapper(): ObjectMapper {
		return ObjectMapper()
				.registerModule(AfterburnerModule())
				.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID)
				.enable(SerializationFeature.WRITE_NULL_MAP_VALUES)
				.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
				.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
				.enable(SerializationFeature.INDENT_OUTPUT)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.enable(JsonParser.Feature.ALLOW_COMMENTS)
				.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
				.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
				.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
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
	inline fun <reified T> fromJson(json: ByteArray): T {
		return OBJECT_MAPPER.readValue(json, T::class.java)
	}

	@JvmStatic
	fun <T> fromJson(json: ByteArray, clazz: Class<T>): T {
		return OBJECT_MAPPER.readValue(json, clazz)
	}

	@JvmStatic
	fun toJson(obj: Any): String {
		return OBJECT_MAPPER.writeValueAsString(obj)
	}

}