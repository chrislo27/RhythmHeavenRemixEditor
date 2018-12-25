package io.github.chrislo27.rhre3.util


data class ExportOptions(val bitrateKbps: Int, val sampleRate: Int, val madeWithComment: Boolean) {

    companion object {
        /**
         * Standard output settings.
         */
        val DEFAULT = ExportOptions(196, 44100, true)
        /**
         * A lower bitrate for quick uploads.
         */
        val QUICKUPLOAD = ExportOptions(128, 44100, true)
        /**
         * Same as [DEFAULT] but with no comments.
         */
        val BLEND = DEFAULT.copy(madeWithComment = false)
    }

}
