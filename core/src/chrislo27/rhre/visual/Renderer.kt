package chrislo27.rhre.visual

import chrislo27.rhre.track.Remix
import com.badlogic.gdx.graphics.g2d.SpriteBatch


abstract class Renderer {

	abstract fun render(batch: SpriteBatch, remix: Remix)

	abstract fun onStart(remix: Remix)

	abstract fun onEnd(remix: Remix)

}