package chrislo27.rhre.script.luaobj

import chrislo27.rhre.editor.Editor
import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.entity.SoundEntity
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.track.Remix
import chrislo27.rhre.track.TempoChange
import org.luaj.vm2.*
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua


class LuaRemix(globals: Globals, remix: Remix) : LuaObj(globals, remix) {

	val entities: LuaTable
	val playbackStart: Float = remix.playbackStart
	val musicStart: Float = remix.musicStartTime
	val tempoChanges: LuaTable
	val length: Float = remix.getDuration()
	val musicVolume: Float = remix.musicVolume

	init {
		val eMap = mutableListOf<Pair<LuaValue, LuaValue>>()
		remix.entities.forEachIndexed { i, en ->
			eMap.add(Pair(CoerceJavaToLua.coerce(i + 1), CoerceJavaToLua.coerce(LuaEntity(globals, remix, en))))
		}
		entities = LuaTable.listOf(eMap.map { it.second }.toTypedArray())

		val tMap = mutableListOf<Pair<LuaValue, LuaValue>>()
		remix.tempoChanges.getBeatMap().values.forEachIndexed { index, tempoChange ->
			tMap.add(Pair(CoerceJavaToLua.coerce(index + 1),
						  CoerceJavaToLua.coerce(LuaTempoChange(globals, remix, tempoChange!!))))
		}
		tempoChanges = LuaTable.listOf(tMap.map { it.second }.toTypedArray())

		this.set("entities", entities)
		this.set("playbackStart", playbackStart.toDouble())
		this.set("musicStart", musicStart.toDouble())
		this.set("tempoChanges", tempoChanges)
		this.set("length", length.toDouble())
		this.set("musicVolume", musicVolume.toDouble())
		this.set("entityCount", remix.entities.size)
		this.set("changePlaybackStart", object : TwoArgFunction() {
			override fun call(self: LuaValue, arg: LuaValue): LuaValue {
				if (arg.type() != LuaValue.TNUMBER)
					throw LuaError(arg)

				val old = remix.playbackStart
				remix.playbackStart = arg.tofloat()

				resetRemixGlobal()
				return LuaValue.valueOf(old.toDouble())
			}
		})
		this.set("changeMusicStart", object : TwoArgFunction() {
			override fun call(self: LuaValue, arg: LuaValue): LuaValue {
				if (arg.type() != LuaValue.TNUMBER)
					throw LuaError(arg)

				val old = remix.musicStartTime
				remix.musicStartTime = arg.tofloat()

				resetRemixGlobal()
				return LuaValue.valueOf(old.toDouble())
			}
		})
		this.set("changeMusicVolume", object : TwoArgFunction() {
			override fun call(self: LuaValue, arg: LuaValue): LuaValue {
				if (arg.type() != LuaValue.TNUMBER)
					throw LuaError(arg)

				val old = remix.musicVolume
				remix.musicVolume = arg.tofloat().coerceIn(0.0f, 1.0f)

				resetRemixGlobal()
				return LuaValue.valueOf(old.toDouble())
			}
		})
		this.set("beatsToSeconds", object : TwoArgFunction() {
			override fun call(self: LuaValue, arg: LuaValue): LuaValue {
				if (arg.type() != LuaValue.TNUMBER)
					throw LuaError(arg)

				return LuaValue.valueOf(remix.tempoChanges.beatsToSeconds(arg.tofloat()).toDouble())
			}
		})
		this.set("secondsToBeats", object : TwoArgFunction() {
			override fun call(self: LuaValue, arg: LuaValue): LuaValue {
				if (arg.type() != LuaValue.TNUMBER)
					throw LuaError(arg)

				return LuaValue.valueOf(remix.tempoChanges.secondsToBeats(arg.tofloat()).toDouble())
			}
		})
		this.set("addCue", object : VarArgFunction() {
			override fun invoke(args: Varargs): Varargs {
				if (args.narg() !in 5..6)
					throw LuaError("Args is wrong number (5 or 6)")

				val id = args.arg(2).tostring().toString()
				if (GameRegistry.getCue(id) == null && GameRegistry.getPattern(id) == null)
					throw LuaError("Unknown ID: $id")

				val beat: Float = args.arg(3).tofloat()
				val track: Int = args.arg(4).toint()
				if (track < 1 || track > Editor.TRACK_COUNT)
					throw LuaError("Track out of range (1.." + Editor.TRACK_COUNT + "): " + track)

				val duration = if (args.narg() < 6) -1f else args.arg(5).tofloat()
				if (duration in 0f..0.125f)
					throw LuaError("Duration out of range (> 0.125): " + duration)

				val isPattern = GameRegistry.getPattern(id) != null
				val entity: Entity
				if (isPattern) {
					entity = PatternEntity(remix, GameRegistry.getPattern(id)!!)
					if (duration >= 0.125f) {
						val old = entity.bounds.width
						entity.bounds.width = duration
						entity.onLengthChange(old)
					}
				} else {
					val cue = GameRegistry.getCue(id)
					if (duration < 0) {
						entity = SoundEntity(remix, cue!!, beat, track - 1, 0)
					} else {
						entity = SoundEntity(remix, cue!!, beat, track - 1, duration, 0)
					}
				}

				remix.entities.add(entity)
				resetRemixGlobal()
				return LuaValue.valueOf(remix.entities.size)
			}
		})
		this.set("removeCue", object : TwoArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue): LuaValue {
				if (!arg2.isint())
					throw LuaError(arg2)

				if (arg2.toint() < 1 || arg2.toint() > remix.entities.size)
					return LuaValue.valueOf(false)

				val res = remix.entities.removeAt(arg2.toint() - 1)
				remix.selection.remove(res)
				resetRemixGlobal()
				return LuaValue.valueOf(true)
			}
		})
		this.set("moveCue", object : ThreeArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue, arg3: LuaValue): LuaValue {
				if (!arg2.isint())
					throw LuaError(arg2)

				if (arg2.toint() < 1 || arg2.toint() > remix.entities.size)
					return LuaValue.valueOf(false)

				remix.entities[arg2.toint()].bounds.x = arg3.tofloat()
				resetRemixGlobal()
				return LuaValue.valueOf(true)
			}
		})
		this.set("changeCueDuration", object : ThreeArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue, arg3: LuaValue): LuaValue {
				if (!arg2.isint())
					throw LuaError(arg2)

				if (arg2.toint() < 1 || arg2.toint() > remix.entities.size)
					return LuaValue.valueOf(false)

				if (arg3.tofloat() in 0f..0.125f)
					return LuaValue.valueOf(false)

				val e = remix.entities[arg2.toint()]
				val old = e.bounds.width
				e.bounds.width = arg3.tofloat()
				e.onLengthChange(old)
				resetRemixGlobal()
				return LuaValue.valueOf(true)
			}
		})
		this.set("changeCueSemitone", object : ThreeArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue, arg3: LuaValue): LuaValue {
				if (!arg2.isint())
					throw LuaError(arg2)

				if (arg2.toint() < 1 || arg2.toint() > remix.entities.size)
					return LuaValue.valueOf(false)

				if (!arg3.isint())
					throw LuaError(arg3)

				val e = remix.entities[arg2.toint()]
				if (!e.isRepitchable)
					return LuaValue.valueOf(false)
				val old = e.semitone
				e.adjustPitch(arg3.toint() - old, -Editor.MAX_SEMITONE, Editor.MAX_SEMITONE)
				if (e.semitone == old)
					return LuaValue.valueOf(false)

				resetRemixGlobal()
				return LuaValue.valueOf(true)
			}
		})
		this.set("addTempoChange", object : ThreeArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue, arg3: LuaValue): LuaValue {
				if (remix.tempoChanges.getTempoChangeFromBeat(arg2.tofloat()) != null)
					return LuaValue.valueOf(false)

				if (arg3.tofloat() <= 0)
					throw LuaError(arg3)

				remix.tempoChanges.add(TempoChange(arg2.tofloat(), arg3.tofloat(), remix.tempoChanges))
				resetRemixGlobal()
				return LuaValue.valueOf(true)
			}
		})
		this.set("removeTempoChange", object : TwoArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue): LuaValue {
				if (remix.tempoChanges.getTempoChangeFromBeat(arg2.tofloat()) == null)
					return LuaValue.valueOf(false)

				remix.tempoChanges.remove(remix.tempoChanges.getTempoChangeFromBeat(arg2.tofloat())!!)
				resetRemixGlobal()
				return LuaValue.valueOf(true)
			}
		})
		this.set("findTempoChange", object : TwoArgFunction() {
			override fun call(arg1: LuaValue?, arg2: LuaValue): LuaValue {
				if (remix.tempoChanges.getTempoChangeFromBeat(arg2.tofloat()) == null)
					return LuaValue.NIL

				return LuaTempoChange(globals, remix, remix.tempoChanges.getTempoChangeFromBeat(arg2.tofloat())!!)
			}
		})
		this.set("removeAllEntities", object : OneArgFunction() {
			override fun call(arg: LuaValue?): LuaValue {
				val count = remix.entities.size

				remix.entities.clear()
				return LuaValue.valueOf(count)
			}
		})
	}

}