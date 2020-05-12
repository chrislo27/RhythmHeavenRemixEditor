package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.graphics.Color
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memAddress
import org.lwjgl.util.tinyfd.TinyFileDialogs.*
import java.io.File
import java.nio.ByteBuffer


object TinyFDWrapper {

    data class FileExtFilter(val description: String, val extensions: List<String>) {
        constructor(desc: String, vararg extensions: String) : this(desc, listOf(*extensions))
    }
    
    private fun File?.toProperPath(): String? {
        if (this == null) return null
        if (this.isDirectory) return this.absolutePath + "/"
        return this.absolutePath
    }

    private fun openFile(title: String, defaultFile: String?, filter: FileExtFilter?): File? {
        return if (filter == null) {
            val path = tinyfd_openFileDialog(title, defaultFile, null, null, false) ?: return null
            File(path)
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(memAddress(stack.UTF8(it)))
                }
                filterPatterns.flip()

                val path = tinyfd_openFileDialog(title, defaultFile, filterPatterns, filter.description, false)
                        ?: return null
                File(path)
            }
        }
    }
    
    fun openFile(title: String, defaultFile: File?, filter: FileExtFilter?, function: (File?) -> Unit) {
        openFile(title, defaultFile.toProperPath(), filter).let(function)
    }

    private fun openMultipleFiles(title: String, defaultFile: String?, filter: FileExtFilter?): List<File>? {
        return if (filter == null) {
            val path = tinyfd_openFileDialog(title, defaultFile, null, null, true) ?: return null
            path.split('|').map { File(it) }
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(memAddress(stack.UTF8(it)))
                }
                filterPatterns.flip()

                val path = tinyfd_openFileDialog(title, defaultFile, filterPatterns, filter.description, true)
                        ?: return null
                path.split('|').map { File(it) }
            }
        }
    }

    fun openMultipleFiles(title: String, defaultFile: File?, filter: FileExtFilter?, function: (List<File>?) -> Unit) {
        openMultipleFiles(title, defaultFile.toProperPath(), filter).let(function)
    }

    private fun saveFile(title: String, defaultFile: String?, filter: FileExtFilter?): File? {
        return if (filter == null) {
            val path = tinyfd_saveFileDialog(title, defaultFile, null, null) ?: return null
            File(path)
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(memAddress(stack.UTF8(it)))
                }
                filterPatterns.flip()

                val path = tinyfd_saveFileDialog(title, defaultFile, filterPatterns, filter.description) ?: return null
                File(path)
            }
        }
    }

    fun saveFile(title: String, defaultFile: File?, filter: FileExtFilter?, function: (File?) -> Unit) {
        saveFile(title, defaultFile.toProperPath(), filter).let(function)
    }

    private fun selectFolder(title: String, defaultFolder: String): File? {
        val path = tinyfd_selectFolderDialog(title, defaultFolder) ?: return null
        return File(path)
    }

    fun selectFolder(title: String, defaultFolder: File, function: (File?) -> Unit) {
        selectFolder(title, defaultFolder.toProperPath()!!).let(function)
    }

    fun selectColor(title: String, defaultHexColor: String): Color? {
        val stack: MemoryStack = MemoryStack.stackPush()
        stack.use {
            val color: ByteBuffer = stack.malloc(3)
            val hex: String? = tinyfd_colorChooser(title, defaultHexColor, null, color) ?: return null
            return Color.valueOf(hex).apply { a = 1f }
        }
    }

    fun selectColor(title: String, defaultColor: Color?): Color? {
        val stack: MemoryStack = MemoryStack.stackPush()
        stack.use {
            val color: ByteBuffer = stack.malloc(3)
            val def = if (defaultColor == null) "#FFFFFF" else "#${defaultColor.toString().take(6)}"
            val hex: String? = tinyfd_colorChooser(title, def, null, color) ?: return null
            return Color.valueOf(hex).apply { a = 1f }
        }
    }

}
