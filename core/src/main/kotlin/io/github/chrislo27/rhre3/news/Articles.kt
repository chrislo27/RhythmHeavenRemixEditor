package io.github.chrislo27.rhre3.news

import com.badlogic.gdx.Preferences
import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import kotlinx.coroutines.experimental.launch
import org.asynchttpclient.AsyncHttpClient
import java.util.concurrent.CopyOnWriteArrayList


object Articles {

    private const val FETCH_URL: String = "https://zorldo.auroranet.me:10443/articles"
    private val httpClient: AsyncHttpClient
        get() = RHRE3Application.httpClient

    enum class FetchState {
        FETCHING, DONE, ERROR
    }

    @Volatile
    var isFetching: FetchState = FetchState.DONE
        private set(value) {
            val old = field
            field = value

            if (old != value) {
                fetchStateListeners.forEach { it(old, value) }
            }
        }
    val articles: CopyOnWriteArrayList<Article> = CopyOnWriteArrayList()
    val fetchStateListeners: MutableList<(old: FetchState, new: FetchState) -> Unit> = mutableListOf()

    fun isArticleViewed(article: Article, preferences: Preferences): Boolean {
        return preferences.getBoolean(article.id, false)
    }

    fun setArticleViewed(article: Article, preferences: Preferences, viewed: Boolean) {
        preferences.putBoolean(article.id, viewed).flush()
    }

    fun fetch() {
        if (isFetching == FetchState.FETCHING)
            return

        isFetching = FetchState.FETCHING
        launch {
            try {
                articles.clear()
                val list = mutableListOf<Article>()
                val req = httpClient.prepareGet(FETCH_URL)
                        .addHeader("User-Agent", "RHRE ${RHRE3.VERSION}")
                        .addHeader("X-Analytics-ID", AnalyticsHandler.getUUID())
                val response = req.execute().get()

                if (response.statusCode == 200) {
                    val body = response.responseBody
                    val articlesJson = JsonHandler.OBJECT_MAPPER.readTree(body) as ArrayNode
                    articlesJson.forEach { articleJson ->
                        try {
                            val article = JsonHandler.OBJECT_MAPPER.treeToValue(articleJson, Article::class.java)
                            if (!article.experimental || (RHRE3.EXPERIMENTAL && !Toolboks.debugMode)) {
                                list += article
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    error("Status code received is not 200: ${response.statusCode} ${response.statusText}")
                }

                articles.addAll(list)
                isFetching = FetchState.DONE
            } catch (e: Exception) {
                e.printStackTrace()
                isFetching = FetchState.ERROR
            }
        }
    }

}