package io.github.chrislo27.rhre3.analytics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Disposable
import com.segment.analytics.Analytics
import com.segment.analytics.messages.IdentifyMessage
import com.segment.analytics.messages.TrackMessage
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.i18n.Localization
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
            }
            return field
        }

    fun initAndIdentify(prefs: Preferences) {
        this.prefs = prefs
        analytics = Analytics.builder(writeKey).flushInterval(5000L, TimeUnit.MILLISECONDS).build()

        identify()
        analytics.flush()
    }

    fun identify() {
        analytics.enqueue(IdentifyMessage.builder()
                                  .userId(userID)
                                  .traits(mapOf(
                                          "createdAt" to prefs.getString(PREFS_USER_CREATED, (System.currentTimeMillis() / 1000L).toString()))
                                         )
                         )
    }

    private fun getContext(): Map<String, *> {
        return mutableMapOf<String, Any>().apply {
            put("app", mapOf("version" to RHRE3.VERSION.toString()))
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