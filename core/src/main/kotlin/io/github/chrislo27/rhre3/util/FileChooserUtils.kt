package io.github.chrislo27.rhre3.util

import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.Toolboks
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import java.awt.Component
import java.io.File
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

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

/**
 * @param extensions *.name
 */
data class FileChooserExtensionFilter(val name: String, val extensions: List<String>) {

    constructor(name: String, vararg extensions: String) : this(name, listOf(*extensions))

}

object FileChooser {

    enum class UIMode {
        UNKNOWN, JAVAFX, SWING
    }

    @Volatile
    var uiMode: UIMode = UIMode.UNKNOWN
        private set

    private var firstFailure = true

    @Synchronized
    private fun determineUIMode() {
        if (uiMode == UIMode.UNKNOWN) {
            // Test for JavaFX first
            try {
                Class.forName("javafx.stage.FileChooser")
                uiMode = UIMode.JAVAFX
                Toolboks.LOGGER.info("[FileChooser] Set UIMode to JavaFX")
                // Load the Platform (pre-J9)
                JFXPanel()
                return
            } catch (e: ClassNotFoundException) {
                // Failed. Try the next one
            }

            // Test for Swing
            try {
                Class.forName("javax.swing.JFileChooser")
                uiMode = UIMode.SWING
                Toolboks.LOGGER.info("[FileChooser] Set UIMode to Swing")
                // Set Look & Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                return
            } catch (e: ClassNotFoundException) {
                // Failed.
            }

            if (firstFailure) {
                firstFailure = false
                Toolboks.LOGGER.warn("[FileChooser] No suitable UI toolkit found!")
            }
        }
    }

    private fun createJavaFXFileChooser(title: String, initialDirectory: File?, initialFile: File?, extensionFilters: List<FileChooserExtensionFilter>, selectedFilter: FileChooserExtensionFilter?): javafx.stage.FileChooser {
        return javafx.stage.FileChooser().apply {
            val mapped = extensionFilters.associate {
                it to javafx.stage.FileChooser.ExtensionFilter(it.name, it.extensions)
            }
            this.extensionFilters.addAll(mapped.values)
            if (selectedFilter != null) {
                this.selectedExtensionFilter = mapped[selectedFilter]
            }

            this.title = title
            this.initialDirectory = initialDirectory
            this.initialFileName = initialFile?.name
        }
    }

    private fun createSwingFileChooser(title: String, initialDirectory: File?, initialFile: File?, extensionFilters: List<FileChooserExtensionFilter>, selectedFilter: FileChooserExtensionFilter?, allowMultipleSelection: Boolean): JFileChooser {
        return object : JFileChooser(){
            override fun createDialog(parent: Component?): JDialog {
                return super.createDialog(parent).also { dia ->
                    dia.isAlwaysOnTop = true
                }
            }
        }.apply {
            val mapped = extensionFilters.associate { filter ->
                val ext = filter.extensions.map { it.substringAfter(".") }.toTypedArray()
                val shouldPutExtsInName = !filter.name.trim().endsWith(")")
                filter to FileNameExtensionFilter(filter.name + if (shouldPutExtsInName) " (${ext.joinToString { "*.$it" }})" else "", *ext)
            }
            mapped.values.forEach(this::addChoosableFileFilter)
            if (selectedFilter != null) {
                this.fileFilter = mapped[selectedFilter]
            }

            this.dialogTitle = title
            this.currentDirectory = initialDirectory
            this.selectedFile = initialFile

            this.isMultiSelectionEnabled = allowMultipleSelection
        }
    }

    fun openFileChooser(title: String, initialDirectory: File? = null, initialFile: File? = null, extensionFilters: List<FileChooserExtensionFilter> = listOf(), selectedFilter: FileChooserExtensionFilter? = null, function: (File?) -> Unit) {
        determineUIMode()
        return when (uiMode) {
            FileChooser.UIMode.UNKNOWN -> function(null)
            FileChooser.UIMode.JAVAFX -> {
                Platform.runLater {
                    function(createJavaFXFileChooser(title, initialDirectory, initialFile, extensionFilters, selectedFilter).showOpenDialog(null))
                }
            }
            FileChooser.UIMode.SWING -> {
                SwingUtilities.invokeLater {
                    val chooser = createSwingFileChooser(title, initialDirectory, initialFile, extensionFilters, selectedFilter, false)
                    val status = chooser.showOpenDialog(null)
                    val file = if (status == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
                    function(file)
                }
            }
        }
    }

    fun saveFileChooser(title: String, initialDirectory: File? = null, initialFile: File? = null, extensionFilters: List<FileChooserExtensionFilter> = listOf(), selectedFilter: FileChooserExtensionFilter? = null, function: (File?) -> Unit) {
        determineUIMode()
        return when (uiMode) {
            FileChooser.UIMode.UNKNOWN -> function(null)
            FileChooser.UIMode.JAVAFX -> {
                Platform.runLater {
                    function(createJavaFXFileChooser(title, initialDirectory, initialFile, extensionFilters, selectedFilter).showSaveDialog(null))
                }
            }
            FileChooser.UIMode.SWING -> {
                SwingUtilities.invokeLater {
                    val chooser = createSwingFileChooser(title, initialDirectory, initialFile, extensionFilters, selectedFilter, false)
                    val status = chooser.showSaveDialog(null)
                    val file = if (status == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
                    function(file)
                }
            }
        }
    }

    fun openMultipleFileChooser(title: String, initialDirectory: File? = null, initialFile: File? = null, extensionFilters: List<FileChooserExtensionFilter> = listOf(), selectedFilter: FileChooserExtensionFilter? = null, function: (List<File>) -> Unit) {
        determineUIMode()
        return when (uiMode) {
            FileChooser.UIMode.UNKNOWN -> function(listOf())
            FileChooser.UIMode.JAVAFX -> {
                Platform.runLater {
                    function(createJavaFXFileChooser(title, initialDirectory, initialFile, extensionFilters, selectedFilter).showOpenMultipleDialog(null) ?: listOf())
                }
            }
            FileChooser.UIMode.SWING -> {
                SwingUtilities.invokeLater {
                    val chooser = createSwingFileChooser(title, initialDirectory, initialFile, extensionFilters, selectedFilter, true)
                    val status = chooser.showOpenDialog(null)
                    val files = if (status == JFileChooser.APPROVE_OPTION) chooser.selectedFiles?.toList() else null
                    function(files ?: listOf())
                }
            }
        }
    }

}
