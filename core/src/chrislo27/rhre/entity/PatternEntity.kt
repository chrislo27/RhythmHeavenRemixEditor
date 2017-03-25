package chrislo27.rhre.entity

import chrislo27.rhre.editor.Editor
import chrislo27.rhre.inspections.InspectionFunction
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.registry.Game
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.registry.Pattern
import chrislo27.rhre.track.PlaybackCompletion
import chrislo27.rhre.track.Remix
import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import ionium.registry.AssetRegistry
import ionium.templates.Main
import ionium.util.Utils
import java.util.*
import java.util.function.Consumer

class PatternEntity(remix: Remix, val pattern: Pattern) : Entity(remix), HasGame, SoundCueActionProvider {

	val internal: MutableList<SoundEntity>
	private val originalBounds: MutableList<Vector2>
	private val originalSemitones: MutableList<Int>
	private val originalWidth: Float
	override val isRepitchable: Boolean
	override val game: Game
	@Volatile override var semitone: Int = 0
		private set

	override val name: String
		get() = pattern.name

	override val inspectionFunctions: List<InspectionFunction>
		get() = pattern.inspectionFunctions

	init {
		this.internal = ArrayList<SoundEntity>()

		pattern.cues.forEach { c ->
			val sc = GameRegistry.getCue(c.id)

			//			Main.logger.debug("Initializing pattern - loading " + pc.getId() + " " + sc);

			internal.add(SoundEntity(remix, sc!!, c.beat, c.track,
									 if (sc!!.canAlterDuration && c.duration > 0) c.duration else sc.duration,
									 c.semitone))
		}

		if (internal.size == 0)
			throw IllegalStateException("Pattern must have content!")

		var highest: SoundEntity? = null
		var furthest: SoundEntity? = null
		for (se in internal) {
			if (highest == null || se.bounds.y > highest.bounds.y)
				highest = se

			if (furthest == null || se.bounds.x + se.bounds.width > furthest.bounds.x + furthest
					.bounds.width)
				furthest = se

		}
		try {
			this.bounds.height = highest!!.bounds.y + highest.bounds.height
			this.bounds.width = furthest!!.bounds.x + furthest.bounds.width
		} catch (e: NullPointerException) {
			throw RuntimeException(
					"Couldn't create pattern entity - highest or furthest was null (h: " + highest + ", f: " +
							furthest + ")")
		}

		this.originalWidth = this.bounds.width

		this.originalBounds = ArrayList<Vector2>()
		internal.forEach { se -> originalBounds.add(Vector2(se.bounds.getX(), se.bounds.getWidth())) }

		this.originalSemitones = ArrayList<Int>()
		internal.forEach { se -> originalSemitones.add(se.semitone) }

		isRepitchable = internal.stream().anyMatch { se -> se.cue.canAlterPitch }
		val id = pattern.cues[0].id
		game = GameRegistry[id.substring(0, id.indexOf('/'))]!!
	}

	override fun attemptLoadSounds(): Boolean {
		var b = false
		for (se in internal) {
			b = b or se.attemptLoadSounds()
		}
		return b
	}

	override fun onLengthChange(old: Float) {
		super.onLengthChange(old)

		for (i in internal.indices) {
			val se = internal[i]
			val originalData = originalBounds[i]

			val ratioX = originalData.x / originalWidth
			se.bounds.x = ratioX * this.bounds.width

			val ratioW = originalData.y / originalWidth
			se.bounds.width = ratioW * this.bounds.width
		}
	}

	override fun adjustPitch(semitoneChange: Int, min: Int, max: Int): Boolean {
		val original = IntArray(internal.size)
		val originalSt = semitone
		for (i in internal.indices) {
			original[i] = internal[i].semitone
		}

		semitone += semitoneChange

		var changed = false

		for (se in internal) {
			se.semitone += semitoneChange

			if (se.semitone < min || se.semitone > max) {
				for (i in internal.indices) {
					internal[i].semitone = original[i]
				}
				semitone = originalSt

				changed = false

				break
			} else {
				changed = true
			}
		}

		return changed
	}

	override fun copy(): PatternEntity {
		val pe = PatternEntity(this.remix, pattern)

		pe.bounds.set(this.bounds)
		pe.semitone = semitone
		pe.internal.forEach { se -> se.semitone += pe.semitone }
		pe.onLengthChange(pe.bounds.width)

		return pe
	}

	override val isStretchable: Boolean
		get() = pattern.isStretchable

	override val id: String
		get() = pattern.id

	override fun render(main: chrislo27.rhre.Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean) {
		renderRect(batch, if (!pattern.isStretchable) palette.pattern else palette.stretchablePattern,
				   palette.selectionTint, selected, this.bounds)

		batch.setColor(0f, 0f, 0f, 0.1f)
		for (se in internal) {
			Main.fillRect(batch, bounds.getX() * Entity.Companion.PX_WIDTH + se.bounds.x * Entity.Companion.PX_WIDTH,
						  bounds.getY() * Entity.Companion.PX_HEIGHT + se.bounds.y * Entity.Companion.PX_HEIGHT, se
								  .bounds.width * Entity.Companion.PX_WIDTH,
						  se.bounds.height * Entity.Companion.PX_HEIGHT)
			Main.drawRect(batch, bounds.getX() * Entity.Companion.PX_WIDTH + se.bounds.x * Entity.Companion.PX_WIDTH,
						  bounds.getY() * Entity.Companion.PX_HEIGHT + se.bounds.y * Entity.Companion.PX_HEIGHT, se
								  .bounds.width * Entity.Companion.PX_WIDTH,
						  se.bounds.height * Entity.Companion.PX_HEIGHT, 4f)
			Main.drawRect(batch, bounds.getX() * Entity.Companion.PX_WIDTH + se.bounds.x * Entity.Companion.PX_WIDTH,
						  bounds.getY() * Entity.Companion.PX_HEIGHT + se.bounds.y * Entity.Companion.PX_HEIGHT, se
								  .bounds.width * Entity.Companion.PX_WIDTH,
						  se.bounds.height * Entity.Companion.PX_HEIGHT, 1f)
		}

		batch.setColor(1f, 1f, 1f, 0.25f)
		batch.draw(AssetRegistry.getTexture(
				"gameIcon_" + internal[0].cue.id.substring(0, internal[0].cue.id.indexOf('/'))),
				   bounds.getX() * Entity.Companion.PX_WIDTH + Editor.GAME_ICON_PADDING,
				   bounds.getY() * Entity.Companion.PX_HEIGHT + bounds.getHeight() * Entity.Companion.PX_HEIGHT.toFloat() * 0.5f - Editor.GAME_ICON_SIZE * 0.5f,
				   Editor.GAME_ICON_SIZE.toFloat(), Editor.GAME_ICON_SIZE.toFloat())
		batch.setColor(1f, 1f, 1f, 1f)

		main.font.data.setScale(0.5f)
		main.font.color = palette.cueText
		val name = pattern.name
		val targetWidth = bounds.getWidth() * Entity.Companion.PX_WIDTH - 8
		val height = Utils.getHeightWithWrapping(main.font, name, targetWidth)
		main.font.draw(batch, name, bounds.getX() * Entity.Companion.PX_WIDTH,
					   bounds.getY() * Entity.Companion.PX_HEIGHT + bounds.getHeight() * Entity.Companion.PX_HEIGHT.toFloat() * 0.5f + height * 0.5f,
					   targetWidth,
					   Align.right, true)
		if (isRepitchable) {
			main.font.draw(batch, Semitones.getSemitoneName(semitone), bounds.getX() * Entity.Companion.PX_WIDTH + 4,
						   bounds.getY() * Entity.Companion.PX_HEIGHT + main.font.capHeight + 4f)
		}
		main.font.data.setScale(1f)
	}

	override fun reset() {
		super.reset()
		internal.forEach(Consumer<SoundEntity> { it.reset() })
	}

	override fun onStart(delta: Float, intendedStart: Float) {
		super.onStart(delta, intendedStart)
	}

	override fun onEnd(delta: Float, intendedStart: Float) {
		super.onEnd(delta, intendedStart)

		internal.forEach { se ->
			se.onEnd(delta, this.bounds.x + se.bounds.x + se.bounds.width)
			se.playbackCompletion = PlaybackCompletion.FINISHED
		}
	}

	override fun onWhile(delta: Float) {
		super.onWhile(delta)

		for (e in internal) {
			if (e.playbackCompletion === PlaybackCompletion.FINISHED)
				continue

			if (remix.getBeat() >= this.bounds.x + e.bounds.x) {
				if (e.playbackCompletion === PlaybackCompletion.WAITING) {
					e.onStart(delta, this.bounds.x + e.bounds.x)
					e.playbackCompletion = PlaybackCompletion.STARTED
				}

				if (e.playbackCompletion === PlaybackCompletion.STARTED) {
					e.onWhile(delta)

					if (remix.getBeat() >= this.bounds.x + e.bounds.x + e.bounds.width) {
						e.onEnd(delta, this.bounds.x + e.bounds.x + e.bounds.width)
						e.playbackCompletion = PlaybackCompletion.FINISHED
					}
				}
			}
		}
	}

	override fun provide(): List<SoundCueAction> {
		val list = ArrayList<SoundCueAction>()

		internal.forEach { se ->
			val startTime = remix.tempoChanges.beatsToSeconds(this.bounds.x + se.bounds.x)
			list.add(SoundCueAction(se.cue, startTime,
									remix.tempoChanges.beatsToSeconds(
											this.bounds.x + se.bounds.x + se.bounds.width) - startTime))
		}

		return list
	}
}
