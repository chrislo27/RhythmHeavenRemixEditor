package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.StreamUtils
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.zip.ZipFile
import kotlin.concurrent.thread


object RemixRecovery {

    private const val RECOVERY_FILE_NAME: String = "recovery.${RHRE3.REMIX_FILE_EXTENSION}"
    private const val LAST_LOADED_FILE = "lastLoadedFile.${RHRE3.REMIX_FILE_EXTENSION}"

    @Volatile
    private var addedToShutdownhook: Boolean = false
    val recoveryFolder: FileHandle by lazy { RHRE3.RHRE3_FOLDER.child("recovery/").apply(FileHandle::mkdirs) }
    val recoveryFile: FileHandle by lazy { recoveryFolder.child(RECOVERY_FILE_NAME) }
    private val lastLoadedFile: FileHandle by lazy { recoveryFolder.child(LAST_LOADED_FILE) }
    private val recoveryPrefs: Preferences by lazy { Gdx.app.getPreferences("RHRE3-recovery") }
    private val messageDigest = MessageDigest.getInstance("SHA-1")
    private var lastChecksum: String = ""

    @Synchronized
    fun addSelfToShutdownHooks() {
        if (!addedToShutdownhook) {
            addedToShutdownhook = true
            Runtime.getRuntime().addShutdownHook(
                    thread(start = false, isDaemon = true, block = this::saveRemixInRecovery,
                           name = "Remix Recovery Shutdown Hook"))
        }
    }

    private fun getChecksumOfZip(fileHandle: FileHandle): String {
        messageDigest.reset()
        try {
            val zipFile: ZipFile = ZipFile(fileHandle.file())
            zipFile.entries().iterator().forEachRemaining {
                if (!it.isDirectory) {
                    zipFile.getInputStream(it).buffered(2048).also {
                        val array = ByteArray(2048)
                        var amt = it.read(array)
                        while (amt > -1) {
                            messageDigest.update(array, 0, amt)
                            amt = it.read(array)
                        }
                        StreamUtils.closeQuietly(it)
                    }
                }
            }

            StreamUtils.closeQuietly(zipFile)
        } catch (t: Throwable) {
        }

        // https://www.samclarke.com/kotlin-hash-strings/
        val hexChars = "0123456789abcdef"
        val bytes = messageDigest.digest()
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }

        return result.toString()
    }

    fun cacheChecksumAfterLoad(remix: Remix) {
        Remix.saveTo(remix, lastLoadedFile.file(), true)
        cacheChecksum(lastLoadedFile)
    }

    fun cacheChecksum(fileHandle: FileHandle) {
        lastChecksum = getChecksumOfZip(fileHandle)
    }

    fun saveRemixInRecovery() {
        if (lastChecksum.isEmpty()) {
            Toolboks.LOGGER.info("Skipping saving recovery remix because last checksum is empty")
            return
        }

        try {
            val editorScreen: EditorScreen = ScreenRegistry.getAsType("editor") ?: error(
                    "No editor screen when attempting to save a remix recovery")
            val editor = editorScreen.editor
            val remix = editor.remix

            Remix.saveTo(remix, recoveryFile.file(), true)
            val recoveryChecksum = getChecksumOfZip(recoveryFile)
            recoveryPrefs.putString("lastSavedChecksum", lastChecksum)
                    .putString("recoveryChecksum", recoveryChecksum)
                    .putLong("time", System.currentTimeMillis())
                    .flush()

            Toolboks.LOGGER.info("Saved remix recovery file successfully")
        } catch (t: Throwable) {
            Toolboks.LOGGER.warn("Failed to save remix recovery file")
            t.printStackTrace()
        }
    }

    fun getLastTime(): Long {
        return recoveryPrefs.getLong("time", -1L)
    }

    fun getLastLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getLastTime()), ZoneId.systemDefault())
    }

    fun canBeRecovered(): Boolean {
        return recoveryFile.exists()
    }

    fun shouldBeRecovered(): Boolean {
        return canBeRecovered() && recoveryPrefs.getString("lastSavedChecksum", "") != recoveryPrefs.getString(
                "recoveryChecksum", "")
    }

}