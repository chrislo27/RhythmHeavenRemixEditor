package io.github.chrislo27.rhre3.news

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.rhre3.RHRE3Application
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object ThumbnailFetcher {

    private val httpClient: AsyncHttpClient
        get() = RHRE3Application.httpClient

    val map: ConcurrentMap<String, Texture> = ConcurrentHashMap()
    private val fetching: ConcurrentMap<String, ListenableFuture<Response>> = ConcurrentHashMap()

    @Synchronized
    fun removeAll() {
        map.forEach { _, t ->
            t.dispose()
        }
        map.clear()
    }

    @Synchronized
    fun cancelAll() {
        fetching.keys.forEach(ThumbnailFetcher::cancel)
    }

    @Synchronized
    fun cancel(url: String) {
        if (url in fetching) {
            fetching.remove(url)?.cancel(true)
        }
    }

    @Synchronized
    fun fetch(url: String, callback: (Texture?, Exception?) -> Unit) {
        if (url in map || url in fetching)
            return

        val future = httpClient.prepareGet(url).execute()
        fetching[url] = future
        future.addListener(Runnable {
            fetching.remove(url)
            try {
                val value = future.get()
                if (future.isDone && !future.isCancelled && value.statusCode == 200) {
                    Gdx.app.postRunnable {
                        try {
                            val bytes = value.responseBodyAsBytes
                            val pixmap = Pixmap(bytes, 0, bytes.size)
                            val texture = Texture(pixmap)
                            pixmap.dispose()
                            map[url] = texture
                            try {
                                callback.invoke(texture, null)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            callback(null, e)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null, e)
            }
        }, null)
    }

}