package io.github.chrislo27.rhre3.lc

import com.badlogic.gdx.Gdx
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.prefs.Preferences
import kotlin.concurrent.thread


object LC {

    private val userRoot: Preferences = Preferences.userRoot().node("io/rhre")

    private fun determineSource(key: String, main: RHRE3Application): String {
        val source1 = main.preferences.getString(key, "") ?: ""
        val source2 = userRoot.get(key, "") ?: ""

        return if (source1 == source2) source1 else (if (source1.isEmpty() && source2.isNotEmpty()) source2 else if (source1.isNotEmpty() && source2.isEmpty()) source1 else "")
    }

    private fun persist(key: String, value: String, main: RHRE3Application) {
        try {
            main.preferences.putString(key, value).flush()
            userRoot.put(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun all(main: RHRE3Application) {
        lc(main)
    }

    fun lc(main: RHRE3Application) {
        GlobalScope.launch {
            var source = determineSource("l", main)

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

            if (RHRE3.lc != null) {
                source = RHRE3.lc?.replace("\\n", "\n") ?: ""
            } else {
                fun persist() = persist("l", source, main)
                Runtime.getRuntime().addShutdownHook(thread(start = false, block = ::persist))
            }

            if (source.isNotEmpty()) {
                val reason = source
                Gdx.app.postRunnable {
                    main.screen = LCScreen(main, reason)
                }
            }
        }
    }

}