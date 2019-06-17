package io.github.chrislo27.rhre3.patternstorage

import com.badlogic.gdx.Gdx
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.PickerName
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.CuePointer
import io.github.chrislo27.rhre3.screen.PatternStoreScreen
import io.github.chrislo27.rhre3.track.Remix
import java.util.*


interface StoredPattern {
    val uuid: UUID
    val name: String
    val data: String
    val datamodel: Datamodel
}

object ClipboardStoredPattern : StoredPattern {
    override val uuid: UUID = UUID.randomUUID()
    override val name: String = "[LIGHT_GRAY](Clipboard)[]"
    override val data: String
        get() = Gdx.app.clipboard.contents
    override val datamodel: Datamodel by lazy {
        object : Datamodel(SFXDatabase.data.specialGame, uuid.toString(), listOf(), name) {
            override val pickerName: PickerName = PickerName(name, "Drag this to paste the clipboard")

            override fun createEntity(remix: Remix, cuePointer: CuePointer?): ModelEntity<*> {
                error("NO-OP")
            }

            override fun dispose() {
            }
        }
    }

}

data class FileStoredPattern(override val uuid: UUID, override val name: String, override val data: String)
    : StoredPattern {

    @Transient
    var filename: String? = null

    @delegate:Transient
    override val datamodel: Datamodel by lazy {
        object : Datamodel(SFXDatabase.data.specialGame, uuid.toString(), listOf(), name) {
            override fun createEntity(remix: Remix, cuePointer: CuePointer?): ModelEntity<*> {
                error("NO-OP")
            }

            override fun dispose() {
            }
        }
    }

}

fun StoredPattern.toEntityList(remix: Remix): List<Entity> {
    return PatternStoreScreen.jsonToEntities(remix, this.data)
}