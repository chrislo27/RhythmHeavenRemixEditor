package io.github.chrislo27.rhre3.sfxdb


enum class BaseBpmRules(val id: String) {
    
    ALWAYS("always"),
    NO_TIME_STRETCH("noTimeStretch"),
    ONLY_TIME_STRETCH("onlyTimeStretch");
    
    /*
    Grid of scenarios:
    TS = time stretching
    
            | with TS    | without TS
    --------+------------+---------------
    ALWAYS  | do TS      | do rate change
    --------+------------+---------------
    NO_TS   | do nothing | do rate change
    --------+------------+---------------
    ONLY_TS | do TS      | do nothing
    
     */
    
    companion object {
        val VALUES: List<BaseBpmRules> = values().toList()
        val MAP: Map<String, BaseBpmRules> = VALUES.associateBy { it.id }
    }
    
}