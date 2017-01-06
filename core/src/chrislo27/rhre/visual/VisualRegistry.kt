package chrislo27.rhre.visual

import chrislo27.rhre.visual.impl.BouncyRoadRenderer

object VisualRegistry {

	val map: Map<String, Renderer> = mutableMapOf()

	init {
		map as MutableMap

		map.put("bouncyRoad", BouncyRoadRenderer())
	}

}
