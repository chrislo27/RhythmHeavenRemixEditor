package io.github.chrislo27.rhre3.midi

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.AssetRegistry
import javax.sound.midi.*
import kotlin.concurrent.thread


object MidiHandler : Disposable {

    private const val SEARCH_INTERVAL = 1000L
    private const val MIDI_NOTE_ID = "sfx_sing_loop"
    @Volatile
    private var midiDevice: MidiDevice? = null
    @Volatile
    var isSearching: Boolean = false

    init {
        thread(isDaemon = true) {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(SEARCH_INTERVAL)
                } catch (e: InterruptedException) {
                    // Ignored
                }
                if (isSearching) {
                    val device = midiDevice
                    if (device == null) {
                        val deviceInfo = MidiSystem.getMidiDeviceInfo().iterator()
                        while (midiDevice == null && deviceInfo.hasNext()) {
                            try {
                                val possibleDevice = MidiSystem.getMidiDevice(deviceInfo.next())
                                if (possibleDevice !is Synthesizer && possibleDevice !is Sequencer && possibleDevice.maxTransmitters != 0) {
                                    this.midiDevice = possibleDevice
                                    possibleDevice.open()
                                    possibleDevice.transmitter.receiver = MidiReceiver(possibleDevice)
                                    Toolboks.LOGGER.info("Got midi device: ${possibleDevice.deviceInfo}")
                                    break
                                }
                            } catch (e: MidiUnavailableException) {
                                // Ignored
                                e.printStackTrace()
                            }
                        }
                    } else if (!device.isOpen) {
                        (device.transmitter.receiver as? MidiReceiver)?.close()
                        this.midiDevice?.close()
                        this.midiDevice = null
                    }
                }
            }
        }
    }

    override fun dispose() {
        midiDevice?.close()
    }

    class MidiReceiver(val device: MidiDevice) : Receiver {

        private val notes: MutableMap<Int, Note> = mutableMapOf()

        inner class Note(val semitone: Int, val volume: Float) {

            private val soundID: Long

            init {
                soundID = if (MIDI_NOTE_ID in AssetRegistry) {
                    val sound = AssetRegistry.get<Sound>(MIDI_NOTE_ID)
                    sound.loop(volume, Semitones.getALPitch(semitone), 0f)
                } else {
                    -1L
                }
            }

            fun onRemove() {
                if (soundID != -1L && MIDI_NOTE_ID in AssetRegistry) {
                    val sound = AssetRegistry.get<Sound>(MIDI_NOTE_ID)
                    sound.stop(soundID)
                }
            }
        }

        override fun send(message: MidiMessage?, timeStamp: Long) {
            if (message == null || message !is ShortMessage)
                return

            val command = message.command
            val semitone: Int = message.data1 - 60
            val volume = Math.sqrt(message.data2 / 127.0).toFloat()

            fun on() {
                notes.remove(semitone)?.onRemove()
                notes[semitone] = Note(semitone, volume)
//                println("Note on: ${Semitones.getSemitoneName(semitone)} $volume")
            }

            fun off() {
                notes.remove(semitone)?.onRemove()
//                println("Note off: ${Semitones.getSemitoneName(semitone)}")
            }

            when (command) {
                ShortMessage.NOTE_ON -> {
                    if (volume <= 0f) off() else on()
                }
                ShortMessage.NOTE_OFF -> off()
            }
        }

        override fun close() {
            notes.values.forEach(Note::onRemove)
            notes.clear()

            if (midiDevice == device) {
                midiDevice?.close()
                midiDevice = null
                Toolboks.LOGGER.info("Closing midi device: ${device.deviceInfo}")
            }
        }

    }
}