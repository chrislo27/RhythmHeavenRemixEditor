package chrislo27.rhre.inspections

import chrislo27.rhre.registry.GameRegistry

object InspectionPostfix {
	@JvmStatic
	fun applyInspectionFunctions() {
		val registry = GameRegistry

		registry.getCue("cropStomp/walk")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCue("cropStomp/stomp")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCue("cropStomp/molefling")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry["cropStomp"]?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry["exhibitionMatch"]?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.getCue("frogHop/shake")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCue("frogHop/one")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCue("frogHop/two")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCue("frogHop/three")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getCue("frogHop/four")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("frogHop_shake")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("frogHop_yahoo")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("frogHop_spinitboys")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("frogHop_yeahyeahyeah")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("frogHop_2BeatCountIn")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("frogHop_4BeatCountIn")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())

		registry.getCue("gleeClub/singLoop")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingAfter("gleeClub/singEnd"))
		registry.getCue("gleeClub/singEnd")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingBefore("gleeClub/singLoop"))

		registry.getPattern("karateMan_pot")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("karateMan_soccer ball")?.inspectionFunctions = listOf(
				InspectionType.InspFuncNotOnbeat())
		registry.getPattern("karateMan_roaster")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("karateMan_hit3")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.getPattern("karateMan_hit4")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.getPattern("karateMan_kick")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("karateMan_combo")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("karateMan_offbeatpot")?.inspectionFunctions = listOf(
				InspectionType.InspFuncNotOffbeat())

		registry["microRow"]?.patterns?.forEach {
			it.inspectionFunctions = if (it.id != "microRow_double") listOf(InspectionType.InspFuncNotOnbeat()) else listOf(InspectionType.InspFuncNotOffbeat())
		}

		registry["munchyMonk"]?.patterns?.forEach {
			it.inspectionFunctions = if (it.id != "munchyMonk_trytwo") listOf(InspectionType.InspFuncNotOnbeat()) else listOf(InspectionType.InspFuncNotOffbeat())
		}
		registry["munchyMonkEs"]?.patterns?.forEach {
			it.inspectionFunctions = if (it.id != "munchyMonkEs_trytwo") listOf(InspectionType.InspFuncNotOnbeat()) else listOf(InspectionType.InspFuncNotOffbeat())
		}
		registry["munchyMonkJa"]?.patterns?.forEach {
			it.inspectionFunctions = if (it.id != "munchyMonkJa_trytwo") listOf(InspectionType.InspFuncNotOnbeat()) else listOf(InspectionType.InspFuncNotOffbeat())
		}

		registry.getCue("moaiDooWop/ooo1")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingAfter("moaiDooWop/wop1"))
		registry.getCue("moaiDooWop/wop1")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingBefore("moaiDooWop/ooo1"))
		registry.getCue("moaiDooWop/ooo2")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingAfter("moaiDooWop/wop2"))
		registry.getCue("moaiDooWop/wop2")?.inspectionFunctions = listOf(
				InspectionType.InspFuncMissingBefore("moaiDooWop/ooo2"))

		registry["rhythmRally"]?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry["ringside"]?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry["screwbotFactory"]?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry["spaceSoccer"]?.patterns?.forEach {
			it.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		}

		registry.getPattern("tapTroupe_step")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())

		registry.getPattern("lockstep_onbeat")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("lockstep_offbeat")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOffbeat())
		registry.getPattern("lockstep_onToOff")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())
		registry.getPattern("lockstep_offToOn")?.inspectionFunctions = listOf(InspectionType.InspFuncNotOnbeat())

	}
}
