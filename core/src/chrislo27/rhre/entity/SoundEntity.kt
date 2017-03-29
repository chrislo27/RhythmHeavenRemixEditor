package chrislo27.rhre.entity

import chrislo27.rhre.Main
import chrislo27.rhre.editor.Editor
import chrislo27.rhre.inspections.InspectionFunction
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.registry.Game
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.registry.SoundCue
import chrislo27.rhre.track.Remix
import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.backends.lwjgl.audio.OpenALSound
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import ionium.registry.AssetRegistry
import ionium.util.Utils
import java.util.*

class SoundEntity(remix: Remix, val cue: SoundCue, beat: Float, level: Int, duration: Float,
				  @Volatile override var semitone: Int) : Entity(remix), HasGame, SoundCueActionProvider {
	override val game: Game
	private val isFillbotsFill: Boolean
	@Volatile private var soundId: Long = -1
	@Volatile private var introSoundId: Long = -1
	@Volatile private var startTime = 0f

	override val isStretchable: Boolean
		get() = cue.canAlterDuration

	override val isRepitchable: Boolean
		get() = cue.canAlterPitch

	override val id: String
		get() = cue.id

	override val name: String
		get() = cue.name

	override val inspectionFunctions: List<InspectionFunction>
		get() = cue.inspectionFunctions

	init {

		this.bounds.set(beat, level.toFloat(), duration, 1f)

		game = GameRegistry[cue.id.substring(0, cue.id.indexOf('/'))]!!
		isFillbotsFill = cue.id == "fillbots/water"
	}

	constructor(remix: Remix, cue: SoundCue, beat: Float, level: Int, semitone: Int) : this(remix, cue, beat, level,
																							cue.duration, semitone)

	override fun attemptLoadSounds(): Boolean {
		return cue.attemptLoadSounds()
	}

	override fun adjustPitch(semitoneChange: Int, min: Int, max: Int): Boolean {
		val old = semitone
		semitone = MathUtils.clamp(semitone + semitoneChange, min, max)

		return semitone == old
	}

	override fun copy(): SoundEntity {
		return SoundEntity(remix, cue, bounds.x, bounds.y.toInt(), bounds.width, semitone)
	}

	override fun render(main: Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean) {
		renderRect(batch, if (!cue.canAlterDuration) palette.soundCue else palette.stretchableSoundCue,
				   palette.selectionTint, selected, bounds)

		batch.setColor(1f, 1f, 1f, 0.25f)
		batch.draw(AssetRegistry.getTexture("gameIcon_" + cue.id.substring(0, cue.id.indexOf('/'))),
				   bounds.getX() * Entity.Companion.PX_WIDTH + Editor.GAME_ICON_PADDING,
				   bounds.getY() * Entity.Companion.PX_HEIGHT + bounds.getHeight() * Entity.Companion.PX_HEIGHT.toFloat() * 0.5f - Editor.GAME_ICON_SIZE * 0.5f,
				   Editor.GAME_ICON_SIZE.toFloat(), Editor.GAME_ICON_SIZE.toFloat())
		batch.setColor(1f, 1f, 1f, 1f)

		main.font.data.setScale(0.5f)
		main.font.color = palette.cueText
		val name = cue.name
		val targetWidth = bounds.getWidth() * Entity.Companion.PX_WIDTH - 8
		val height = Utils.getHeightWithWrapping(main.font, name, targetWidth)
		main.font.draw(batch, name, bounds.getX() * Entity.Companion.PX_WIDTH,
					   bounds.getY() * Entity.Companion.PX_HEIGHT + bounds.getHeight() * Entity.Companion.PX_HEIGHT.toFloat() * 0.5f + height * 0.5f,
					   targetWidth,
					   Align.right, true)

		if (cue.canAlterPitch) {
			main.font.draw(batch, Semitones.getSemitoneName(semitone), bounds.getX() * Entity.Companion.PX_WIDTH + 4,
						   bounds.getY() * Entity.Companion.PX_HEIGHT + main.font.capHeight + 4f)
		}
		main.font.data.setScale(1f)
	}

	override fun onStart(delta: Float, intendedStart: Float) {
		super.onStart(delta, intendedStart)

		startTime = intendedStart

		val bpm = remix.tempoChanges.getTempoAt(remix.getBeat())

		if (cue.getIntroSoundObj() != null) {
			val s = cue.getIntroSoundObj()!! as OpenALSound
			introSoundId = s.play(1f, cue.getPitch(semitone, bpm), 0f)
		}

		val s = cue.getSoundObj() as OpenALSound
		val realPos = remix.tempoChanges.beatsToSeconds(remix.getBeat() - intendedStart) % s.duration()
		if (cue.shouldBeLooped()) {
			soundId = s.loop(1f, cue.getPitch(semitone, bpm), 0f)
		} else {
			soundId = s.play(1f, cue.getPitch(semitone, bpm), 0f)
		}

	}

	override fun onWhile(delta: Float) {
		super.onWhile(delta)

		if (isFillbotsFill && soundId != -1L) {
			val s = cue.getSoundObj() as OpenALSound
			val remainder = MathUtils
					.clamp(1f - (startTime + bounds.width - remix.getBeat()) / bounds.width, 0f, 1f)
			val from = if (bounds.width <= 3)
				MathUtils.lerp(1f, 0.6f, (bounds.width - 1) / 2f)
			else
				MathUtils.lerp(0.6f, 0.4f, (bounds.width - 3) / 4f)

			s.setPitch(soundId, MathUtils.lerp(from, from + 0.6f, remainder))
			// medium: 0.6f - 1.2f
			// big:    0.5f - 1.1f
			// small:  1.0f - 1.6f
		}
	}

	override fun onEnd(delta: Float, intendedEnd: Float) {
		super.onEnd(delta, intendedEnd)

		if (cue.shouldBeStopped()) {
			if (cue.getIntroSoundObj() != null)
				cue.getIntroSoundObj()!!.stop(introSoundId)
			cue.getSoundObj().stop(soundId)
		}

	}

	override fun provide(): List<SoundCueAction> {
		val list = ArrayList<SoundCueAction>()

		val startTime = remix.tempoChanges.beatsToSeconds(this.bounds.x)
		list.add(SoundCueAction(cue, startTime,
								remix.tempoChanges.beatsToSeconds(this.bounds.x + this.bounds.width) - startTime))

		return list
	}
}
