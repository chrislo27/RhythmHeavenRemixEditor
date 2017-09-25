package io.github.chrislo27.rhre3.util

import io.github.chrislo27.rhre3.RHRE3Application
import java.io.File

private val userHomeFile: File = File(System.getProperty("user.home"))
private val desktopFile: File = userHomeFile.resolve("Desktop")

internal fun persistDirectory(main: RHRE3Application, prefName: String, file: File) {
    main.preferences.putString(prefName, file.absolutePath)
    main.preferences.flush()
}

internal fun attemptRememberDirectory(main: RHRE3Application, prefName: String): File? {
    val f: File = File(main.preferences.getString(prefName, null) ?: return null)

    if (f.exists() && f.isDirectory)
        return f

    return null
}

internal fun getDefaultDirectory(): File =
        if (!desktopFile.exists() || !desktopFile.isDirectory)
            userHomeFile
        else
            desktopFile
