package io.github.chrislo27.rhre3.news

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.concurrent.thread


object ThumbnailFetcher : Disposable {

    private val httpClient: AsyncHttpClient
        get() = RHRE3Application.httpClient

    val thumbnailFolder: FileHandle by lazy { RHRE3.RHRE3_FOLDER.child("thumbnails/").apply(FileHandle::mkdirs) }
    val map: ConcurrentMap<String, Texture> = ConcurrentHashMap()
    private val fetching: ConcurrentMap<String, ListenableFuture<Response>> = ConcurrentHashMap()

    init {
        Runtime.getRuntime().addShutdownHook(thread(start = false, isDaemon = true) {
            thumbnailFolder.list(".png")
                    .filter { (System.currentTimeMillis() - it.lastModified()) / (1000 * 60 * 60 * 24) > 7 }
                    .forEach { it.delete() }
        })
    }

    @Synchronized
    override fun dispose() {
        cancelAll()
        removeAll()
    }

    @Synchronized
    fun disposeOf(texture: Texture) {
        val found = texture in map.values
        if (found) {
            map.entries.filter { it.value == texture }.forEach {
                map.remove(it.key, it.value)
            }
            texture.dispose()
        }
    }

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

        val urlHash: String = MessageDigest.getInstance("SHA-1").let {
            it.update(url.toByteArray())
            BigInteger(1, it.digest()).toString(16)
        }

        val cachedFile: FileHandle = thumbnailFolder.let {
            it.mkdirs()
            it.child("$urlHash.png")
        }

        fun doFetch() {
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
                                try {
                                    // Cache to file
                                    PixmapIO.writePNG(cachedFile, pixmap)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
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

        if (cachedFile.exists()) {
            Gdx.app.postRunnable {
                try {
                    val bytes = cachedFile.readBytes()
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
                    cachedFile.delete()
                    map.remove(url)
                    doFetch()
                }
            }
        } else {
            doFetch()
        }
    }

}