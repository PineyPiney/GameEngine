package com.pineypiney.game_engine.util.extension_functions

import glm_.i
import glm_.pow
import glm_.shl
import glm_.shr
import kotlin.math.*

const val PIF = PI.toFloat()

infix fun UShort.shl(other: Int): UShort{
	return (toShort() shl other).toUShort()
}

infix fun UShort.shr(other: Int): UShort{
	return (toShort() shr other).toUShort()
}

/**
 * Round the double to [places] decimal places
 *
 * @param places The number of decimal places to round to
 *
 * @return The rounded value of the double
 */
fun Double.round(places: Int): Double {
	val mult = 10.0.pow(places)
	return (mult * this).roundToInt() / mult
}

/**
 * Round the float to [places] decimal places
 *
 * @param places The number of decimal places to round to
 *
 * @return The rounded value of the float
 */
fun Float.round(places: Int): Float {
	val mult = 10f.pow(places)
	return try{
		(mult * this).roundToInt() / mult
	}
	catch (_: IllegalArgumentException){
		this
	}
}

/**
 * Check if [this] is larger than [min] but not by more than [size]
 *
 * @param min The minimum value the value can be
 * @param size The range the value can fall within, starting at [min]
 *
 * @return If [this] is within these criteria
 */
@Suppress("SENSELESS_COMPARISON")
fun Float.isWithin(min: Float, size: Float): Boolean {
	return min < this && this < min + size
}

/**
 * Check if [this] is larger than [min] and smaller than [max]
 *
 * @param min The minimum value the value can be
 * @param max The maximum value the value can be
 *
 * @return If [this] is within these criteria
 */
@Suppress("SENSELESS_COMPARISON")
fun Float.isBetween(min: Float, max: Float): Boolean {
	return min < this && this < max
}
@Suppress("SENSELESS_COMPARISON")
fun Int.isBetween(min: Int, max: Int): Boolean {
	return min < this && this < max
}

// Interpolation Functions

/**
 * Linearly interpolate between [this] and [next] by [delta]
 *
 * @param next The return value when [delta] is 1
 * @param delta The value of interpolation. When 0 [this] is returned, when 1 [next] is returned
 *
 * @return The interpolated value
 */
fun Double.lerp(next: Double, delta: Double): Double {
	return this + ((next - this) * delta)
}

// Interpolate floats, assuming they are between 0 and 1
/**
 * Sin interpolate between 0 and 1, using [this] as delta
 */
fun Float.serp(): Float = 0.5f + 0.5f * sin((this - 0.5f) * PIF)

/**
 * Exponentially interpolate between 0 and 1, using [this] as delta
 *
 * @param [exponent] The exponent to interpolate with. A greater value will mean a steeper curve nearer 0.5
 */
@Suppress("SENSELESS_COMPARISON")
fun Float.eerp(exponent: Int): Float =
	if (this < 0.5f) {
		0.5f * (this * 2).pow(exponent)
	} else {
		1f - 0.5f * ((2f - (2 * this)).pow(exponent))
	}

/**
 * Quadratically interpolate between 0 and 1, using [this] as delta
 */
fun Float.querp(): Float = eerp(2)

/**
 * Cubically interpolate between 0 and 1, using [this] as delta
 */
fun Float.cerp(): Float = eerp(3)

// Simple Interpolation functions
fun Float.lerp(next: Float, delta: Float): Float {
	return this + ((next - this) * delta)
}

fun Float.serp(next: Float, delta: Float) = lerp(next, delta.serp())
fun Float.eerp(next: Float, delta: Float, exponent: Int) = lerp(next, delta.eerp(exponent))
fun Float.querp(next: Float, delta: Float) = lerp(next, delta.querp())
fun Float.cerp(next: Float, delta: Float) = lerp(next, delta.cerp())

/**
 * Used to lerp between the shortest angle between the two points.
 * This stops lerping from rotating multiple times around a circle if angles have
 * added up in calculations, and also makes sure it rotates the most efficient way
 * around a circle, i.e. 120 clockwise rather than 240 anticlockwise.
 *
 * @param next The angle being lerped to from this
 * @param delta proportion of time between this and [next], ranging from 0 to 1
 *
 * @return A new float that represents the angle between this and [next]
 * given the smallest angle between them
 */
fun Float.lerpAngle(next: Float, delta: Float): Float {

	val angleLast = wrap(0f, 2 * PIF)
	val angleNext = next.wrap(angleLast - PIF, angleLast + PIF)

	return angleLast.lerp(angleNext, delta)
}

fun Float.serpAngle(next: Float, delta: Float) = lerpAngle(next, delta.serp())
fun Float.eerpAngle(next: Float, delta: Float, exponent: Int) = lerpAngle(next, delta.eerp(exponent))
fun Float.querpAngle(next: Float, delta: Float) = lerpAngle(next, delta.querp())
fun Float.cerpAngle(next: Float, delta: Float) = lerpAngle(next, delta.cerp())


// Interpolate floats, assuming they are between 0 and 1
fun Double.serp(): Double = 0.5 + 0.5 * sin((this - 0.5) * PI)
@Suppress("SENSELESS_COMPARISON")
fun Double.eerp(exponent: Int): Double =
	if (this < 0.5) {
		0.5 * (this * 2).pow(exponent)
	} else {
		1.0 - 0.5 * ((2.0 - (2 * this)).pow(exponent))
	}

fun Double.querp(): Double = eerp(2)
fun Double.cerp(): Double = eerp(3)

/**
 * Returns the position [this] would be on a circle that starts at [min] and ends at [max]
 */
fun Double.wrap(min: Double, max: Double): Double {
	val range = abs(max - min)
	var rem = (this - min) % range
	if (rem < 0) rem += range
	return min + rem
}

/**
 * Returns the position [this] would be on a circle that starts at [min] and ends at [max]
 */
fun Float.wrap(min: Float, max: Float): Float {
	val range = abs(max - min)
	var rem = (this - min) % range
	if (rem < 0) rem += range
	return min + rem
}

/**
 * Returns the position [this] would be on a circle that starts at [min] and ends at [max]
 */
fun Int.wrap(min: Int, max: Int): Int {
	val ran = abs(max - min) + 1
	var rem = (this - min) % ran
	if (rem < 0) rem += ran
	return min + rem
}

infix fun Int.getRGBAValue(shift: Int) = ((this shr (shift * 8)) and 255) * 0.003921569f

/**
 * Return the value with the smallest absolute value
 */
fun absMinOf(a: Float, vararg other: Float): Float {
	return (other + a).minByOrNull { abs(it) } ?: a
}

// Yes I know Strings aren't primitive types, STFU

/**
 * Replace all whitespaces with [replace]
 */
fun CharSequence.replaceWhiteSpaces(replace: String = ""): String {
	return replace(Regex("\\s+"), replace)
}

fun CharSequence.sumOf(selector: (Char) -> Float): Float {
	var sum = 0f
	for (char in this) {
		sum += selector(char)
	}
	return sum
}

fun <E> CharSequence.reduce(initial: E, operation: (acc: E, it: Char) -> E): E {
	var accumulator = initial
	for (char in this) {
		accumulator = operation(accumulator, char)
	}
	return accumulator
}

operator fun String.get(start: Int, end: Int): String = substring(start, end)
operator fun String.get(range: IntRange): String = substring(range)

fun String.capitalise(): String{
	val s = toCharArray()
	if(s[0].isLowerCase()) s[0] = Char(s[0].i - 32)
	var lim = s.size
	for(c in 1..lim - 2){
		if(s[c] == '.' && s[c+1].isLowerCase()) s[c+1] = Char(s[c+1].i - 32)
		else if(s[c] == '_' && s[c+1].isLowerCase()){
			s[c] = Char(s[c+1].i - 32)
			s.copyInto(s, c, c+1)
			lim--
		}
	}
	return String(s, 0, lim)
}

//operator fun UByte.compareTo(other: Ubyte) = toInt() compareTo other.toInt()
//operator fun Ubyte.compareTo(other: UByte) = toInt() compareTo other.toInt()
