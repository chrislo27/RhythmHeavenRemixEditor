package io.github.chrislo27.rhre3.modding

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.registry.GameRegistry


class ModdingMetadata(private val data: GameRegistry.RegistryData,
                      private val sourceFolder: FileHandle, private val customFolder: FileHandle) {

    init {

    }

}
