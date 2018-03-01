package io.github.chrislo27.rhre3.entity.model


interface IStretchable {

    companion object {
        const val STRETCH_AREA: Float = 1f / 12f
        const val MIN_STRETCH: Float = 1f / 8f
    }

    val isStretchable: Boolean

}