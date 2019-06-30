package io.github.chrislo27.rhre3.sfxdb


enum class Language(val code: String, val langName: String) {
    
    UNKNOWN("???", "UNKNOWN LANGUAGE CODE"), ENGLISH("en", "English"), JAPANESE("ja", "Japanese"), KOREAN("ko", "Korean"), 
    SPANISH("es", "Spanish"), FRENCH("fr", "French"), ITALIAN("it", "Italian"), GERMAN("de", "German");
    
    companion object {
        val VALUES: List<Language> = values().toList()
        val CODE_MAP: Map<String, Language> = VALUES.associateBy { it.code }
        
        fun getOrUnknown(code: String?): Language? {
            if (code == null) return null
            return CODE_MAP[code] ?: UNKNOWN
        }
    }
    
}