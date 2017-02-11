package chrislo27.rhre.util;

/**
 * port of nvidias gameworks sample
 * https://github.com/NVIDIAGameWorks/OpenGLSamples/blob/master/samples/es3-kepler/HDR/HDRImages.h
 *
 * @author nvidia, bas
 * @version 1.0, 2015.03.05
 */
public final class HFloat {
	//~--- static fields --------------------------------------------------------

	/**
	 * -15 stored using a single precision bias of 127
	 */
	public final static int HALF_FLOAT_MIN_BIASED_EXP_AS_SINGLE_FP_EXP = 0x38000000;

	/**
	 * max exponent value in single precision that will be converted to Inf or Nan when stored as a half-float
	 */
	public final static int HALF_FLOAT_MAX_BIASED_EXP_AS_SINGLE_FP_EXP = 0x47800000;

	/**
	 * 255 is the max exponent biased value
	 */
	public final static int FLOAT_MAX_BIASED_EXP = (0xFF << 23);
	public final static int HALF_FLOAT_MAX_BIASED_EXP = (0x1F << 10);

	//~--- methods --------------------------------------------------------------

	/**
	 * @return float
	 */
	public static float overflow() {
		float f = 1.0e10f;

		for (int i = 0; i < 10; i++)
			f *= f;  // this will overflow before the for loop terminates

		return f;
	}

	/**
	 * @param hf short
	 * @return float
	 */
	public static float convertHFloatToFloat(final short hf) {
		return Float.intBitsToFloat(convertHFloatToFloatBits(hf));
	}

	/**
	 * @param f float
	 * @return short
	 */
	public static short convertFloatToHFloat(final float f) {
		return convertFloatBitsToHFloat(Float.floatToRawIntBits(f));
	}

	/**
	 * @param hf short
	 * @return int
	 */
	public static int convertHFloatToFloatBits(final short hf) {
		final int sign = (hf >> 15);
		int mantissa = (hf & ((1 << 10) - 1));
		int exp = (hf & HALF_FLOAT_MAX_BIASED_EXP);
		final int f;

		if (exp == HALF_FLOAT_MAX_BIASED_EXP) {

			/**
			 * we have a half-float NaN or Inf
			 * half-float NaNs will be converted to a single precision NaN
			 * half-float Infs will be converted to a single precision Inf
			 */
			exp = FLOAT_MAX_BIASED_EXP;

			if (mantissa != 0)
				mantissa = (1 << 23) - 1;  // set all bits to indicate a NaN
		} else if (exp == 0x0) {

			/** convert half-float zero/denorm to single precision value */
			if (mantissa != 0) {
				mantissa <<= 1;
				exp = HALF_FLOAT_MIN_BIASED_EXP_AS_SINGLE_FP_EXP;

				/** check for leading 1 in denorm mantissa */
				while ((mantissa & (1 << 10)) == 0) {

					/** for every leading 0, decrement single precision exponent by 1 and shift half-float mantissa
					 * value to the left */
					mantissa <<= 1;
					exp -= (1 << 23);
				}

				/** clamp the mantissa to 10-bits */
				mantissa &= ((1 << 10) - 1);

				/** shift left to generate single-precision mantissa of 23-bits */
				mantissa <<= 13;
			}
		} else {

			/** shift left to generate single-precision mantissa of 23-bits */
			mantissa <<= 13;

			/** generate single precision biased exponent value */
			exp = (exp << 13) + HALF_FLOAT_MIN_BIASED_EXP_AS_SINGLE_FP_EXP;
		}

		f = (sign << 31) | exp | mantissa;

		return f;
	}

	/**
	 * @param floatBits int
	 * @return short
	 */
	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static short convertFloatBitsToHFloat(final int floatBits) {
		final int s = (floatBits >> 16) & 0x00008000;
		int e = ((floatBits >> 23) & 0x000000ff) - (127 - 15);
		int m = floatBits & 0x007fffff;

		/** Now reassemble s, e and m into a half: */
		if (e <= 0) {
			if (e < -10) {

				/**
				 * E is less than -10. The absolute value of f is less than HALF_MIN (f may be a small normalized
				 * float, a denormalized float or a zero).
				 *
				 * We convert f to a half zero with the same sign as f.
				 */
				return (short) s;
			}

			/**
			 * E is between -10 and 0. F is a normalized float
			 * whose magnitude is less than HALF_NRM_MIN.
			 *
			 * We convert f to a denormalized half.
			 *
			 *
			 * Add an explicit leading 1 to the significand.
			 */
			m |= 0x00800000;

			/**
			 * Round to m to the nearest (10+e)-bit value (with e between
			 * -10 and 0); in case of a tie, round to the nearest even value.
			 *
			 * Rounding may cause the significand to overflow and make
			 * our number normalized. Because of the way a half's bits
			 * are laid out, we don't have to treat this case separately;
			 * the code below will handle it correctly.
			 */
			final int t = 14 - e;
			final int a = (1 << (t - 1)) - 1;
			final int b = (m >> t) & 1;

			m = (m + a + b) >> t;

			/** Assemble the half from s, e (zero) and m. */
			final int r = s | m;

			return (short) r;
		} else if (e == 0xff - (127 - 15)) {
			if (m == 0) {

				/**
				 * F is an infinity; convert f to a half infinity with the same sign as f.
				 */
				final int r = s | 0x7c00;

				return (short) r;
			} else {

				/**
				 * F is a NAN; we produce a half NAN that preserves
				 * the sign bit and the 10 leftmost bits of the
				 * significand of f, with one exception: If the 10
				 * leftmost bits are all zero, the NAN would turn
				 * into an infinity, so we have to set at least one
				 * bit in the significand.
				 */
				m >>= 13;

				final int r = s | 0x7c00 | m | ((m == 0) ? 1 : 0);

				return (short) r;
			}
		} else {

			/**
			 * E is greater than zero. F is a normalized float.
			 * We try to convert f to a normalized half.
			 *
			 * Round to m to the nearest 10-bit value. In case of
			 * a tie, round to the nearest even value.
			 */
			m += 0x00000fff + ((m >> 13) & 1);

			if ((m & 0x00800000) != 0) {
				m = 0;                     // overflow in significand,
				e += 1;                    // adjust exponent
			}

			/** Handle exponent overflow */
			if (e > 30) {
				overflow();                // Cause a hardware floating point overflow;

				final int r = s | 0x7c00;  // if this returns, the half becomes an infinity with the same sign as f.

				return (short) r;
			}

			/** Assemble the half from s, e and m. */
			final int r = s | (e << 10) | (m >> 13);

			return (short) r;
		}
	}
}
