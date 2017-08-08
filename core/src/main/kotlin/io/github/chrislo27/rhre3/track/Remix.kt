package io.github.chrislo27.rhre3.track

import chrislo27.rhre.oopsies.ActionHistory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Cue
import io.github.chrislo27.rhre3.tempo.Tempos
import io.github.chrislo27.rhre3.theme.DarkTheme
import io.github.chrislo27.rhre3.util.JsonHandler


class Remix(val camera: OrthographicCamera, val editor: Editor) : ActionHistory<Remix>() {

    val main: RHRE3Application
        get() = editor.main
    val entities: MutableList<Entity> = mutableListOf()
    val tempos: Tempos = Tempos()

    var seconds: Float = 0f
        set(value) {
            field = value
            beat = tempos.secondsToBeats(field)
        }
    var beat: Float = 0f
        private set

    init {
    }

    fun render(batch: SpriteBatch) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            entities +=
                    CueEntity(this, GameRegistry.data.objectMap["lockstep/hai"] as Cue)

            println(JsonHandler.toJson(DarkTheme()))
        }

        entities.forEach {
            it.render(batch)
        }

    }

}