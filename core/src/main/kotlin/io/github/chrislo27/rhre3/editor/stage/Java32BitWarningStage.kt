package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class Java32BitWarningStage(val screen: EditorScreen) : Stage<EditorScreen>(screen.stage, screen.stage.camera) {

    val genericStage: GenericStage<EditorScreen> = GenericStage(screen.main.uiPalette, this, this.camera)

    init {
        genericStage.run {
            drawBackground = false
            titleLabel.text = "Java 32-bit ver. detected"
            titleLabel.isLocalizationKey = false
            titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("logo_256"))
            backButton.visible = true
            onBackButtonClick = {
                Gdx.app.postRunnable {
                    (this@Java32BitWarningStage.parent as Stage<EditorScreen>).elements.remove(this@Java32BitWarningStage)
                }
            }
        }

        elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 1f)
        }
        elements += InputSponge(this, this).apply {
            this.shouldAbsorbInput = true
        }
        elements += genericStage

        val palette = screen.main.uiPalette
        genericStage.centreStage.elements += TextLabel(palette, genericStage.centreStage, genericStage.centreStage).apply {
            this.text = """A 32-bit version of the Java Runtime Environment was detected.
                |
                |Please note that only 64-bit versions of Java are supported for use with
                |the Rhythm Heaven Remix Editor. You may experience crashes due to running out of memory on a 32-bit Java version.
                |
                |Please open the link below and download & install the appropriate 64-bit Java version.
                |Be sure to uninstall your existing Java installation first.
                |
                |Windows: Windows Offline (64-bit)
                |macOS: Mac OS X
                |Linux: Linux x64 (or through your system package manager)
            """.trimMargin()
            this.isLocalizationKey = false
            this.textWrapping = true
            this.fontScaleMultiplier = 0.85f
        }
        genericStage.bottomStage.elements += Button(palette, genericStage.bottomStage, genericStage.bottomStage).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI("""https://java.com/en/download/manual.jsp""")
            }
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.textWrapping = false
                this.text = "Open Java downloads page\n[#8CB8FF]https://java.com/en/download/manual.jsp[]"
            })
            this.location.set(0.275f, 0f, 0.45f, 1f)
        }
    }

}