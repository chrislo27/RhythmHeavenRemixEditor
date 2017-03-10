package chrislo27.rhre

import com.badlogic.gdx.files.FileHandle


interface WhenFilesDropped {

	fun onFilesDropped(list: List<FileHandle>)

}