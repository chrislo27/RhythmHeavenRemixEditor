package io.github.chrislo27.rhre3.jcvib

import java.nio.ByteBuffer
import java.util.*


/**
 * [https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/rumble_data_table.md]
 * implementing the [jcvib format][https://github.com/CTCaer/jc_toolkit/commit/999afff7235aceac3aa82e322a1908890dd7d7c3]
 */
object JoyConVibration {

    private const val RRAW = 0x52524157
    private val LOG2 = Math.log10(2.0)

    private val amplitudeTable: NavigableMap<Double, Pair<Byte, Short>> = listOf(
            0.000000 to (0x00 to 0x0040),
            0.007843 to (0x02 to 0x8040),
            0.011823 to (0x04 to 0x0041),
            0.014061 to (0x06 to 0x8041),
            0.016720 to (0x08 to 0x0042),
            0.019885 to (0x0a to 0x8042),
            0.023648 to (0x0c to 0x0043),
            0.028123 to (0x0e to 0x8043),
            0.033442 to (0x10 to 0x0044),
            0.039771 to (0x12 to 0x8044),
            0.047296 to (0x14 to 0x0045),
            0.056246 to (0x16 to 0x8045),
            0.066886 to (0x18 to 0x0046),
            0.079542 to (0x1a to 0x8046),
            0.094592 to (0x1c to 0x0047),
            0.112491 to (0x1e to 0x8047),
            0.117471 to (0x20 to 0x0048),
            0.122671 to (0x22 to 0x8048),
            0.128102 to (0x24 to 0x0049),
            0.133774 to (0x26 to 0x8049),
            0.139697 to (0x28 to 0x004a),
            0.145882 to (0x2a to 0x804a),
            0.152341 to (0x2c to 0x004b),
            0.159085 to (0x2e to 0x804b),
            0.166129 to (0x30 to 0x004c),
            0.173484 to (0x32 to 0x804c),
            0.181166 to (0x34 to 0x004d),
            0.189185 to (0x36 to 0x804d),
            0.197561 to (0x38 to 0x004e),
            0.206308 to (0x3a to 0x804e),
            0.215442 to (0x3c to 0x004f),
            0.224982 to (0x3e to 0x804f),
            0.229908 to (0x40 to 0x0050),
            0.234943 to (0x42 to 0x8050),
            0.240087 to (0x44 to 0x0051),
            0.245345 to (0x46 to 0x8051),
            0.250715 to (0x48 to 0x0052),
            0.256206 to (0x4a to 0x8052),
            0.261816 to (0x4c to 0x0053),
            0.267549 to (0x4e to 0x8053),
            0.273407 to (0x50 to 0x0054),
            0.279394 to (0x52 to 0x8054),
            0.285514 to (0x54 to 0x0055),
            0.291765 to (0x56 to 0x8055),
            0.298154 to (0x58 to 0x0056),
            0.304681 to (0x5a to 0x8056),
            0.311353 to (0x5c to 0x0057),
            0.318171 to (0x5e to 0x8057),
            0.325138 to (0x60 to 0x0058),
            0.332258 to (0x62 to 0x8058),
            0.339534 to (0x64 to 0x0059),
            0.346969 to (0x66 to 0x8059),
            0.354566 to (0x68 to 0x005a),
            0.362331 to (0x6a to 0x805a),
            0.370265 to (0x6c to 0x005b),
            0.378372 to (0x6e to 0x805b),
            0.386657 to (0x70 to 0x005c),
            0.395124 to (0x72 to 0x805c),
            0.403777 to (0x74 to 0x005d),
            0.412619 to (0x76 to 0x805d),
            0.421652 to (0x78 to 0x005e),
            0.430885 to (0x7a to 0x805e),
            0.440321 to (0x7c to 0x005f),
            0.449964 to (0x7e to 0x805f),
            0.459817 to (0x80 to 0x0060),
            0.469885 to (0x82 to 0x8060),
            0.480174 to (0x84 to 0x0061),
            0.490689 to (0x86 to 0x8061),
            0.501433 to (0x88 to 0x0062),
            0.512413 to (0x8a to 0x8062),
            0.523633 to (0x8c to 0x0063),
            0.535100 to (0x8e to 0x8063),
            0.546816 to (0x90 to 0x0064),
            0.558790 to (0x92 to 0x8064),
            0.571027 to (0x94 to 0x0065),
            0.583530 to (0x96 to 0x8065),
            0.596307 to (0x98 to 0x0066),
            0.609365 to (0x9a to 0x8066),
            0.622708 to (0x9c to 0x0067),
            0.636344 to (0x9e to 0x8067),
            0.650279 to (0xa0 to 0x0068),
            0.664518 to (0xa2 to 0x8068),
            0.679069 to (0xa4 to 0x0069),
            0.693939 to (0xa6 to 0x8069),
            0.709133 to (0xa8 to 0x006a),
            0.724662 to (0xaa to 0x806a),
            0.740529 to (0xac to 0x006b),
            0.756745 to (0xae to 0x806b),
            0.773316 to (0xb0 to 0x006c),
            0.790249 to (0xb2 to 0x806c),
            0.807554 to (0xb4 to 0x006d),
            0.825237 to (0xb6 to 0x806d),
            0.843307 to (0xb8 to 0x006e),
            0.861772 to (0xba to 0x806e),
            0.880643 to (0xbc to 0x006f),
            0.899928 to (0xbe to 0x806f),
            0.919633 to (0xc0 to 0x0070),
            0.939771 to (0xc2 to 0x8070),
            0.960348 to (0xc4 to 0x0071),
            0.981378 to (0xc6 to 0x8071),
            1.002867 to (0xc8 to 0x0072)
                                                                                )
            .map {
                it.first to (it.second.first.toByte() to it.second.second.toShort())
            }.toMap(TreeMap())

    private fun log2(value: Double): Double {
        return Math.log10(value) / LOG2
    }

    private fun frequencyToHexFreq(frequency: Float): Byte {
        val freq = frequency.coerceIn(0f, 1252f)
        return Math.round(log2(freq * 0.1) * 32.0).toByte()
    }

    fun frequencyToHF(frequency: Float): Int {
        // Convert to Joy-Con HF range. Range in big-endian: 0x0004-0x01FC with +0x0004 steps.
        return ((frequencyToHexFreq(frequency) - 0x60) * 4).toInt()
    }

    fun frequencyToLF(frequency: Float): Short {
        // Convert to Joy-Con LF range. Range: 0x01-0x7F.
        return (frequencyToHexFreq(frequency) - 0x40).toShort()
    }

    fun amplitudeToHA(amplitude: Float): Short {
        return ((amplitudeTable.floorEntry(
                amplitude.toDouble().coerceAtMost(
                        1.003)))?.value?.first ?: amplitudeTable.firstEntry().value.first).toShort()
    }

    fun amplitudeToLA(amplitude: Float): Int {
        return ((amplitudeTable.floorEntry(
                amplitude.toDouble().coerceAtMost(
                        1.003)))?.value?.second ?: amplitudeTable.firstEntry().value.second).toInt()
    }

    fun writeToInt(highFreq: Float, highAmp: Float, lowFreq: Float, lowAmp: Float): Int {
        return writeToInt(frequencyToHF(highFreq), amplitudeToHA(highAmp), frequencyToLF(lowFreq),
                   amplitudeToLA(lowAmp))
    }

    fun writeToInt(highFreq: Int, highAmp: Short, lowFreq: Short, lowAmp: Int): Int {
        return ((highFreq and 0xFF) shl 24) or ((highAmp + 0x01) and 0xFF shl 16) or ((lowFreq + 0x80) and 0xFF shl 8) or (lowAmp and 0xFF)
    }

    fun jcvib(sampleRate: Short, rumblePatterns: IntArray): ByteArray {
        // Magic RRAW (int) + ms sample rate (short) + number of rumble patterns (int) + rumble pattern data (int)
        val buffer = ByteBuffer.allocate(4 + 2 + 4 + 4 * rumblePatterns.size)
        buffer.putInt(RRAW)
        buffer.putShort(sampleRate)
        buffer.putInt(rumblePatterns.size)
        rumblePatterns.forEach {
            buffer.putInt(it)
        }

        return buffer.array()
    }

}