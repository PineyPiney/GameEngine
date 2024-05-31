package com.pineypiney.game_engine.util.extension_functions

import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.*
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec1.Vec1Vars
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
    return if (y == 0f) (if (x > 0) PIF * .5f else PIF * 1.5f)
            else (atan(x / y) + (if (y < 0) PIF else 0f)).wrap(0f, PIF * 2)
}

fun Vec2.angleBetween(other: Vec2): Float{
    return (angle() - other.angle()).wrap(-PIF, PIF)
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

fun Vec3.lerp(next: Vec3, delta: Float) = erp(next, delta, Float::lerp)
fun Vec3.serp(next: Vec3, delta: Float) = erp(next, delta, Float::serp)
fun Vec3.eerp(next: Vec3, delta: Float, exponent: Int) = erp(next, delta) { n, d -> eerp(n, d, exponent) }
fun Vec3.querp(next: Vec3, delta: Float) = erp(next, delta, Float::querp)
fun Vec3.cerp(next: Vec3, delta: Float) = erp(next, delta, Float::cerp)
fun Vec3.lerpAngle(next: Vec3, delta: Float) = erp(next, delta, Float::lerpAngle)
fun Vec3.serpAngle(next: Vec3, delta: Float) = erp(next, delta, Float::serpAngle)
fun Vec3.eerpAngle(next: Vec3, delta: Float, exponent: Int) = erp(next, delta) { n, d -> eerpAngle(n, d, exponent) }
fun Vec3.querpAngle(next: Vec3, delta: Float) = erp(next, delta, Float::querpAngle)
fun Vec3.cerpAngle(next: Vec3, delta: Float) = erp(next, delta, Float::cerpAngle)
fun Vec3.erp(next: Vec3, delta: Float, func: Float.(Float, Float) -> Float) = Vec3(x.func(next.x, delta), y.func(next.y, delta), z.func(next.z, delta))

fun Vec2.roundedString(places: Int) = arrayOf("${x.round(places)}", "${y.round(places)}")
fun Vec3.roundedString(places: Int) = arrayOf("${x.round(places)}", "${y.round(places)}", "${z.round(places)}")

fun Vec3.toHex(): Int = ((x * 255).i shl 16) and ((y * 255).i shl 8) and ((z * 255).i)
fun Vec4.toHex(): Int = ((x * 255).i shl 24) and ((y * 255).i shl 16) and ((z * 255).i shl 8) and ((w * 255).i)

fun Vec2.copy() = Vec2(0, floatArrayOf(x, y))
fun Vec3.copy() = Vec3(0, floatArrayOf(x, y, z))

fun Vec2.Companion.fromAngle(angle: Float, length: Float = 1f): Vec2{
    return Vec2(length * sin(angle), length * cos(angle))
}

fun Vec3.Companion.fromHex(num: Int): Vec3{
    return Vec3(num getRGBAValue 2, num getRGBAValue 1, num getRGBAValue 0)
}

fun Vec4.Companion.fromHex(num: Int, alpha: Float = 1f): Vec4{
    return Vec4(num getRGBAValue 2, num getRGBAValue 1, num getRGBAValue 0, alpha)
}

fun Vec4.Companion.fromHex(num: Int, alpha: Int): Vec4{
    return Vec4(num getRGBAValue 2, num getRGBAValue 1, num getRGBAValue 0, alpha * 0.003921569f)
}

fun Vec3.rotate(rotation: Vec3): Vec3{
    return Vec3(I.rotate(rotation) * Vec4(this, 1))
}

fun Vec3.rotate(rotation: Quat): Vec3{
    return Vec3(rotation.toMat4() * Vec4(this, 1))
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

fun Mat4.getTranslation(): Vec3{
    return Vec3(this[3])
}
fun Mat4.setTranslation(translation: Vec3, res: Mat4 = Mat4(this)): Mat4{
    translation.to(res.array, 12)
    return res
}

fun Mat4.getRotation(): Quat{
    return rotationComponent().toQuat()
}
fun Mat4.setRotation(rotation: Quat, res: Mat4 = Mat4(this)): Mat4{
    val rMat = rotation.toMat3()
    val scale = getScale()
    (rMat[0] * scale.x).to(res.array, 0)
    (rMat[1] * scale.y).to(res.array, 4)
    (rMat[2] * scale.z).to(res.array, 8)
    return res
}

fun Mat4.getScale(): Vec3{
    return Vec3(Vec3(this[0]).length(), Vec3(this[1]).length(), Vec3(this[2]).length())
}
fun Mat4.setScale(scale: Vec3, res: Mat4 = Mat4(this)): Mat4{
    val currentScale = getScale()
    val multiplier = scale / currentScale
    (Vec3(this[0]) * multiplier.x).to(res.array, 0)
    (Vec3(this[1]) * multiplier.y).to(res.array, 4)
    (Vec3(this[2]) * multiplier.z).to(res.array, 8)
    return res
}

fun Mat4.rotationComponent(): Mat4{
    val scale = getScale()
    return Mat4(
        this[0] / scale.x,
        this[1] / scale.y,
        this[2] / scale.z,
        Vec4(0f, 0f, 0f, 1f)
    )
}


fun Vec1Vars<Float>.toString(separator: String = ", ", serialise: Float.() -> String = Float::toString): String{
    var s = this[0].serialise()
    for(i in 1..3){
        try {
            s += separator + this[i].serialise()
        }
        catch (e: IndexOutOfBoundsException){
            break
        }
    }
    return s
}
fun Vec1Vars<Float>.toHexString(separator: String = ", "): String = toString(separator){ asIntBits.asHexString }

fun Quat.toString(separator: String = ", ", serialise: Float.() -> String = Float::toString): String{
    var s = this[0].serialise()
    for(i in 1..3){
        try {
            s += separator + this[i].serialise()
        }
        catch (e: IndexOutOfBoundsException){
            break
        }
    }
    return s
}

fun Vec2.Companion.fromString(s: String, trim: Boolean = true, parse: String.() -> Float = java.lang.Float::parseFloat): Vec2{
    val trimmed = if(trim) s.trim().removePrefix("Vec2{").removeSuffix("}") else s
    val (s1, s2) = trimmed.split(",")
    val f1 = s1.parse()
    val f2 = s2.parse()
    return Vec2(f1, f2)
}

/**
 *
 * @throws NumberFormatException if the string doesn't contain an integer value
 */
fun Vec2.Companion.fromHexString(s: String) = fromString(s){ intValue(16).bitsAsFloat }

fun Vec3.Companion.fromString(s: String, trim: Boolean = false, parse: String.() -> Float = java.lang.Float::parseFloat): Vec3{
    val trimmed = if(trim) s.trim().removePrefix("Vec3{").removeSuffix("}") else s
    return trimmed.split(",").map { if(trim) it.trim() else it }.let { Vec3(it[0].parse(), it[1].parse(), it[2].parse()) }
}

fun Vec3.Companion.fromHexString(s: String) = fromString(s){ intValue(16).bitsAsFloat }

fun Vec4.Companion.fromString(s: String, trim: Boolean = false, parse: String.() -> Float = java.lang.Float::parseFloat): Vec4{
    val trimmed = if(trim) s.trim().removePrefix("Vec4{").removeSuffix("}") else s
    return trimmed.split(",").let { Vec4(it[0].trim().parse(), it[1].trim().parse(), it[2].trim().parse(), it[3].trim().parse()) }
}

fun Quat.Companion.fromString(s: String, trim: Boolean = false, parse: String.() -> Float = java.lang.Float::parseFloat): Quat{
    val trimmed = if(trim) s.trim().removePrefix("Quat{").removeSuffix("}") else s
    return trimmed.split(",").let { Quat(it[0].trim().parse(), it[1].trim().parse(), it[2].trim().parse(), it[3].trim().parse()) }
}