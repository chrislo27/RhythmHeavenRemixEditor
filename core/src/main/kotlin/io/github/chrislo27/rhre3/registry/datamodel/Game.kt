package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.version.Version


data class Game(val id: String, val name: String, val requiresVersion: Version, val objects: List<Datamodel>,
                val icon: Texture) : Disposable {

    override fun dispose() {
        objects.forEach(Disposable::dispose)
    }
}