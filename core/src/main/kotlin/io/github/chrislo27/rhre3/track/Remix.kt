package io.github.chrislo27.rhre3.track

import chrislo27.rhre.oopsies.ActionHistory
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.entity.Entity


class Remix(val camera: OrthographicCamera) : ActionHistory<Remix>() {

    val entities: MutableList<Entity> = mutableListOf()

    fun render(batch: SpriteBatch) {


    }

}