package io.github.chrislo27.toolboks.version


/**
 * A simple semantic version class. Comparisons can be made which only use [major], [minor], and [patch].
 * There are special constants you may use in the [companion object][Companion].
 */
data class Version(val major: Int, val minor: Int, val patch: Int, val suffix: String = "") : Comparable<Version> {

    override fun compareTo(other: Version): Int {
        return numericalValue - other.numericalValue
    }

    val numericalValue: Int
    private val stringRepresentation: String by lazy {
        if (isUnknown) {
            "<unknown version>"
        } else {
            "v$major.$minor.$patch${if (!suffix.isBlank()) "-$suffix" else ""}"
        }
    }
    val isUnknown: Boolean

    companion object {

        val UNKNOWN: Version = Version(-1, -1, -1)
        val RETRIEVING: Version = Version(-2, -2, -2)

        const val MAX_PART_VALUE: Int = 0xFF
        val REGEX: Regex = "v(\\d+).(\\d+).(\\d+)(?:-(.+))?".toRegex()

//		init {
//			val snapshotRegex = "SNAPSHOT_(\\d\\d\\d\\d)(\\d\\d)(\\d\\d).".toRegex()
//			if (VERSION.suffix.matches(snapshotRegex)) {
//				val match = snapshotRegex.matchEntire(VERSION.suffix)!!
//				val now = LocalDate.now()
//
//				if (match.groupValues[1] != now.year.toString()) {
//					IllegalArgumentException("Current version $VERSION has the wrong date").printStackTrace()
//				} else if (match.groupValues[2].toInt() != now.month.value) {
//					IllegalArgumentException("Current version $VERSION has the wrong date").printStackTrace()
//				} else if (match.groupValues[3].toInt() != now.dayOfMonth) {
//					IllegalArgumentException("Current version $VERSION has the wrong date").printStackTrace()
//				}
//			}
//		}

        fun fromNumberOrNull(numerical: Int, suffix: String = ""): Version? {
            return Version((numerical ushr 16) and 0xFF, (numerical ushr 8) and 0xFF, numerical and 0xFF, suffix)
        }

        fun fromStringOrNull(thing: String): Version? {
            val match: MatchResult = REGEX.matchEntire(thing) ?:
                    return null

            try {
                return Version(Integer.parseInt(match.groupValues[1]), Integer.parseInt(match.groupValues[2]),
                               Integer.parseInt(match.groupValues[3]), match.groupValues.getOrNull(4) ?: "")
            } catch (e: Exception) {
                return null
            }
        }

        fun fromNumber(numerical: Int, suffix: String = ""): Version {
            return fromNumberOrNull(numerical, suffix) ?: throw IllegalArgumentException(
                    "Invalid arguments: $numerical, $suffix")
        }

        fun fromString(thing: String): Version {
            return fromStringOrNull(thing) ?: throw IllegalArgumentException("Invalid argument: $thing")
        }
    }

    init {
        if ((major == -1 || major == -2) && minor == major && patch == major) {
            isUnknown = true
        } else {
            isUnknown = false
            if (major !in 0..MAX_PART_VALUE || minor !in 0..MAX_PART_VALUE || patch !in 0..MAX_PART_VALUE) {
                throw IllegalArgumentException("Invalid version. The max part value is $MAX_PART_VALUE. $this")
            }
        }

        numericalValue = (major shl 16) or (minor shl 8) or (patch)
    }

    override fun toString(): String {
        return stringRepresentation
    }

    override fun equals(other: Any?): Boolean {
        if (other is Version) {
            if (other.numericalValue == numericalValue && other.suffix == suffix) {
                return true
            }
            return false
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + suffix.hashCode()
        return result
    }

}