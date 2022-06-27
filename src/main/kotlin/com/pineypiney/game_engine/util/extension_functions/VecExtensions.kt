package com.pineypiney.game_engine.util.extension_functions

import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * The distance between [this] and [other]
 */
fun Vec2.dist(other: Vec2): Float = sqrt(this dot other)

/**
 * Coerce the x and y values of [this] between the x and y values of [low] and [high]
 *
 * @param low The minimum x and y values
 * @param high The maximum x and y values
 */
fun Vec2.coerceIn(low: Vec2, high: Vec2): Vec2 = Vec2(this.x.coerceIn(low.x, high.x), this.y.coerceIn(low.y, high.y))

/**
 * coerce [this] between -[mag] and [mag]
 */
fun Vec2.coerceIn(mag: Vec2): Vec2 = this.coerceIn(-mag, mag)

/**
 * Coerce the xyz values of [this] between the xyz values of [low] and [high]
 *
 * @param low The minimum xyz values
 * @param high The maximum xyz values
 */
fun Vec3.coerceIn(low: Vec3, high: Vec3): Vec3 = Vec3(this.x.coerceIn(min(low.x, high.x), max(low.x, high.x)), this.y.coerceIn(min(low.y, high.y), max(low.y, high.y)), this.z.coerceIn(min(low.z, high.z), max(low.z, high.z)))

/**
 * Check if [this] is within a box of dimensions [size] starting at [origin]
 */
fun Vec2.isWithin(origin: Vec2, size: Vec2): Boolean{
    return this.x.isWithin(origin.x, size.x) &&
            this.y.isWithin(origin.y, size.y)
}

/**
 * Check if the xy values of [this] are larger than those of [bl] and smaller than those of [tr]
 */
fun Vec2.isBetween(bl: Vec2, tr: Vec2): Boolean{
    return this.x.isBetween(bl.x, tr.x) &&
            this.y.isBetween(bl.y, tr.y)
}


fun Vec2.round(round: Float): Vec2 {
    this.x =
        if(this.x >= 0) this.x - this.x.mod(round)
        else this.x + round - this.x.mod(round)
    this.y =
        if(this.y >= 0 ) this.y - this.y.mod(round)
        else this.y + round - this.y.mod(round)

    return this
}

fun Vec2.lerp(next: Vec2, delta: Float) = Vec2(this.x.lerp(next.x, delta), this.y.lerp(next.y, delta))
fun Vec2.serp(next: Vec2, delta: Float) = Vec2(this.x.serp(next.x, delta), this.y.serp(next.y, delta))
fun Vec2.eerp(next: Vec2, delta: Float, exponent: Int) = Vec2(this.x.eerp(next.x, delta, exponent), this.y.eerp(next.y, delta, exponent))
fun Vec2.querp(next: Vec2, delta: Float) = Vec2(this.x.querp(next.x, delta), this.y.querp(next.y, delta))
fun Vec2.cerp(next: Vec2, delta: Float) = Vec2(this.x.cerp(next.x, delta), this.y.cerp(next.y, delta))


fun Vec3.transform(m: Mat4): Vec3 {
    return Vec3(m * Vec4(this))
}

fun Vec2.roundedString(places: Int) = arrayOf("${x.round(places)}", "${y.round(places)}")
fun Vec3.roundedString(places: Int) = arrayOf("${x.round(places)}", "${y.round(places)}", "${z.round(places)}")

fun Vec2.copy() = Vec2(ofs, array.clone())
fun Vec3.copy() = Vec3(ofs, array.clone())

