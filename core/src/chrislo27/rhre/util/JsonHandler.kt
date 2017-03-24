package chrislo27.rhre.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder


object JsonHandler {

//	val OBJECT_MAPPER = createObjectMapper()
	val GSON: Gson = createObjectMapper()

	@JvmStatic
	fun createObjectMapper(): Gson {
		return GsonBuilder().setPrettyPrinting().create()
	}

	@JvmStatic
	inline fun <reified T> fromJson(json: String): T {
		return GSON.fromJson(json, T::class.java)
	}

	@JvmStatic
	fun <T> fromJson(json: String, clazz: Class<T>): T {
		return GSON.fromJson(json, clazz)
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
		return GSON.toJson(obj)
	}

	@JvmStatic
	fun <T> toJson(obj: Any, clazz: Class<T>): String {
		return GSON.toJson(obj, clazz)
	}

}