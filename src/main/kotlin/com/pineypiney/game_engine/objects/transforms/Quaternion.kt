package com.pineypiney.game_engine.objects.transforms

import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kotlin.math.*

class Quaternion(val r: Float, val i: Float, val j: Float, val k: Float) {

    constructor(vec: Vec4): this(vec.x, vec.y, vec.z, vec.w)

    constructor(eulerAngles: Vec3): this(eulerToValues(eulerAngles))

    // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles#Quaternion_to_Euler_angles_(in_3-2-1_sequence)_conversion
    fun toEulerAngles(): Vec3 {
        val x = atan2(2 * ((r * i) + (j * k)), 1 - (2 * ((i * i) + (j * j))))
        val a = 2 * ((r * j) - (i * k))
        val y = -PI /2 + (2 * atan2(sqrt(1 + a), sqrt(1 - a)))
        val z = atan2(2 * ((r * k) + (i * j)), 1 - (2 * ((j * j) + (k * k))))
        return Vec3(x, y, z)
    }

    infix operator fun plus(q: Quaternion): Quaternion{
        return Quaternion(r + q.r, i + q.i, j + q.j, k + q.k)
    }

    // https://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/arithmetic/index.htm
    infix operator fun times(q: Quaternion): Quaternion{
        val nr = r*q.r - i*q.i - j*q.j - k*q.k
        val ni = i*q.r + r*q.i + j*q.k- k*q.j
        val nj = r*q.j - i*q.k+ j*q.r + k*q.i
        val nk = r*q.k + i*q.j - j*q.i + k*q.r
        return Quaternion(nr, ni, nj, nk)
    }

    infix operator fun times(f: Float): Quaternion{
        return Quaternion(r * f, i*f, j*f, k*f)
    }

    fun size(): Float{
        return sqrt(size2())
    }

    fun size2(): Float{
        return (r*r)+(i*i)+(j*j)+(k*k)
    }

    fun conjugate(): Quaternion{
        return Quaternion(r, -i, -j, -k)
    }

    fun normalize(): Quaternion{
        return this * (1 / size())
    }

    fun slerp(other: Quaternion, delta: Float): Quaternion{
        return this * (conjugate() * other).pow(delta)
    }

    // https://math.stackexchange.com/a/939288
    infix fun pow(power: Float): Quaternion{
        return (ln() * power).exp().normalize()
    }

    fun exp(): Quaternion{
        val v = Vec3(i, j, k)
        val vs = v.length()
        val er = exp(r)
        val s = if (vs >= 0.00001f) er * sin(vs) / vs else 0f

        return Quaternion(er * cos(vs), i*s, j*s, k*s)
    }

    fun ln(): Quaternion{
        val v = Vec3(i, j, k)
        val ijk = v.length2()
        val vs = sqrt(ijk)
        val t = if (vs > 0.00001f) atan2(vs, this.r) / vs else 0f
        return Quaternion(0.5f * ln((this.r * this.r) + ijk), i*t, j*t, k*t)
    }

    companion object{
        // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles#Euler_angles_(in_3-2-1_sequence)_to_quaternion_conversion
        fun eulerToValues(euler: Vec3): Vec4{
            val cr: Double = cos(euler.x * 0.5)
            val sr: Double = sin(euler.x * 0.5)
            val cp: Double = cos(euler.y * 0.5)
            val sp: Double = sin(euler.y * 0.5)
            val cy: Double = cos(euler.z * 0.5)
            val sy: Double = sin(euler.z * 0.5)

            return Vec4(cr * cp * cy + sr * sp * sy ,sr * cp * cy - cr * sp * sy ,cr * sp * cy + sr * cp * sy ,cr * cp * sy - sr * sp * cy).normalize()
        }
    }
}