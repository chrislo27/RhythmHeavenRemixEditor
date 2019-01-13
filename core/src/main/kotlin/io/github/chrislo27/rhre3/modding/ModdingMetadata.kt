package io.github.chrislo27.rhre3.modding


abstract class ModdingMetadata {

    abstract fun computeString(): String

}

class StaticFunction(val result: String) : ModdingMetadata() {
    override fun computeString(): String = result
}

//class WidthRangeFunction(val results: Map<ClosedRange<Float>, String>, val elseCase: String = "") : ModdingMetadata() {
//
//    override fun computeString(): String {
//    }
//
//}