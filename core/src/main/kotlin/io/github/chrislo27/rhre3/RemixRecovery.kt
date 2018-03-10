package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import kotlin.concurrent.thread


object RemixRecovery {

    private const val RECOVERY_FILE_NAME: String = "recovery.${RHRE3.REMIX_FILE_EXTENSION}"

    @Volatile
    private var addedToShutdownhook: Boolean = false
    val recoveryFolder: FileHandle by lazy { RHRE3.RHRE3_FOLDER.child("recovery/").apply(FileHandle::mkdirs) }
    val recoveryFile: FileHandle by lazy { recoveryFolder.child(RECOVERY_FILE_NAME) }
    private val recoveryPrefs: Preferences by lazy { Gdx.app.getPreferences("RHRE3-recovery") }

    @Synchronized
    fun addSelfToShutdownHooks() {
        if (!addedToShutdownhook) {
            addedToShutdownhook = true
            Runtime.getRuntime().addShutdownHook(thread(start = false, isDaemon = true, block = this::saveRemixInRecovery, name = "Remix Recovery Shutdown Hook"))
        }
    }

    fun saveRemixInRecovery() {
        try {
            val editorScreen: EditorScreen = ScreenRegistry.getAsType("editor") ?: error(
                    "No editor screen when attempting to save a remix recovery")
            val editor = editorScreen.editor
            val remix = editor.remix

            Remix.saveTo(remix, recoveryFile.file(), true)
            recoveryPrefs.putBoolean("wasEditedAfterSave", remix.hasBeenModifiedAfterSave()).flush()

            Toolboks.LOGGER.info("Saved remix recovery file successfully")
        } catch (t: Throwable) {
            Toolboks.LOGGER.warn("Failed to save remix recovery file")
            t.printStackTrace()
        }
    }

    fun canBeRecovered(): Boolean {
        return recoveryFile.exists()
    }

    fun shouldBeRecovered(): Boolean {
        return canBeRecovered() && recoveryPrefs.getBoolean("wasEditedAfterSave", false)
    }

}