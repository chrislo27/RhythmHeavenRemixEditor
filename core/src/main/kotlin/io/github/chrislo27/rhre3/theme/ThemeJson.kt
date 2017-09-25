package io.github.chrislo27.rhre3.theme

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.util.*


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@JacksonAnnotationsInside
@JsonSerialize(using = LibgdxColorSerializer::class)
@JsonDeserialize(using = LibgdxColorDeserializer::class)
annotation class HexColor

class LibgdxColorDeserializer(cl: Class<*>? = null) : StdDeserializer<Color>(cl) {

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Color {
        val node: JsonNode = jp.codec.readTree(jp)
        var str = node.asText().replace("#", "")

        if (str.length < 8)
            str += "F".repeat(8 - str.length)

        return Color(str.substring(0, 2).toInt(16) / 255f,
                     str.substring(2, 4).toInt(16) / 255f,
                     str.substring(4, 6).toInt(16) / 255f,
                     str.substring(6, 8).toInt(16) / 255f)
    }

}

class LibgdxColorSerializer(cl: Class<Color>? = null) : StdSerializer<Color>(cl) {

    override fun serialize(value: Color, gen: JsonGenerator, provider: SerializerProvider) {
        fun String.padZeroes(num: Int = 2): String =
                this.padStart(num, '0')
        gen.writeString(("#${(value.r * 255).toInt().toString(16).padZeroes()}" +
                (value.g * 255).toInt().toString(16).padZeroes() +
                (value.b * 255).toInt().toString(16).padZeroes() +
                if (value.a < 1f) (value.a * 255).toInt().toString(16).padZeroes() else ""
                                )
                                .toUpperCase(Locale.ROOT))
    }

}
