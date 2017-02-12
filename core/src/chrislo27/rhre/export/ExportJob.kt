package chrislo27.rhre.export

import chrislo27.rhre.entity.SoundCueAction
import chrislo27.rhre.entity.SoundCueActionProvider
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.DynamicByteBuffer
import chrislo27.rhre.util.HFloat
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable
import java.io.FileOutputStream


class ExportJob(val remix: Remix, val handle: FileHandle) : Disposable {

	private lateinit var stream: FileOutputStream

	private var byteBuffer: DynamicByteBuffer = DynamicByteBuffer(17000000, 2f)

	private val musicMap: MutableMap<String, OpenALMusic> = mutableMapOf()
	private val scActions: MutableList<SoundCueAction> = mutableListOf()

	// TODO not assume same sample rate?

	fun processJob(): Boolean {
		start()

		if (musicMap.isEmpty()) return false

		var realAmt: Long = 0L
		while (true) {
			var buffer: ByteArray = ByteArray(4096 * 10)

			var amount: Int = (remix.music!!.music as OpenALMusic).readWithMonoConversion(buffer)

			fun applyModifications(buffer: ByteArray, amt: Int, pitch: Float, volume: Float): ByteArray {
				if (pitch == 1.0f && volume == 1.0f) return buffer

				println("applying new changes of pitch float $pitch $volume")

//				val newSize = (amt / pitch).toInt()
//				val moddedBuffer = ByteArray(newSize) {
//					index ->
//					val oldIndex: Int = (amt * (index.toFloat() / newSize.toFloat())).toInt()
//							.coerceIn(0, amt - 1)
//
//					return@ByteArray (buffer[oldIndex] * volume).coerceIn(-127f..127f).toByte()
//				}
//
//				amount = newSize
//
//				return moddedBuffer

				val newSize = (buffer.size / 2 / pitch).toInt()
				val moddedShortBuffer = ShortArray(newSize) {
					index ->
					val oldIndex: Int = (buffer.size * (index.toFloat() / newSize.toFloat())).toInt()
							.coerceIn(0, buffer.size - 1)

					return@ShortArray HFloat.convertFloatToHFloat(
							HFloat.convertHFloatToFloat(
									(buffer[oldIndex].toInt() or (buffer[oldIndex + 1].toInt() shl 8)).toShort())
									* volume)
				}

				return ByteArray(moddedShortBuffer.size * 2) {
					index ->
					var data = moddedShortBuffer[index / 2].toInt()

					if (index % 2 == 1) {
						data = (data ushr 8)
					}

					data = (data and 0xFF)

					return@ByteArray data.toByte()
				}
			}

			buffer = applyModifications(buffer, amount, 1.75f, remix.musicVolume)

			println("reading $amount bytes - buffer size ${buffer.size}")

			if (amount <= 0) {
				val array = byteBuffer.array()
				stream.write(array)
				dispose()
				println("done! ${byteBuffer.capacity()} ${byteBuffer.limit()}")
				break
			}

			byteBuffer.put(buffer, 0, amount)

			realAmt += amount

		}

		println("realamt $realAmt")

		System.gc()

		return true
	}

	override fun dispose() {
		stream.close()

		musicMap.forEach { k, v -> v.reset() }
	}

	private fun start() {
		if (handle.exists()) {
			handle.copyTo(handle.sibling(handle.nameWithoutExtension() + "-copy." + handle.extension()))
		}

		handle.delete()
		handle.file().createNewFile()

		stream = FileOutputStream(handle.file())

		if (remix.music != null) {
			musicMap["music"] = remix.music!!.music as OpenALMusic
		}

		remix.entities.filterIsInstance(
				SoundCueActionProvider::class.java).flatMap { it.provide() }.forEach { scActions.add(it) }
		scActions.forEach {
			if (!musicMap.contains(it.cue.id)) {
				musicMap[it.cue.id] = it.cue.alMusic
			}
		}

		musicMap.values.forEach { it.reset() }
	}

}

fun OpenALMusic.readWithMonoConversion(array: ByteArray): Int {
	if (this.channels == 1) {
		val halfBuffer = ByteArray(array.size / 2)
		val amt = this.read(halfBuffer)
		if (amt <= 0) return amt

		for (i in 0..halfBuffer.size - 1) {
			array[i * 2] = halfBuffer[i]
			array[i * 2 + 1] = halfBuffer[i]
		}

		println("converted from mono to stereo")

		return amt * 2
	} else {
		return this.read(array)
	}
}
