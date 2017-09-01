package io.github.chrislo27.rhre3

import io.github.chrislo27.toolboks.version.Version


object VersionHistory {

    val previousVersions: List<Version> by lazy {
        listOf(
                Version(3, 0, 0),
                Version(3, 0, 1),
                Version(3, 0, 2),
                Version(3, 0, 3),
                Version(3, 0, 4)
              )
    }

    val CUSTOM_SOUNDS_GET_PREFIXES: Version by lazy { Version(3, 1 ,0) }

}