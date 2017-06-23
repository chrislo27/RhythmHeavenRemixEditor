package chrislo27.rhre.version


class RHRE2Version(val major: Int, val minor: Int, val patch: Int, val suffix: String = "") : Comparable<RHRE2Version> {

	override fun compareTo(other: RHRE2Version): Int {
		return numericalValue - other.numericalValue
	}

	val numericalValue: Int

	companion object {
		const val MAX_PART_VALUE: Int = 255
		val VERSION: RHRE2Version = RHRE2Version(2, 16, 0, "SNAPSHOT")
	}

	init {
		if (major !in 0..MAX_PART_VALUE || minor !in 0..MAX_PART_VALUE || patch !in 0..MAX_PART_VALUE) {
			throw IllegalArgumentException("Invalid version. The max part value is $MAX_PART_VALUE. $this")
		}

		numericalValue = (major shl 16) or (minor shl 8) or (patch)
	}

	override fun toString(): String {
		return "v$major.$minor.$patch-$suffix"
	}

	override fun equals(other: Any?): Boolean {
		if (other is RHRE2Version) {
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