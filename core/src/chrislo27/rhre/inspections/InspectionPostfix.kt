package chrislo27.rhre.inspections

import chrislo27.rhre.registry.GameRegistry

object InspectionPostfix {
	@JvmStatic
	fun applyInspectionFunctions() {
		val registry = GameRegistry.instance()

		registry.getCueRaw("cropStomp/walk")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("cropStomp/stomp")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("cropStomp/molefling")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.getCueRaw("cropStomp/pick1")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat(),
																			InspectionType.InspFuncMissing(0.5f,
																										   "cropStomp/pick2"))
		registry.getCueRaw("cropStomp/pick2")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat(),
																			InspectionType.InspFuncMissing(-0.5f,
																										   "cropStomp/pick1"))
		registry.get("cropStomp")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.getPatternRaw("lockstep_onbeat")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("lockstep_offbeat")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())

	}
}
