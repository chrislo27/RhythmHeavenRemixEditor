package io.github.chrislo27.rhre3.analytics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Disposable
import com.segment.analytics.Analytics
import com.segment.analytics.messages.IdentifyMessage
import com.segment.analytics.messages.TrackMessage
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.i18n.Localization
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


object AnalyticsHandler : Disposable {

    private val PREFS_USER_ID = "userID"
    private val PREFS_USER_CREATED = "userCreated"

    private val writeKey = "sAAtVfehoB8inZF6oUUwv5HIT0W00wcW"
    private lateinit var analytics: Analytics
    private lateinit var prefs: Preferences
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

    fun initAndIdentify(prefs: Preferences) {
        this.prefs = prefs
        val n = java.util.prefs.Preferences.userRoot().node("io/rhre")
        val reg = n.get("analyticsID", "")
        val fromP = prefs.getString(PREFS_USER_ID, "")
        this.userID = if (reg == fromP) reg else (reg.takeUnless(String::isEmpty) ?: fromP)
        n.put("analyticsID", getUUID())
        analytics = Analytics.builder(writeKey).flushInterval(5000L, TimeUnit.MILLISECONDS).build()

        identify()
        analytics.flush()

        if (RHRE3.noAnalytics) {
            GlobalScope.launch {
                delay(2000L)
                analytics.shutdown()
            }
        }
    }

    fun getUUID(): String {
        return this.userID
    }

    fun identify() {
        analytics.enqueue(IdentifyMessage.builder()
                                  .userId(userID)
                                  .context(getContext())
                                  .traits(mapOf(
                                          "createdAt" to prefs.getString(PREFS_USER_CREATED, (System.currentTimeMillis() / 1000L).toString()),
                                          "analyticsDisabled" to RHRE3.noAnalytics,
                                          "onlineCounterDisabled" to RHRE3.noOnlineCounter
                                               )
                                         )
                         )
    }

    private fun getContext(): Map<String, *> {
        return mutableMapOf<String, Any>().apply {
            put("app", mapOf("version" to RHRE3.VERSION.toString(), "build" to System.getProperty("java.version")?.trim()))
            put("locale", Localization.currentBundle.locale.toString())
            put("os", mapOf("name" to System.getProperty("os.name"),
                            "version" to System.getProperty("os.version")))
            put("timezone", TimeZone.getDefault().id)
            put("screen", mapOf("density" to Gdx.graphics.density,
                                "width" to Gdx.graphics.width,
                                "height" to Gdx.graphics.height))
        }
    }

    fun track(event: String, properties: Map<String, Any>) {
        analytics.enqueue(TrackMessage.builder(event)
                                  .userId(userID)
                                  .context(getContext())
                                  .properties(properties))
    }

    override fun dispose() {
        if (this::analytics.isInitialized) {
            analytics.flush()
            analytics.shutdown()
        }
    }

}