package io.github.chrislo27.rhre3.lc

import com.badlogic.gdx.Gdx
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.prefs.Preferences
import kotlin.concurrent.thread


class LC(val main: RHRE3Application) {

    init {
        GlobalScope.launch {
            val source1 = main.preferences.getString("l", "") ?: ""
            val prefs = Preferences.userRoot().node("io/rhre")
            val source2 = prefs.get("l", "") ?: ""

            var source = if (source1 == source2) source1 else (if (source1.isEmpty() && source2.isNotEmpty()) source2 else if (source1.isNotEmpty() && source2.isEmpty()) source1 else "")

            try {
                val req = RHRE3Application.httpClient.prepareGet("https://zorldo.auroranet.me:10443/rhre3/lc")
                        .addHeader("User-Agent", "RHRE ${RHRE3.VERSION}")
                        .addHeader("X-Analytics-ID", AnalyticsHandler.getUUID())
                        .execute().get()

                if (req.statusCode == 200) {
                    source = req.responseBody
                } else if (req.statusCode == 204) {
                    source = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            fun persist() {
                try {
                    main.preferences.putString("l", source).flush()
                    prefs.put("l", source)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            persist()
            Runtime.getRuntime().addShutdownHook(thread(start = false, block = ::persist))

            if (source.isNotEmpty()) {
                val reason = source
                Gdx.app.postRunnable {
                    main.screen = LCScreen(main, reason)
                }
            }
        }
    }

}