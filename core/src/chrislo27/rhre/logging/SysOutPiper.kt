package chrislo27.rhre.logging

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import ionium.templates.Main
import ionium.util.MemoryUtils
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
		val file: File = File(folder, "log_" + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
				Date(System.currentTimeMillis())) + ".txt")
		file.createNewFile()

		stream = FileOutputStream(file)

		val ps = PrintStream(stream)
		ps.println("==============\nAUTO-GENERATED\n==============\n")
		val builder = StringBuilder()
		builder.append("Game Specifications:\n")
		builder.append("   Version: " + Main.version + "\n")
		builder.append("   Application type: " + Gdx.app.type.toString() + "\n")

		builder.append("\n")

		builder.append("Operating System Specifications:\n")
		builder.append("   Java Version: " + System.getProperty("java.version") + " " + System.getProperty(
				"sun.arch.data.model") + " bit" + "\n")
		builder.append("   OS Name: " + System.getProperty("os.name") + "\n")
		builder.append("   OS Version: " + System.getProperty("os.version") + "\n")
		builder.append("   JVM memory available: " + MemoryUtils.getMaxMemory() + " KB\n")

		builder.append("\n")

		builder.append("Processor Specifications:\n")
		builder.append("   Cores: " + MemoryUtils.getCores() + "\n")

		builder.append("\n")

		builder.append("Graphics Specifications:\n")
		builder.append("   Resolution: " + Gdx.graphics.width + "x" + Gdx.graphics.height + "\n")
		builder.append("   Fullscreen: " + Gdx.graphics.isFullscreen + "\n")
		builder.append("   GL_VENDOR: " + Gdx.gl.glGetString(GL20.GL_VENDOR) + "\n")
		builder.append("   Graphics: " + Gdx.gl.glGetString(GL20.GL_RENDERER) + "\n")
		builder.append("   GL Version: " + Gdx.gl.glGetString(GL20.GL_VERSION) + "\n")
		ps.println(builder.toString())
		ps.println("\n")
		ps.flush()

		newOut = TeeOutputStream(oldOut, stream)
		newErr = TeeOutputStream(oldErr, stream)

		System.setOut(PrintStream(newOut))
		System.setErr(PrintStream(newErr))

		Runtime.getRuntime().addShutdownHook(thread(start = false) {
			stream.close()
		})
	}

}
