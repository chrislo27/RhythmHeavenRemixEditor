package chrislo27.rhre.inspections

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.math.MathUtils
import ionium.util.i18n.Localization


sealed class InspectionType(val beat: Float, val entity: Entity, val name: String, vararg val infoParams: String) {

	class InspNotOnbeat(beat: Float, entity: Entity) : InspectionType(beat, entity, "notOnbeat")
	class InspNotOffbeat(beat: Float, entity: Entity) : InspectionType(beat, entity, "notOffbeat")
	class InspMissing(beat: Float, entity: Entity, val needs: String, val needsAt: Float) :
			InspectionType(beat, entity, "missing", *arrayOf(entity.name, needs, needsAt.toString()))

	// inspect funcs

	class InspFuncNotOnbeat : InspectionFunction {
		override fun inspect(target: Entity, remix: Remix): InspectionType? {
			if (!MathUtils.isEqual(target.bounds.x, target.bounds.x.toInt().toFloat())) {
				return InspNotOnbeat(target.bounds.x, target)
			}

			return null
		}
	}

	class InspFuncNotOffbeat : InspectionFunction {
		override fun inspect(target: Entity, remix: Remix): InspectionType? {
			if (!MathUtils.isEqual(target.bounds.x, target.bounds.x.toInt().toFloat() + 0.5f)) {
				return InspNotOffbeat(target.bounds.x, target)
			}

			return null
		}
	}

	class InspFuncMissing(val offset: Float, val needsID: String) : InspectionFunction {
		override fun inspect(target: Entity, remix: Remix): InspectionType? {
			remix.entities.find {
				it !== target && MathUtils.isEqual(target.bounds.x + offset, it.bounds.x) && it.id == needsID
			} ?: return InspMissing(target.bounds.x, target, needsID.run {
				val pattern = GameRegistry.instance().getPatternRaw(needsID)
				if (pattern != null) {
					return@run pattern.name
				}

				val sound = GameRegistry.instance().getCueRaw(needsID)
				if (sound != null) {
					return@run sound.name
				}

				return@run needsID
			}, target.bounds.x + offset)

			return null
		}
	}

	// other stuff

	fun getNameKey(): String = "inspections.$name.name"
	fun getProperName(): String = Localization.get(getNameKey())

	fun getInfoKey(): String = "inspections.$name.info"
	fun getProperInfo(): String = Localization.get(getInfoKey(), *infoParams)

}

@FunctionalInterface
interface InspectionFunction {

	fun inspect(target: Entity, remix: Remix): InspectionType?

}

