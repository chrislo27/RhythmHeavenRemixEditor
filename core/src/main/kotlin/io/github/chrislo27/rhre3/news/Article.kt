package io.github.chrislo27.rhre3.news

import java.time.Instant
import java.time.LocalDate


class Article(val id: String, val title: String, val body: String,
              val thumbnail: String, val publishedAt: Long,
              val url: String?, val images: List<String>) {

    companion object {
        val BLANK = Article("---", "", "", "", 0L, null, listOf())
    }

    val publishedDate: LocalDate by lazy { LocalDate.from(Instant.ofEpochMilli(publishedAt)) }

}
