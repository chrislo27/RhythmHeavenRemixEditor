package io.github.chrislo27.rhre3.editor


class SubbeatSection {

    var start: Float = 0f
        set(value) {
            field = value
            if (start > end)
                normalise()
        }

    var end: Float = 0f
        set(value) {
            field = value
            if (start > end)
                normalise()
        }

    var enabled: Boolean = false

    private fun normalise() {
        if (start > end) {
            val delta = start - end
            val oldEnd = end
            start = end
            end = oldEnd + delta
        }
    }

}