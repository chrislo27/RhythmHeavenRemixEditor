package io.github.chrislo27.toolboks.oshi

import oshi.SystemInfo


object OSHI {

    @Volatile var isInitialized: Boolean = false
        private set

    val sysInfo: SystemInfo by lazy {
        isInitialized = false
        SystemInfo().apply {
            hardware.apply {
                processor
            }

            isInitialized = true
        }
    }

}