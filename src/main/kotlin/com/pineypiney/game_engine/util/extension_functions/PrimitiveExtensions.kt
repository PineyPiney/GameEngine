package com.pineypiney.game_engine.util.extension_functions

import glm_.f
import glm_.pow
import kotlin.math.*


fun Double.round(places: Int): Double{
    val mult = 10.0.pow(places)
    return (mult * this).roundToInt()/mult
}

fun Float.round(places: Int): Float{
    val mult = 10f.pow(places)
    return (mult * this).roundToInt()/mult
}

fun Float.isWithin(left: Float, size: Float): Boolean{
    return left < this && this < left + size
}

fun Float.isBetween(left: Float, right: Float): Boolean{
    return left < this && this < right
}


// Interpolation Functions

fun Double.lerp(next: Double, delta: Double): Double{
    return this + ((next - this) * delta)
}

// Interpolate floats, assuming they are between 0 and 1
fun Float.serp(): Float = 0.5f + 0.5f * sin((this - 0.5) * PI).f
fun Float.eerp(exponent: Int): Float =
    if(this < 0.5){
        0.5 * pow(exponent) / 0.5.pow(exponent)
    }
    else{
        1 - 0.5 * ((1f - this).pow(exponent) / 0.5.pow(exponent))
    }.f
fun Float.querp(): Float = eerp(2)
fun Float.cerp(): Float = eerp(3)

// Simple Interpolation functions
fun Float.lerp(next: Float, delta: Float): Float{
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
fun Float.lerpAngle(next: Float, delta: Float): Float{

    val angleLast = wrap(0f, 2 * PI.f)
    val angleNext = next.wrap(angleLast - PI.f, angleLast + PI.f)

    return angleLast.lerp(angleNext, delta)
}
fun Float.serpAngle(next: Float, delta: Float) = lerpAngle(next, delta.serp())
fun Float.eerpAngle(next: Float, delta: Float, exponent: Int) = lerpAngle(next, delta.eerp(exponent))
fun Float.querpAngle(next: Float, delta: Float) = lerpAngle(next, delta.querp())
fun Float.cerpAngle(next: Float, delta: Float) = lerpAngle(next, delta.cerp())


// Interpolate floats, assuming they are between 0 and 1
fun Double.serp(): Double = 0.5 + 0.5 * sin((this - 0.5) * PI)
fun Double.eerp(exponent: Int): Double =
    if(this < 0.5){
        0.5 * pow(exponent) / 0.5.pow(exponent)
    }
    else{
        1 - 0.5 * ((1.0 - this).pow(exponent) / 0.5.pow(exponent))
    }
fun Double.querp(): Double = eerp(2)
fun Double.cerp(): Double = eerp(3)

fun Float.wrap(min: Float, max: Float): Float{
    val d = abs(max- min)
    var x = this
    while(x < min) x += d
    while(x > max) x -= d
    return x
}

fun absMinOf(a: Float, vararg other: Float): Float{
    var min = a
    other.forEach {
        if (abs(it) < abs(min)) min = it
    }
    return min
}

// Yes I know Strings aren't primitive types, STFU

fun String.replaceWhiteSpaces(replace: String = ""): String{
    return replace("\\s+", replace)
}
