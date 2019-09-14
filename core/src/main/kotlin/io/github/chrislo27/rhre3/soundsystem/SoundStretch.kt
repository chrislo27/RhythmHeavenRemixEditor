package io.github.chrislo27.rhre3.soundsystem

import java.io.File
import java.util.*


/**
 * A simple wrapper around the [SoundStretch](https://www.surina.net/soundtouch/soundstretch.html) Windows and macOS executables.
 */
object SoundStretch {

    enum class OS(val executableName: String) {
        UNSUPPORTED(""), WINDOWS("SoundStretch_windows.exe"), MACOS("SoundStretch_macOS");
        
        companion object {
            val ALL_VALUES: List<OS> = values().toList()
            val SUPPORTED: List<OS> = ALL_VALUES - UNSUPPORTED
        }
        
    }

    val TEMPO_CHANGE_RANGE: ClosedRange<Float> = -95f..5000f
    val PITCH_CHANGE_RANGE: ClosedRange<Float> = -60f..60f
    val RATE_CHANGE_RANGE: ClosedRange<Float> = -95f..5000f

    val currentOS: OS = try {
        val osName: String = System.getProperty("os.name", "???")?.toLowerCase(Locale.ROOT) ?: "???"
        when {
            "win" in osName -> OS.WINDOWS
            "mac" in osName -> OS.MACOS
            else -> OS.UNSUPPORTED
        }
    } catch (e: Exception) {
        e.printStackTrace()
        OS.UNSUPPORTED
    }

    private fun makeProcessBuilder(directoryPath: File, args: List<String>): ProcessBuilder = when (val os = currentOS) {
        OS.WINDOWS, OS.MACOS -> {
            ProcessBuilder(listOf(directoryPath.resolve(os.executableName).absolutePath) + args)
        }
        else -> throw NotImplementedError("Not implemented for the current operating system, supported OSs: ${OS.SUPPORTED}")
    }

    /**
     * Returns a WAV output stream with SoundStretch having applied the result.
     * Throws an error if the change parameters are not in bounds.
     * @param executableDir The File object pointing to the directory of the executables
     * @param input A WAV file
     * @param output The file to which to output the modified WAV data
     * @param tempoPercent Changes the sound tempo by this amount of percents. See [TEMPO_CHANGE_RANGE]
     * @param pitchSemitones Changes the sound pitch by this amount of semitones. See [PITCH_CHANGE_RANGE]
     * @param ratePercent Changes the sound rate by this amount of percents. See [RATE_CHANGE_RANGE]
     * @param quick Enables the -quick parameter. Gains speed but will probably lose quality.
     */
    fun processStreams(executableDir: File, input: File, output: File, tempoPercent: Float, pitchSemitones: Float, ratePercent: Float, quick: Boolean): Int {
        require(tempoPercent in TEMPO_CHANGE_RANGE) { "Tempo percent must be between $TEMPO_CHANGE_RANGE%, got $tempoPercent" }
        require(pitchSemitones in PITCH_CHANGE_RANGE) { "Pitch semitones must be between $PITCH_CHANGE_RANGE%, got $pitchSemitones" }
        require(ratePercent in RATE_CHANGE_RANGE) { "Rate percent must be between $RATE_CHANGE_RANGE%, got $ratePercent" }

        val args = mutableListOf(input.absolutePath, output.absolutePath,
                "-tempo=$tempoPercent", "-pitch=$pitchSemitones", "-rate=$ratePercent")
        if (quick) {
            args += "-quick"
        }
        val process: Process = makeProcessBuilder(executableDir, args)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
        process.waitFor()
        return process.exitValue()
    }

}