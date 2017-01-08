package chrislo27.rhre.visual.impl

import chrislo27.rhre.track.Remix
import chrislo27.rhre.visual.Renderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import ionium.registry.AssetRegistry


class BouncyRoadRenderer : Renderer() {

	val texture: Texture by lazy {
		AssetRegistry.getTexture("visual_bouncyRoad")
	}

	val redRod: TextureRegion by lazy {
		TextureRegion(texture, 1, 1, 54, 214)
	}

	val yellowRod: TextureRegion by lazy {
		TextureRegion(texture, 57, 1, 54, 214)
	}

	val bouncer: TextureRegion by lazy {
		TextureRegion(texture, 113, 1, 46, 78)
	}

	val spheroid: TextureRegion by lazy {
		TextureRegion(texture, 161, 1, 38, 38)
	}

	val background: TextureRegion by lazy {
		TextureRegion(texture, 232, 0, 24, 256)
	}

	val entities: MutableList<Object> = mutableListOf()
	val bouncers: MutableList<Bouncer> = mutableListOf()
	val topOfBouncer = 48

	init {
		prepare()
	}

//	fun getBounce(): Float {
//		return MathUtils.sin()
//	}

	fun prepare() {
		bouncers.add(Bouncer(0, -23f, 222f))
		bouncers.add(Bouncer(0, 80f, 218f))
		bouncers.add(Bouncer(0, 80f + 64f, 213f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f, 207f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f, 200f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f, 192f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f + 28, 180f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f + 28 + 24, 164f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f + 28 + 24 + 8, 150f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f + 28 + 24 + 8 - 5, 132f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f + 28 + 24 + 8 - 5 - 32, 118f))
		bouncers.add(Bouncer(0, 80f + 64f + 56f + 48f + 32f + 28 + 24 + 8 - 5 - 32 - 48, 104f))
		bouncers.add(Bouncer(2, 80f + 64f + 56f + 48f + 32f + 28 + 24 + 8 - 5 - 32 - 48 - 72, 92f))
		bouncers.add(Bouncer(1, 80f + 64f + 56f + 48f + 32f + 28 + 24 + 8 - 5 - 32 - 48 - 72 - 104, 84f))
		bouncers.add(Bouncer(0, -28f, 76f))
		entities.addAll(bouncers)
		Interpolation.bounce
	}

	override fun onEnd(remix: Remix) {
	}

	override fun onStart(remix: Remix) {
	}

	override fun render(batch: SpriteBatch, remix: Remix) {
		batch.draw(background, 0f, 0f, 400f, 240f)
		entities.sortByDescending { it.y }
		entities.forEach { it.render(batch, remix) }

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			entities.clear()
			prepare()
		}
	}

	open inner class Object(val x: Float, val y: Float) {

		constructor() : this(0f, 0f)

		open fun render(batch: SpriteBatch, remix: Remix) {

		}

		open fun update() {

		}

		fun getScale(): Float {
			return MathUtils.lerp(1.0f, 0.4f, y / 240f)
		}
	}

	inner class Bouncer(val type: Int, x: Float, y: Float) : Object(x, y) {

		var bounce: Float = 0f

		override fun update() {
			super.update()

			if (bounce > 0) {
				bounce = Math.max(0f, bounce - Gdx.graphics.deltaTime / 0.25f)
			}
		}

		override fun render(batch: SpriteBatch, remix: Remix) {
			val region: TextureRegion
			when (type) {
				0 -> region = bouncer
				1 -> region = redRod
				2 -> region = yellowRod
				else -> return
			}

			fun SpriteBatch.drawWithPart(region: TextureRegion, x: Float, y: Float, width: Float, height: Float,
										 srcX: Int, srcY: Int, srcW: Int, srcH: Int) {
				this.draw(region.texture, x, y, width * 0.5f, 0f, width, height,
						  getScale(), getScale(), 0f, srcX + region.regionX,
						  srcY + region.regionY, srcW, srcH, false, false)
			}

			batch.drawWithPart(region, x, y - topOfBouncer * getScale() + bounce * 5 * getScale(),
							   region.regionWidth * 1f,
							   topOfBouncer * 1f, 0, 0, region.regionWidth, topOfBouncer)
			batch.drawWithPart(region, x,
							   y - (region.regionHeight) * getScale() + bounce * topOfBouncer * 1.2f * getScale(),
							   region.regionWidth * 1f,
							   (region.regionHeight - topOfBouncer) * 1f, 0, topOfBouncer, region.regionWidth,
							   region.regionHeight - topOfBouncer)
		}

	}

}