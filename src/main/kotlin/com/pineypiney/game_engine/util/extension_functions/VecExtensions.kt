package com.pineypiney.game_engine.util.extension_functions

import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun Vec2.Companion.of(array: FloatArray) = Vec2(array.expand(2))
fun Vec3.Companion.of(array: FloatArray) = Vec3(array.expand(3))
fun Vec4.Companion.of(array: FloatArray) = Vec4(array.expand(4))

fun Vec2.Companion.of(array: Array<Float>) = Vec2.of(array.toFloatArray())
fun Vec3.Companion.of(array: Array<Float>) = Vec3.of(array.toFloatArray())
fun Vec4.Companion.of(array: Array<Float>) = Vec4.of(array.toFloatArray())


fun Vec3.positive() : Vec3 {
    this.x = this.x.coerceAtLeast(0f)
    this.y = this.y.coerceAtLeast(0f)
    this.z = this.z.coerceAtLeast(0f)

    return this
}


fun Vec2.sqLength(): Float = this.x*this.x + this.y*this.y
fun Vec3.sqLength(): Float = this.x*this.x + this.y*this.y + this.z*this.z

fun Vec2.sqDist(other: Vec2): Float = (this - other).sqLength()
fun Vec3.sqDist(other: Vec3): Float = (this - other).sqLength()

fun Vec2.dist(other: Vec2): Float = sqrt(this.sqDist(other))


fun Vec2.coerceIn(low: Vec2, high: Vec2): Vec2 = Vec2(this.x.coerceIn(low.x, high.x), this.y.coerceIn(low.y, high.y))
fun Vec2.coerceIn(mag: Vec2): Vec2 = this.coerceIn(-mag, mag)

fun Vec3.coerceIn(low: Vec3, high: Vec3): Vec3 = Vec3(this.x.coerceIn(min(low.x, high.x), max(low.x, high.x)), this.y.coerceIn(min(low.y, high.y), max(low.y, high.y)), this.z.coerceIn(min(low.z, high.z), max(low.z, high.z)))

fun Vec2.isWithin(origin: Vec2, size: Vec2): Boolean{
    return this.x.isWithin(origin.x, size.x) &&
            this.y.isWithin(origin.y, size.y)
}

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

fun Vec2.roundedString(places: Int) = "${x.round(places)}, ${y.round(places)}"
fun Vec3.roundedString(places: Int) = "${x.round(places)}, ${y.round(places)}, ${z.round(places)}"

fun Vec2.copy() = Vec2(x, y)
fun Vec3.copy() = Vec3(x, y, z)

