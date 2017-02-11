package chrislo27.rhre.inspections

import chrislo27.rhre.registry.GameRegistry

object InspectionPostfix {
	@JvmStatic
	fun applyInspectionFunctions() {
		val registry = GameRegistry.instance()

		registry.getCueRaw("cropStomp/walk")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("cropStomp/stomp")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("cropStomp/molefling")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.get("cropStomp")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.get("exhibitionMatch")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.getCueRaw("frogHop/shake")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("frogHop/one")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("frogHop/two")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("frogHop/three")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCueRaw("frogHop/four")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("frogHop_shake")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("frogHop_yahoo")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("frogHop_spinitboys")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("frogHop_yeahyeahyeah")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("frogHop_2BeatCountIn")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("frogHop_4BeatCountIn")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())

		registry.getCueRaw("gleeClub/singLoop")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingAfter("gleeClub/singEnd"))
		registry.getCueRaw("gleeClub/singEnd")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingBefore("gleeClub/singLoop"))

		registry.getPatternRaw("karateMan_pot")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("karateMan_soccer ball")?.inspectionFunctions = listOf(
				InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("karateMan_roaster")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("karateMan_hit3")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.getPatternRaw("karateMan_hit4")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.getPatternRaw("karateMan_kick")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("karateMan_combo")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("karateMan_offbeatpot")?.inspectionFunctions = listOf(
				InspectionType.InspFuncNotOffbeat())

		registry.get("microRow")?.patterns?.forEach {
			it.inspectionFunctions = if (it.id != "microRow_double") listOf(InspectionType.InspFuncNotOnbeat()) else listOf(InspectionType.InspFuncNotOffbeat())
		}

		registry.getCueRaw("moaiDooWop/ooo1")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingAfter("moaiDooWop/wop1"))
		registry.getCueRaw("moaiDooWop/wop1")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingBefore("moaiDooWop/ooo1"))
		registry.getCueRaw("moaiDooWop/ooo2")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingAfter("moaiDooWop/wop2"))
		registry.getCueRaw("moaiDooWop/wop2")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingBefore("moaiDooWop/ooo2"))

		registry.get("rhythmRally")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.get("ringside")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.get("screwbotFactory")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.get("spaceSoccer")?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.getPatternRaw("tapTroupe_step")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())

		registry.getPatternRaw("lockstep_onbeat")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPatternRaw("lockstep_offbeat")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())

	}
}
