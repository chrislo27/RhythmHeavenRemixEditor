package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.sun.jna.Platform
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.RemixRecovery
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.Java32BitWarningStage
import io.github.chrislo27.rhre3.editor.stage.StartupStage
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.toolboks.ToolboksScreen


class EditorScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, EditorScreen>(main) {

    companion object {
        var enteredEditor: Boolean = false
    }

    val editor: Editor = Editor(main, main.defaultCamera, true)
    override val stage: EditorStage
        get() = editor.stage

    private var firstShowing = true

    private val presenceCycle = 60f
    private var refreshPresence: Float = 0f

    override fun show() {
        super.show()
        (Gdx.input.inputProcessor as? InputMultiplexer)?.addProcessor(editor)
        enteredEditor = true

        if (firstShowing) {
            firstShowing = false
            RemixRecovery.cacheRemixChecksum(editor.remix)
            
            if (main.preferences.getBoolean(PreferenceKeys.SHOW_STARTUP_SCREEN, true)) {
                stage.elements += StartupStage(this)
            }
            if (!Platform.is64Bit()) {
                stage.elements += Java32BitWarningStage(this)
                AnalyticsHandler.track("32-bit Warning Screen", mapOf())
            }
            stage.updatePositions()
        }

        DiscordHelper.updatePresence(PresenceState.InEditor)
        refreshPresence = presenceCycle

        editor.updateMessageLabel()
    }

    override fun hide() {
        super.hide()
        editor.remix.playState = PlayState.STOPPED
        (Gdx.input.inputProcessor as? InputMultiplexer)?.removeProcessor(editor)
    }

    override fun render(delta: Float) {
        editor.render()
        super.render(delta)
        editor.postStageRender()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        editor.renderUpdate()

        refreshPresence -= Gdx.graphics.deltaTime
        if (refreshPresence <= 0) {
            refreshPresence = presenceCycle
            if (!stage.playalongStage.visible && !stage.presentationModeStage.visible && !(editor.remix.playState != PlayState.STOPPED && editor.remix.midiInstruments > 0)) {
                DiscordHelper.updatePresence(PresenceState.InEditor)
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        editor.resize()
    }

    override fun dispose() {
        editor.dispose()
    }

    override fun getDebugString(): String? {
        return editor.getDebugString()
    }

    override fun tickUpdate() {
    }
}