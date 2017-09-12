package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.rhre3.track.music.MusicVolumeChange
import io.github.chrislo27.rhre3.track.tempo.TempoChange
import io.github.chrislo27.rhre3.track.timesignature.TimeSignature
import io.github.chrislo27.rhre3.tracker.Tracker
import io.github.chrislo27.toolboks.registry.AssetRegistry
import kotlin.reflect.KClass


enum class Tool(val texId: String, val nameId: String, val trackerClass: KClass<out Tracker>? = null, val canSelect: Boolean = false) {

    SELECTION("tool_selection", "tool.normal.name", canSelect = true),
    MULTIPART_SPLIT("tool_multipart_split", "tool.multipartsplit.name"),
    BPM("tool_bpm", "tool.bpm.name", trackerClass = TempoChange::class),
    MUSIC_VOLUME("tool_music_volume", "tool.musicvolume.name", trackerClass = MusicVolumeChange::class),
    TIME_SIGNATURE("tool_time_signature", "tool.timesignature.name", trackerClass = TimeSignature::class),
    GAME_BOUNDARIES("weird_wakame", "tool.gameBoundaries.name", canSelect = true);

    companion object {
        val VALUES: List<Tool> by lazy { Tool.values().toList() }
    }

    val isTrackerRelated: Boolean = trackerClass != null

    val texture: Texture
        get() = AssetRegistry[texId]

}