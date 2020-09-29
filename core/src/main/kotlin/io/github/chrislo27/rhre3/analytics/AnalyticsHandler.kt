package io.github.chrislo27.rhre3.analytics

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.i18n.Localization
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Response
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.*
import kotlin.concurrent.thread


object AnalyticsHandler : Disposable {

    private val PREFS_USER_ID = "userID"
    private val PREFS_USER_CREATED = "userCreated"
    
    private val apiKey: String = "64c9f7af3680d665a2b62e6f1e4007f8"
    private val http: AsyncHttpClient
        get() = RHRE3Application.httpClient
    private val objectMapper: ObjectMapper = JsonHandler.createObjectMapper(false, false)
    private val sessionID: Long = System.currentTimeMillis()
    private val eventQueue: BlockingQueue<Event> = LinkedBlockingQueue()
    private val flushScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1, ThreadFactory { Thread(it).apply { isDaemon = true } })
    private lateinit var prefs: Preferences
    private var isInitialized = false
    private var isShutdown = false
    private var userID: String = ""
        get() {
            if (field.isEmpty()) {
                field = UUID.randomUUID().toString()
                prefs.putString(PREFS_USER_ID, field)
                        .putString(PREFS_USER_CREATED, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now()))
                        .flush()
                java.util.prefs.Preferences.userRoot().node("io/rhre").put("analyticsID", field)
            }
            return field
        }

    @Synchronized
    fun initAndIdentify(prefs: Preferences) {
        if (isInitialized) return
        this.prefs = prefs
        val n = java.util.prefs.Preferences.userRoot().node("io/rhre")
        val reg = n.get("analyticsID", "")
        val fromP = prefs.getString(PREFS_USER_ID, "")
        this.userID = if (reg == fromP) reg else (reg.takeUnless(String::isEmpty) ?: fromP)
        n.put("analyticsID", getUUID())

        identify()
        flush()

        flushScheduler.scheduleAtFixedRate({ flush() }, 5000L, 5000L, TimeUnit.MILLISECONDS)

        if (RHRE3.noAnalytics) {
            GlobalScope.launch {
                delay(2000L)
                dispose()
            }
        }
        isInitialized = true
    }

    fun getUUID(): String {
        return this.userID
    }

    fun identify() {
        if (isShutdown) return
        eventQueue.offer(Event.Identify())
    }

    fun track(event: String, properties: Map<String, Any?>) {
        if (isShutdown) return
        eventQueue.offer(Event.Track(event, properties))
    }

    private fun preparePost(endpoint: String): BoundRequestBuilder {
        return http.preparePost("https://api.amplitude.com/$endpoint")
                .addHeader("Accept-Encoding", "identity")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "rhre.dev")
    }

    private fun createBaseJsonPayload(): ObjectNode {
        return objectMapper.createObjectNode().apply {
            val osName = System.getProperty("os.name")
            put("app_version", RHRE3.VERSION.toString())
            put("os_name", osName)
            put("os_version", System.getProperty("os.version"))
            put("platform", osName.toLowerCase(Locale.ROOT))
            put("time", System.currentTimeMillis())
            put("user_id", getUUID())
        }
    }

    // The identification object for the /identify body
    private fun createIdentifyObjPayload(): ObjectNode {
        return createBaseJsonPayload().apply {
            put("user_id", getUUID())
            set<ObjectNode>("user_properties", objectNode().apply {
                set<ObjectNode>("\$set", objectMapper.valueToTree<ObjectNode>(mapOf(
                        "createdAt" to prefs.getString(PREFS_USER_CREATED, (System.currentTimeMillis() / 1000L).toString()),
                        "analyticsDisabled" to RHRE3.noAnalytics,
                        "onlineCounterDisabled" to RHRE3.noOnlineCounter,
                        "language" to Localization.currentBundle.locale.toString()
                                                                                   )))
            })
        }
    }

    // One object in the events array for the /batch body
    private fun createEventObjPayload(event: String, eventProps: Map<String, Any?>): ObjectNode {
        return createBaseJsonPayload().apply {
            put("user_id", getUUID())
            put("session_id", sessionID)
            put("event_type", event)
            set<ObjectNode>("event_properties", objectMapper.valueToTree(eventProps))
        }
    }

    override fun dispose() {
        if (isShutdown) return
        if (isInitialized) {
            flush()
            isShutdown = true
        }
    }

    fun flush() {
        if (isInitialized && eventQueue.size > 0 && !isShutdown) {
            val evts = eventQueue.toList()
            val identifys = evts.filterIsInstance<Event.Identify>()
            val tracks = evts.filterIsInstance<Event.Track>()
            eventQueue.removeAll(evts)

            if (identifys.isNotEmpty()) {
                try {
                    preparePost("identify")
                            .setHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addFormParam("api_key", apiKey)
                            .addFormParam("identification", objectMapper.writeValueAsString(createIdentifyObjPayload()))
                            .execute().toCompletableFuture()
                            .whenComplete { response: Response?, throwable: Throwable? ->
                                if (response != null) {
                                    if (response.statusCode == 429 || response.statusCode >= 500) {
                                        // Retry after 15 sec
                                        GlobalScope.launch {
                                            delay(15000L)
                                            identify()
                                        }
                                    } /*else println("Identify sent w/ ${response.statusCode}: ${response.responseBody}")*/
                                } else {
                                    throwable?.printStackTrace()
                                }
                            }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            tracks.chunked(5).forEach { chunk ->
                val obj = objectMapper.createObjectNode().apply {
                    put("api_key", apiKey)
                    set<ArrayNode>("events", arrayNode().apply {
                        chunk.forEach { e ->
                            add(createEventObjPayload(e.event, e.properties))
                        }
                    })
                }
                val body = objectMapper.writeValueAsString(obj)
                try {
                    preparePost("2/httpapi")
                            .setBody(body)
                            .execute().toCompletableFuture()
                            .whenComplete { response: Response?, throwable: Throwable? ->
                                if (response != null) {
                                    if (response.statusCode == 429 || response.statusCode >= 500) {
                                        // Retry after 30 sec
                                        GlobalScope.launch {
                                            delay(30_000L)
                                            chunk.forEach { e ->
                                                eventQueue.offer(e)
                                                delay(1_500L)
                                            }
                                        }
                                    } /*else println("Events ${chunk.map { it.event }} sent w/ ${response.statusCode}: ${response.responseBody}")*/
                                } else {
                                    throwable?.printStackTrace()
                                }
                            }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private sealed class Event {
        class Identify : Event()
        class Track(val event: String, val properties: Map<String, Any?>) : Event()
    }

}