package io.github.chrislo27.rhre3.track

import chrislo27.rhre.oopsies.ActionHistory
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.tempo.Tempos


class Remix(val camera: OrthographicCamera) : ActionHistory<Remix>() {

    val entities: MutableList<Entity> = mutableListOf()
    val tempos: Tempos = Tempos()

    var seconds: Float = 0f
        set(value) {
            field = value
            beat = tempos.secondsToBeats(field)
        }
    var beat: Float = 0f
        private set

    fun render(batch: SpriteBatch) {


    }

}