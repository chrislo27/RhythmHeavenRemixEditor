package io.github.chrislo27.rhre3.playalong


data class ControllerMapping(val inUse: Boolean, val name: String, val mapping: PlayalongControls = PlayalongControls.INVALID.copy()) {

    companion object {
        val INVALID = ControllerMapping(false, "<null>")
    }

    fun deepCopy(inUse: Boolean = this.inUse, name: String = this.name,
                 mapping: PlayalongControls = this.mapping.copy()): ControllerMapping =
            ControllerMapping(inUse, name, mapping)

}