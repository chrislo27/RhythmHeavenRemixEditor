package io.github.chrislo27.toolboks.logging


open class Logger {

    val msTimeStarted: Long = System.currentTimeMillis()
    val msTimeElapsed: Long
        get() = System.currentTimeMillis() - msTimeStarted

    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR, NONE
    }

    var loggingLevel: LogLevel = LogLevel.DEBUG

    protected open fun defaultPrint(level: LogLevel, msg: String) {
        val millis = msTimeElapsed
        val second = (millis / 1000) % 60
        val minute = millis / (1000 * 60) % 60
        val hour = millis / (1000 * 60 * 60) % 24
        val text = "${String.format("%02d:%02d:%02d.%03d", hour, minute, second, millis % 1000)}: [${level.name}][${Thread.currentThread().name}] $msg"

        if (level.ordinal >= LogLevel.WARN.ordinal) {
            System.err.println(text)
        } else {
            System.out.println(text)
        }
    }

    open fun debug(msg: String) {
        if (loggingLevel.ordinal <= LogLevel.DEBUG.ordinal) {
            defaultPrint(LogLevel.DEBUG, msg)
        }
    }

    open fun info(msg: String) {
        if (loggingLevel.ordinal <= LogLevel.INFO.ordinal) {
            defaultPrint(LogLevel.INFO, msg)
        }
    }

    open fun warn(msg: String) {
        if (loggingLevel.ordinal <= LogLevel.WARN.ordinal) {
            defaultPrint(LogLevel.WARN, msg)
        }
    }

    open fun error(msg: String) {
        if (loggingLevel.ordinal <= LogLevel.ERROR.ordinal) {
            defaultPrint(LogLevel.ERROR, msg)
        }
    }

}