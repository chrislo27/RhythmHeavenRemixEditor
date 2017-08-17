package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.toolboks.registry.AssetRegistry


enum class Tool(val texId: String, val nameId: String, val isTrackerRelated: Boolean = false) {

    NORMAL("tool_normal", "tool.normal.name"),
    MULTIPART_SPLIT("tool_multipart_split", "tool.multipartsplit.name"),
    BPM("tool_bpm", "tool.bpm.name", true),
    TIME_SIGNATURE("tool_time_signature", "tool.timesignature.name", true),
    MUSIC_VOLUME("tool_music_volume", "tool.musicvolume.name", true);

    companion object {
        val VALUES: List<Tool> by lazy { Tool.values().toList() }
    }

    val texture: Texture
        get() = AssetRegistry[texId]

}