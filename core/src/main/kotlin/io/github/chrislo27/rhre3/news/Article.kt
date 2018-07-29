package io.github.chrislo27.rhre3.news

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class Article(val id: String, val title: String, val body: String,
              val thumbnail: String, val publishedAt: Long,
              val url: String?, val urlTitle: String?, val experimental: Boolean) {

    companion object {
        val BLANK = Article("---", "", "", "", 0L, null, null, false)
    }

    val publishedDate: LocalDate by lazy(Instant.ofEpochMilli(publishedAt).atZone(ZoneId.systemDefault())::toLocalDate)
    @Transient var genuine: Boolean = false

}
