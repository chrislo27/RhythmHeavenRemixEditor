package io.github.chrislo27.toolboks.logging

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.oshi.OSHI
import io.github.chrislo27.toolboks.util.MemoryUtils
import org.apache.commons.io.output.TeeOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 * Splits the console out/err into a file and normal console.
 */
object SysOutPiper {

    lateinit var oldOut: PrintStream
        private set
    lateinit var oldErr: PrintStream
        private set

    private lateinit var newOut: TeeOutputStream
    private lateinit var newErr: TeeOutputStream

    private lateinit var stream: FileOutputStream

    @Volatile
    private var piped: Boolean = false

    fun pipe(args: List<String>, game: ToolboksGame, folder: File) {
        if (piped)
            return
        piped = true
        oldOut = System.out
        oldErr = System.err

        folder.mkdirs()
        val file: File = File(folder, "log_" + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
                Date(System.currentTimeMillis())) + ".txt")
        file.createNewFile()

        stream = FileOutputStream(file)

        val ps = PrintStream(stream)
        ps.println("==============\nAUTO-GENERATED\n==============\n")
        val builder = StringBuilder()
        builder.append("Program Specifications:\n")
        builder.append("    Launch arguments: $args\n")
        builder.append("    Version: " + game.version.toString() + "\n")
        builder.append("    Application type: " + Gdx.app.type.toString() + "\n")
        builder.append("    Lazy loading enabled: " + LazySound.loadLazilyWithAssetManager + "\n")

        builder.append("\n")

        builder.append("Operating System Specifications:\n")
        builder.append("    Java Version: " + System.getProperty("java.version") + " " + System.getProperty(
                "sun.arch.data.model") + " bit" + "\n")
        builder.append("    Java Vendor: ${System.getProperty("java.vendor")}\n")
        builder.append("    Kotlin Version: ${kotlin.KotlinVersion.CURRENT}\n")
        builder.append("    OS Name: " + System.getProperty("os.name") + "\n")
        builder.append("    OS Version: " + System.getProperty("os.version") + "\n")
        builder.append("    JVM memory available: " + MemoryUtils.maxMemory + " KB\n")

        builder.append("\n")

        builder.append("Graphics Specifications:\n")
        builder.append("    Resolution: " + Gdx.graphics.width + "x" + Gdx.graphics.height + "\n")
        builder.append("    Fullscreen: " + Gdx.graphics.isFullscreen + "\n")
        builder.append("    GL_VENDOR: " + Gdx.gl.glGetString(GL20.GL_VENDOR) + "\n")
        builder.append("    Graphics: " + Gdx.gl.glGetString(GL20.GL_RENDERER) + "\n")
        builder.append("    GL Version: " + Gdx.gl.glGetString(GL20.GL_VERSION) + "\n")
        ps.println(builder.toString())
        ps.println("\n")
        ps.flush()

        thread(isDaemon = true) {
            val builder = StringBuilder()
            // OSHI related
            val systemInfo = OSHI.sysInfo
            val processor = systemInfo.hardware.processor
            builder.append("\n==============\n" +
                                   "AUTO-GENERATED\n" +
                                   "==============\n")
            builder.append("Processor Specifications:\n")
            builder.append("    Name: ${processor.name}\n")
            builder.append("    CPU(s): ${processor.physicalProcessorCount} physical, ${processor.logicalProcessorCount} logical\n")
            builder.append("    Is 64-bit?: ${processor.isCpu64bit}\n")
            builder.append("    Identifier: ${processor.identifier}\n")
            builder.append("\n")
            ps.println(builder.toString())
        }

        newOut = TeeOutputStream(oldOut, stream)
        newErr = TeeOutputStream(oldErr, stream)

        System.setOut(PrintStream(newOut))
        System.setErr(PrintStream(newErr))

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            StreamUtils.closeQuietly(stream)
        })
    }

}
