package io.github.chrislo27.rhre3.demo

import io.github.chrislo27.rhre3.RemixRecovery
import io.github.chrislo27.toolboks.Toolboks
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.concurrent.thread


object Demo {

    val startTime: ZonedDateTime = ZonedDateTime.of(2018, 7, 10, 12, 0, 0, 0, ZoneId.of("America/Vancouver"))
    val endTime: ZonedDateTime = ZonedDateTime.of(2018, 7, 11, 12, 0, 0, 0, ZoneId.of("America/Vancouver"))

    val localStartTime: LocalDateTime = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val localEndTime: LocalDateTime = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    fun startForceExitThread(currentTime: ZonedDateTime) {
        thread(name = "Demo Timebomb") {
            try {
                while (!Thread.interrupted()) {
                    val sleepTime: Long = (endTime.toEpochSecond() - currentTime.toEpochSecond()) * 1000L
                    Toolboks.LOGGER.info("Sleep time is $sleepTime ms")
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            if (e is InterruptedException) {
                                Toolboks.LOGGER.warn("Kill thread was interrupted")
                            }
                        }
                    }
                    try {
                        RemixRecovery.saveRemixInRecovery()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Toolboks.LOGGER.info("Exiting due to time constraints")
                    System.exit(0)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            Toolboks.LOGGER.info("Exiting due to being broken out of while loop")
            System.exit(1)
        }
    }
}