package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette


class TestScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, TestScreen>(main) {

    override val stage: Stage<TestScreen> = Stage(null, main.defaultCamera)

    val palette: UIPalette

    init {
        // init stage
        palette = UIPalette(main.fonts[main.defaultFontKey], 1f, Color(1f, 1f, 1f, 1f), Color(0f, 0f, 0f, 0.75f),
                            Color(0f, 0.5f, 0.5f, 0.75f), Color(0.25f, 0.25f, 0.25f, 0.75f))

        stage.elements.add(object : TextLabel<TestScreen>(palette, stage, stage) {

        }.apply {
            this.background = true
            this.textAlign = Align.center
            this.location.set(0.25f, 0.25f, 0f, 0f, 0f, 0f, 512f, 64f)
            this.setText("Test Text yeyeyeyey")
        })

        val button = object : Button<TestScreen>(palette, stage, stage) {
            var toggle = true
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (toggle) {
                    (labels.first() as TextLabel).setText("Depuis que je t'ai croisée,\n" +
                                                                  "Je ne peux plus t'oublier,\n" +
                                                                  "Et je danse jour et nuit,\n" +
                                                                  "Je rêve de ta companie,\n" +
                                                                  "Tu as changé ma vie,\n" +
                                                                  "Tu es si jolie! Yeah! Yeah!\n" +
                                                                  "\n" +
                                                                  "Mignonne petite poupée,\n" +
                                                                  "Regardez-la danser. Ooh!\n" +
                                                                  "Elle et son partenaire,\n" +
                                                                  "Ils sont tous les deux d'enfer!\n" +
                                                                  "Tu as changé ma vie!\n" +
                                                                  "Tu es si jolie! Ooh! Ooh! Ooh!\n" +
                                                                  "\n" +
                                                                  "Prend le temps\n" +
                                                                  "De profiter de la vie,\n" +
                                                                  "(T'en fais pas,\n" +
                                                                  "Oublie tes soucis!)\n" +
                                                                  "T'arrête pas mon ange dorée,\n" +
                                                                  "Il faut toujours avancer,\n" +
                                                                  "Passe toutes tes nuits\n" +
                                                                  "A danser! Hey!\n" +
                                                                  "\n" +
                                                                  "Allons vibrer!\n" +
                                                                  "Profitons de cette chanson!\n" +
                                                                  "Tout le monde, bougeons au\n" +
                                                                  "Rythme de cette musique! Hey!\n" +
                                                                  "Chantons en choeur! Après tout\n" +
                                                                  "Ce n'est qu'une chanson!\n" +
                                                                  "J'en veux encore!\n" +
                                                                  "Le rock, c'est magique!\n" +
                                                                  "\n" +
                                                                  "Encore une fois! Dansons\n" +
                                                                  "Sur un bon rock'n'roll! Tous\n" +
                                                                  "Ensemble, montrez ce que\n" +
                                                                  "Vous savez faire! Hey!\n" +
                                                                  "Chantons en choeur!\n" +
                                                                  "Reprenons ensemble\n" +
                                                                  "Ces paroles! Allez vibrons!\n" +
                                                                  "Ensemble, solidaires!")
                } else {
                    (labels.first() as TextLabel).setText("Button test")
                }
                toggle = !toggle
            }
        }

        val label = object : TextLabel<TestScreen>(palette, button, stage) {

        }.apply {
            this.setText("Button test")
        }

        button.apply {
            this.addLabel(label)

            this.location.set(0.25f, 0.5f, 0f, 0f, 0f, 0f, 256f, 64f)
        }


        stage.elements.add(button)

        stage.updatePositions()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render(delta)
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}