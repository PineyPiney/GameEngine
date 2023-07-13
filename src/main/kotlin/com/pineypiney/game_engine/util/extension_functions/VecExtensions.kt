package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.f
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import kotlin.math.*

/**
 * The distance between [this] and [other]
 */
fun Vec2.dist(other: Vec2): Float = (this - other).length()

/**
 * Angle of Vec2, in radians, going clockwise starting at (0, 1)
 */
fun Vec2.angle(): Float {
    return if (y == 0f) (if (x > 0) PI / 2 else PI * 3 / 2).f
            else (atan(x / y) + (if (y < 0) PI.f else 0f)).wrap(0f, PI.f * 2)
}

fun Vec2.angleBetween(other: Vec2): Float{
    return (angle() - other.angle()).wrap(-PI.f, PI.f)
}

/**
 * Function to return the normal of the Vector
 */
fun Vec2.normal(): Vec2 {
    return Vec2(y, -x)
}

fun Vec2i.length() = sqrt((x * x).f + (y * y))
fun Vec3i.length() = sqrt((x * x).f + (y * y) + (z * z))

/**
 * Coerce the x and y values of [this] between the x and y values of [low] and [high]
 *
 * @param low The minimum x and y values
 * @param high The maximum x and y values
 */
fun Vec2.coerceIn(low: Vec2, high: Vec2): Vec2 = Vec2(x.coerceIn(low.x, high.x), y.coerceIn(low.y, high.y))

/**
 * coerce [this] between -[mag] and [mag]
 */
fun Vec2.coerceIn(mag: Vec2): Vec2 = coerceIn(-mag, mag)

/**
 * Coerce the xyz values of [this] between the xyz values of [low] and [high]
 *
 * @param low The minimum xyz values
 * @param high The maximum xyz values
 */
fun Vec3.coerceIn(low: Vec3, high: Vec3): Vec3 = Vec3(x.coerceIn(min(low.x, high.x), max(low.x, high.x)), y.coerceIn(min(low.y, high.y), max(low.y, high.y)), z.coerceIn(min(low.z, high.z), max(low.z, high.z)))

/**
 * Check if [this] is within a box of dimensions [size] starting at [origin]
 */
fun Vec2.isWithin(origin: Vec2, size: Vec2): Boolean{
    return x.isWithin(origin.x, size.x) &&
            y.isWithin(origin.y, size.y)
}

/**
 * Check if the xy values of [this] are larger than those of [bl] and smaller than those of [tr]
 */
fun Vec2.isBetween(bl: Vec2, tr: Vec2): Boolean{
    return x.isBetween(bl.x, tr.x) &&
            y.isBetween(bl.y, tr.y)
}


fun Vec2.round(round: Float): Vec2 {
    x =
        if(x >= 0) x - x.mod(round)
        else x + round - x.mod(round)
    y =
        if(y >= 0 ) y - y.mod(round)
        else y + round - y.mod(round)

    return this
}

infix fun Vec2.projectOn(other: Vec2): Vec2{
    return other * (this dot other) / (other dot other)
}

infix fun Vec3.projectOn(other: Vec3): Vec3{
    return other * (this dot other) / (other dot other)
}

fun Vec2.lerp(next: Vec2, delta: Float) = Vec2(x.lerp(next.x, delta), y.lerp(next.y, delta))
fun Vec2.serp(next: Vec2, delta: Float) = Vec2(x.serp(next.x, delta), y.serp(next.y, delta))
fun Vec2.eerp(next: Vec2, delta: Float, exponent: Int) = Vec2(x.eerp(next.x, delta, exponent), y.eerp(next.y, delta, exponent))
fun Vec2.querp(next: Vec2, delta: Float) = Vec2(x.querp(next.x, delta), y.querp(next.y, delta))
fun Vec2.cerp(next: Vec2, delta: Float) = Vec2(x.cerp(next.x, delta), y.cerp(next.y, delta))


fun Vec3.transform(m: Mat4): Vec3 {
    return Vec3(m * Vec4(this))
}

fun Vec2.roundedString(places: Int) = arrayOf("${x.round(places)}", "${y.round(places)}")
fun Vec3.roundedString(places: Int) = arrayOf("${x.round(places)}", "${y.round(places)}", "${z.round(places)}")

fun Vec3.toHex(): Int = (x * 255).i shl 16 + (y * 255).i shl 8 + (z * 255).i
fun Vec4.toHex(): Int = (x * 255).i shl 24 + (y * 255).i shl 16 + (z * 255).i shl 8 + (w * 255).i

fun Vec2.copy() = Vec2(0, floatArrayOf(x, y))
fun Vec3.copy() = Vec3(0, floatArrayOf(x, y, z))

fun Vec2.Companion.fromAngle(angle: Float, length: Float = 1f): Vec2{
    return Vec2(length * sin(angle), length * cos(angle))
}
fun Vec2.Companion.fromMat4Translation(matrix: Mat4): Vec2{
    return Vec2(matrix[3, 0], matrix[3, 1])
}
fun Vec3.Companion.fromMat4Translation(matrix: Mat4): Vec3{
    return Vec3(matrix[3, 0], matrix[3, 1], matrix[3, 2])
}

fun Vec3.Companion.fromHex(num: Int): Vec3{
    return Vec3(num getRGBAValue 2, num getRGBAValue 1, num getRGBAValue 0)
}

fun Vec4.Companion.fromHex(num: Int, alpha: Float = 1f): Vec4{
    return Vec4(num getRGBAValue 2, num getRGBAValue 1, num getRGBAValue 0, alpha)
}

fun Vec4.Companion.fromHex(num: Int, alpha: Int): Vec4{
    return Vec4(num getRGBAValue 2, num getRGBAValue 1, num getRGBAValue 0, alpha.f / 255)
}

fun Vec3.rotate(rotation: Vec3): Vec3{
    return Vec3(I.rotate(rotation) * Vec4(this, 1))
}

infix fun Vec3.transformedBy(m: Mat4) = Vec3(m * Vec4(this, 1))

fun Mat4.translate(vec2: Vec2) = translate(Vec3(vec2, 0))
fun Mat4.rotate(angle: Float) = rotate(angle, normal)

fun Mat4.rotate(rotation: Vec3) = rotate(rotation.x, rotation.y, rotation.z)
fun Mat4.rotate(angleX: Float, angleY: Float, angleZ: Float): Mat4{
    return rotate(angleX, Vec3(1, 0, 0))
        .rotate(angleY, Vec3(0, 1, 0))
        .rotate(angleZ, Vec3(0, 0, 1))
}
fun Mat4.scale(vec2: Vec2) = scale(Vec3(vec2, 1))
