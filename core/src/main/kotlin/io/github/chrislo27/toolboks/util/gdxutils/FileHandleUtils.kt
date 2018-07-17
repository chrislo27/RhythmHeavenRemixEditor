package io.github.chrislo27.toolboks.util.gdxutils

import com.badlogic.gdx.files.FileHandle


fun FileHandle.copyHandle(): FileHandle =
        FileHandle(this.file())
