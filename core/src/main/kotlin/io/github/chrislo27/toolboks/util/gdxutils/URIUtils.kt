package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.Net
import com.badlogic.gdx.files.FileHandle
import java.awt.Desktop
import java.io.File
import java.net.URI


fun Net.openFileExplorer(absPath: String): Boolean {
    return try {
        Desktop.getDesktop().browse(URI("file:///${absPath.replace("\\", "/")}"))
        true
    } catch (t: Throwable) {
        t.printStackTrace()
        false
    }
}

fun Net.openFileExplorer(file: File): Boolean = openFileExplorer(file.absolutePath)

fun Net.openFileExplorer(fileHandle: FileHandle): Boolean = openFileExplorer(fileHandle.file())
