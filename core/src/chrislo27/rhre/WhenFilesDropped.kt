package chrislo27.rhre

import com.badlogic.gdx.files.FileHandle
import java.io.File


interface WhenFilesDropped {

	abstract fun onFilesDropped(list: List<FileHandle>)

}