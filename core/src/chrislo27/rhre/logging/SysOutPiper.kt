package chrislo27.rhre.logging

import org.apache.commons.io.output.TeeOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

object SysOutPiper {

	lateinit var oldOut: PrintStream
		private set
	lateinit var oldErr: PrintStream
		private set

	private lateinit var newOut: TeeOutputStream
	private lateinit var newErr: TeeOutputStream

	private lateinit var stream: FileOutputStream

	fun pipe() {
		oldOut = System.out
		oldErr = System.err

		val folder: File = File("logs/")
		folder.mkdir()
		val file: File = File(folder, "log_" + SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(
				Date(System.currentTimeMillis())) + ".txt")
		file.createNewFile()

		stream = FileOutputStream(file)

		newOut = TeeOutputStream(oldOut, stream)
		newErr = TeeOutputStream(oldErr, stream)

		System.setOut(PrintStream(newOut))
		System.setErr(PrintStream(newErr))

		Runtime.getRuntime().addShutdownHook(thread(start = false) {
			stream.close()
		})
	}

}
