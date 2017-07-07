package chrislo27.rhre.inspections.impl

import chrislo27.rhre.registry.Game
import chrislo27.rhre.track.Remix
import java.util.*

class InspTabSeries : GameListingTab() {
    override val name: String = "inspections.series.title"
    override val noneText: String = "inspections.series.none"

    override fun populateMap(remix: Remix, list: List<Game>): LinkedHashMap<String, List<Game>> {
        return list
                .groupBy { it ->
                    return@groupBy it.series
                }
                .mapKeysTo(linkedMapOf()) { entry ->
                    "series." + entry.key.name
                }
    }
}
