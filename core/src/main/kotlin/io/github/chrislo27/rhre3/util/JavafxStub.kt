package io.github.chrislo27.rhre3.util

import javafx.application.Application
import javafx.stage.Stage


/**
 * Used to start the JavaFX runtime for JavaFX file choosers only.
 */
class JavafxStub : Application() {

    companion object {
        lateinit var application: JavafxStub
            private set
    }

    var primaryStage: Stage? = null
        private set

    override fun start(primaryStage: Stage) {
        Companion.application = this
        this.primaryStage = primaryStage
    }
}