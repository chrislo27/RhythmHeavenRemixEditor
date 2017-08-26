package io.github.chrislo27.rhre3.util

import java.time.LocalDateTime


class ReleaseObject {

    var html_url: String? = null
    var tag_name: String? = null
    var id: Int = -1
    var name: String? = null
    var body: String? = null
    var published_at: String? = null
    var assets: List<AssetObject>? = null

    @Transient
    lateinit var bodyLines: List<String>

    @Transient
    var publishedTime: LocalDateTime? = null

    class AssetObject {

        var browser_download_url: String? = null
        var name: String? = null
        var size: Long = -1
        var download_count: Long = -1

    }

}