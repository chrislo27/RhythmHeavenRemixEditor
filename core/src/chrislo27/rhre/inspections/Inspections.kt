package chrislo27.rhre.inspections

import chrislo27.rhre.track.Remix


class Inspections(val remix: Remix) {

	val inspections: MutableList<InspectionType> = mutableListOf()
	var lastRefreshDuration: Double = 0.0
		private set

	/**
	 * @return millisecond duration to inspect
	 */
	fun refresh(): Double {
		val nanoTime = System.nanoTime()

		inspections.clear()

		remix.entities
				.filter {
					it.inspectionFunctions != null
				}
				.forEach { entity ->
					entity.inspectionFunctions.forEach {
						val result = it.inspect(entity, remix)
						if (result != null) inspections.add(result)
					}
				}

		synchronized(lastRefreshDuration) {
			lastRefreshDuration = (System.nanoTime() - nanoTime) / 1000000.0
			return lastRefreshDuration
		}
	}

}