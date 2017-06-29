package chrislo27.rhre.entity

import chrislo27.rhre.Main
import chrislo27.rhre.editor.Editor
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.registry.Game
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.registry.SoundCue
import chrislo27.rhre.track.Remix
import chrislo27.rhre.track.Semitones
import com.badlogic.gdx.backends.lwjgl.audio.OpenALSound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import ionium.registry.AssetRegistry
import ionium.util.Utils

class SoundEntity(remix: Remix, val cue: SoundCue, beat: Float, level: Int, duration: Float,
				  @Volatile override var semitone: Int) : Entity(remix), HasGame{
	override val game: Game
	private val isFillbotsFill: Boolean
	var stopSoundAlways: Boolean = false
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

	private val iconId: String = "gameIcon_" + cue.id.substring(0, cue.id.indexOf('/'))

	init {
		this.bounds.set(beat, level.toFloat(), duration, 1f)

		game = GameRegistry[cue.id.substring(0, cue.id.indexOf('/'))]!!
		isFillbotsFill = cue.id == "fillbots/water" || cue.id == "fillbotsMegamix/water"
	}

	constructor(remix: Remix, cue: SoundCue, beat: Float, level: Int, semitone: Int) : this(remix, cue, beat, level,
																							cue.duration, semitone)

	override fun attemptLoadSounds(): Boolean {
		return cue.attemptLoadSounds()
	}

	override fun needsToBeLoaded(): Boolean {
		return cue.needsToBeLoaded()
	}

	override fun adjustPitch(semitoneChange: Int, min: Int, max: Int): Boolean {
		val old = semitone
		semitone = MathUtils.clamp(semitone + semitoneChange, min, max)

		return semitone != old
	}

	override fun copy(): SoundEntity {
		return SoundEntity(remix, cue, bounds.x, bounds.y.toInt(), bounds.width, semitone)
	}

	override fun render(main: Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean) {
		renderRect(batch, if (!cue.canAlterDuration) palette.soundCue else palette.stretchableSoundCue,
				   palette.selectionTint, selected, bounds)

		batch.setColor(1f, 1f, 1f, 0.25f)
		batch.draw(AssetRegistry.getTexture(iconId),
				   bounds.getX() * Entity.Companion.PX_WIDTH + Editor.GAME_ICON_PADDING,
				   bounds.getY() * Entity.Companion.PX_HEIGHT + bounds.getHeight() * Entity.Companion.PX_HEIGHT.toFloat() * 0.5f - Editor.GAME_ICON_SIZE * 0.5f,
				   Editor.GAME_ICON_SIZE.toFloat(), Editor.GAME_ICON_SIZE.toFloat())
		batch.setColor(1f, 1f, 1f, 1f)

		main.font.data.setScale(0.5f)
		main.font.color = palette.cueText
		val name = cue.newlinedName
		val targetWidth = bounds.getWidth() * Entity.Companion.PX_WIDTH - 8
		val height = Utils.getHeightWithWrapping(main.font, name, targetWidth)
		main.font.draw(batch, name, bounds.getX() * Entity.Companion.PX_WIDTH,
					   bounds.getY() * Entity.Companion.PX_HEIGHT + bounds.getHeight() * Entity.Companion.PX_HEIGHT.toFloat() * 0.5f + height * 0.5f,
					   targetWidth,
					   Align.right, true)

		if (cue.canAlterPitch || semitone != 0) {
			if (!cue.canAlterPitch) {
				main.font.color = Color.RED
			}
			main.font.draw(batch, Semitones.getSemitoneName(semitone), bounds.getX() * Entity.Companion.PX_WIDTH + 4,
						   bounds.getY() * Entity.Companion.PX_HEIGHT + main.font.capHeight + 4f)
		}
		main.font.data.setScale(1f)
	}

	override fun onStart(delta: Float, intendedStart: Float) {
		super.onStart(delta, intendedStart)

		startTime = intendedStart

		val bpm = remix.tempoChanges.getTempoAt(remix.beat)
		val pan = cue.pan

		if (cue.getIntroSoundObj() != null) {
			val s = cue.getIntroSoundObj()!! as OpenALSound
			introSoundId = s.play(volume, cue.getPitch(semitone, bpm), pan)
		}

		val s = cue.getSoundObj() as OpenALSound
		val realPos = remix.tempoChanges.beatsToSeconds(remix.beat - intendedStart) % s.duration()
		if (cue.shouldBeLooped()) {
			soundId = s.loop(volume, cue.getPitch(semitone, bpm), pan)
		} else {
			soundId = s.play(volume, cue.getPitch(semitone, bpm), pan)
		}

	}

	override fun onWhile(delta: Float) {
		super.onWhile(delta)

		if (isFillbotsFill && soundId != -1L) {
			val s = cue.getSoundObj() as OpenALSound
			val remainder = MathUtils
					.clamp(1f - (startTime + bounds.width - remix.beat) / bounds.width, 0f, 1f)
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

		if (cue.shouldBeStopped() || stopSoundAlways) {
			if (cue.getIntroSoundObj() != null)
				cue.getIntroSoundObj()!!.stop(introSoundId)
			cue.getSoundObj().stop(soundId)
		}

	}
}
