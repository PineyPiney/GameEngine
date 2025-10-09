package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.extension_functions.*
import glm_.asLongBits
import glm_.getInt
import glm_.quat.Quat
import glm_.quat.QuatD
import glm_.quat.QuatT
import glm_.shl
import glm_.vec2.*
import glm_.vec3.*
import glm_.vec4.*
import unsigned.toUint
import kotlin.math.min

@Suppress("UNUSED")
class ByteData {

	companion object {

		inline fun <T> bytesToNum(bytes: ByteArray, offset: Int, length: Int, bigEndian: Boolean, crossinline converter: UByte.() -> T, crossinline shl: (T, Int) -> T, or: Iterable<Int>.((Int) -> T) -> T): T{
//			or.invoke()
			return if(bigEndian) (offset..<offset + length).or { shl(bytes[it].toUByte().converter(), (8 * (length - (it + 1)))) }
			else (offset..<offset + length).or { shl(bytes[it].toUByte().converter(), (8 * it)) }
		}


		fun short2Bytes(i: Int, bytes: Int = 2): ByteArray {
			val numBytes = min(bytes, 2)
			return ByteArray(numBytes) { x -> ((i shr ((numBytes - (x + 1)) * 8)) and 255).toByte() }
		}

		fun short2String(i: Int, chars: Int = 2): String {
			val bytes = min(chars, 2)
			val a = CharArray(bytes) { x -> ((i shr ((bytes - (x + 1)) * 8)) and 255).toChar() }
			return String(a)
		}
		fun bytes2Short(bytes: ByteArray, offset: Int = 0, length: Int = minOf(2, bytes.size - offset), bigEndian: Boolean = true): Short{
			return bytesToNum(bytes, offset, length, bigEndian, UByte::toShort, Short::shl, Iterable<Int>::orOfShort)
		}
		fun string2Short(s: String, length: Int = minOf(2, s.length), bigEndian: Boolean = true): Short{
			return if(bigEndian) (0..<length).orOfShort { s[it].code.toShort() shl (8 * (length - (it + 1))) }
			else (0..<length).orOfShort { s[it].code.toShort() shl (8 * it) }
		}
		fun ushort2Bytes(i: UShort, bytes: Int = 2): ByteArray {
			val numBytes = min(bytes, 2)
			return ByteArray(numBytes) { x -> ((i shr ((numBytes - (x + 1)) * 8)) and 255u).toByte() }
		}

		fun ushort2String(i: UShort, chars: Int = 2): String {
			val bytes = min(chars, 2)
			val a = CharArray(bytes) { x -> ((i shr ((bytes - (x + 1)) * 8)) and 255u).toInt().toChar() }
			return String(a)
		}
		fun bytes2UShort(bytes: ByteArray, offset: Int = 0, length: Int = minOf(2, bytes.size - offset), bigEndian: Boolean = true): UShort{
			return bytesToNum(bytes, offset, length, bigEndian, UByte::toUShort, UShort::shl, Iterable<Int>::orOfUShort)
		}
		fun string2UShort(s: String, length: Int = minOf(2, s.length), bigEndian: Boolean = true): UShort{
			return if(bigEndian) (0..<length).orOfUShort { s[it].code.toUShort() shl (8 * (length - 1 - it)) }
			else (0..<length).orOfUShort { s[it].code.toUShort() shl (8 * it) }
		}
		

		fun int2Bytes(i: Int, bytes: Int = 4): ByteArray {
			val numBytes = min(bytes, 4)
			return ByteArray(numBytes) { x -> ((i shr ((numBytes - (x + 1)) * 8)) and 255).toByte() }
		}

		fun int2String(i: Int, chars: Int = 4): String {
			val bytes = min(chars, 4)
			val a = CharArray(bytes) { x -> ((i shr ((bytes - (x + 1)) * 8)) and 255).toChar() }
			return String(a)
		}
		fun bytes2Int(bytes: ByteArray, offset: Int = 0, length: Int = minOf(4, bytes.size - offset), bigEndian: Boolean = true): Int{
			return bytesToNum(bytes, offset, length, bigEndian, { toUByte().toInt() }, Int::shl, Iterable<Int>::orOfInt)
		}
		fun string2Int(s: String, length: Int = minOf(4, s.length), bigEndian: Boolean = true): Int{
			return if(bigEndian) (0..<length).orOfInt { s[it].code shl (24 - (8 * it)) }
			else (0..<length).orOfInt { s[it].code shl (8 * it) }
		}
		fun uint2Bytes(i: UInt, bytes: Int = 4): ByteArray {
			val numBytes = min(bytes, 4)
			return ByteArray(numBytes) { x -> ((i shr ((numBytes - (x + 1)) * 8)) and 255u).toByte() }
		}

		fun uint2String(i: UInt, chars: Int = 4): String {
			val bytes = min(chars, 4)
			val a = CharArray(bytes) { x -> ((i shr ((bytes - (x + 1)) * 8)) and 255u).toInt().toChar() }
			return String(a)
		}
		fun bytes2UInt(bytes: ByteArray, offset: Int = 0, length: Int = minOf(4, bytes.size - offset), bigEndian: Boolean = true): UInt{
			return bytesToNum(bytes, offset, length, bigEndian, UByte::toUInt, UInt::shl, Iterable<Int>::orOfUInt)
		}
		fun string2UInt(s: String, length: Int = minOf(4, s.length), bigEndian: Boolean = true): UInt{
			return if(bigEndian) (0..<length).orOfUInt { s[it].code.toUInt() shl (8 * (length - 1 - it)) }
			else (0..<length).orOfUInt { s[it].code.toUInt() shl (8 * it) }
		}

		fun long2Bytes(l: Long, bytes: Int = 8): ByteArray {
			val numBytes = min(bytes, 8)
			return ByteArray(numBytes) { x -> ((l shr ((numBytes - (x + 1)) * 8)) and 255).toByte() }
		}

		fun long2String(l: Long, chars: Int = 8): String {
			val bytes = min(chars, 8)
			val a = CharArray(bytes) { x -> ((l shr ((bytes - (x + 1)) * 8)) and 255).toInt().toChar() }
			return String(a)
		}
		fun bytes2Long(bytes: ByteArray, offset: Int, length: Int = minOf(8, bytes.size - offset), bigEndian: Boolean = true): Long{
			return bytesToNum(bytes, offset, length, bigEndian, UByte::toLong, Long::shl, Iterable<Int>::orOfLong)
		}
		fun string2Long(s: String, length: Int = minOf(8, s.length), bigEndian: Boolean = true): Long{
			return if(bigEndian) (0..<length).orOfLong { s[it].code.toLong() shl (56 - (8 * it)) }
			else (0..<length).orOfLong { s[it].code.toLong() shl (8 * it) }
		}

		fun float2Bytes(f: Float): ByteArray{
			val n = java.lang.Float.floatToIntBits(f)
			return ByteArray(4) { x -> ((n shr (24 - (x * 8))) and 255).toByte() }
		}

		fun float2String(f: Float): String {
			val n = java.lang.Float.floatToIntBits(f)
			val a = CharArray(4) { x -> ((n shr (24 - (x * 8))) and 255).toChar() }
			return String(a)
		}

		fun bytes2Float(b: ByteArray, offset: Int = 0): Float {
			val i = bytes2Int(b, offset)
			return Float.fromBits(i)
		}

		fun string2Float(s: String, bigEndian: Boolean = true): Float {
			val i = string2Int(s, bigEndian = bigEndian)
			return Float.fromBits(i)
		}

		fun double2Bytes(d: Double): ByteArray {
			val n = d.asLongBits
			return ByteArray(8) { x -> ((n shr (56 - (x * 8))) and 255).toByte() }
		}

		fun double2String(d: Double): String {
			val n = d.asLongBits
			val a = CharArray(8) { x -> Char((n shr (56 - (x * 8))).toInt() and 255) }
			return String(a)
		}

		fun bytes2Double(bytes: ByteArray, offset: Int): Double {
			val i = bytes2Long(bytes, offset)
			return Double.fromBits(i)
		}

		fun string2Double(s: String, bigEndian: Boolean = true): Double {
			val i = string2Long(s, bigEndian = bigEndian)
			return Double.fromBits(i)
		}

		fun <T: Number> vec22Bytes(vec: Vec2t<T>, bigEndian: Boolean = true): ByteArray{
			val bytes = ByteArray(8)
			return vec.to(bytes, bigEndian)
		}

		fun <T: Number, V: Vec2t<T>> bytes2Vec2t(bytes: ByteArray, tSize: Int, bytes2t: (ByteArray, Int) -> T, construct: (T, T) -> V, default: V): V{
			return when (bytes.size) {
				tSize * 2 + 1 -> construct(bytes2t(bytes, 0), bytes2t(bytes, tSize + 1))
				tSize * 2 -> construct(bytes2t(bytes, 0), bytes2t(bytes, tSize))
				else -> default
			}
		}

		fun bytes2Vec2i(bytes: ByteArray): Vec2i = bytes2Vec2t(bytes, 4, ByteArray::getInt, ::Vec2i, Vec2i(0))
		fun bytes2Vec2ui(bytes: ByteArray): Vec2ui = bytes2Vec2t(bytes, 4, { a, o -> bytes2UInt(a, o).toInt().toUint() }, ::Vec2ui, Vec2ui())
		fun bytes2Vec2(bytes: ByteArray): Vec2 = bytes2Vec2t(bytes, 4, ::bytes2Float, ::Vec2, Vec2(0f))
		fun bytes2Vec2d(bytes: ByteArray): Vec2d = bytes2Vec2t(bytes, 8, ::bytes2Double, ::Vec2d, Vec2d(0.0))

		fun <T: Number, V: Vec2t<T>> string2Vec2t(string: String, tSize: Int, string2t: (String) -> T, construct: (T, T) -> V, default: V): V{
			return when (string.length) {
				tSize * 2 + 1 -> construct(string2t(string.take(tSize)), string2t(string.substring(tSize + 1..2 * tSize)))
				tSize * 2 -> construct(string2t(string.take(tSize)), string2t(string.substring(tSize, 2 * tSize)))
				else -> default
			}
		}

		fun string2Vec2i(string: String): Vec2i = string2Vec2t(string, 4, ::string2Int, ::Vec2i, Vec2i(0))
		fun string2Vec2ui(string: String): Vec2ui = string2Vec2t(string, 4, { string2UInt(it).toInt().toUint() }, ::Vec2ui, Vec2ui())
		fun string2Vec2(string: String): Vec2 = string2Vec2t(string, 4, ::string2Float, ::Vec2, Vec2(0f))
		fun string2Vec2d(string: String): Vec2d = string2Vec2t(string, 8, ::string2Double, ::Vec2d, Vec2d(0.0))

		fun <T: Number> vec32Bytes(vec: Vec3t<T>, bigEndian: Boolean = true): ByteArray{
			val bytes = ByteArray(12)
			return vec.to(bytes, bigEndian)
		}

		fun <T: Number, V: Vec3t<T>> string2Vec3t(string: String, tSize: Int, string2t: (String) -> T, construct: (T, T, T) -> V, default: V): V{
			return when (string.length) {
				tSize * 3 + 2 -> construct(string2t(string.take(tSize)), string2t(string.substring(tSize + 1..2 * tSize)), string2t(string.substring(2 * tSize + 2, 3 * tSize + 2)))
				tSize * 3 -> construct(string2t(string.take(tSize)), string2t(string.substring(tSize, 2 * tSize)), string2t(string.substring(2 * tSize, 3 * tSize)))
				else -> default
			}
		}

		fun string2Vec3i(string: String): Vec3i = string2Vec3t(string, 4, ::string2Int, ::Vec3i, Vec3i(0))
		fun string2Vec3ui(string: String): Vec3ui = string2Vec3t(string, 4, { string2UInt(it).toInt().toUint() }, ::Vec3ui, Vec3ui())
		fun string2Vec3(string: String): Vec3 = string2Vec3t(string, 4, ::string2Float, ::Vec3, Vec3(0f))
		fun string2Vec3d(string: String): Vec3d = string2Vec3t(string, 8, ::string2Double, ::Vec3d, Vec3d(0.0))

		fun <T: Number, V: Vec4t<T>> string2Vec4t(string: String, tSize: Int, string2t: (String) -> T, construct: (T, T, T, T) -> V, default: V): V{
			return when (string.length) {
				tSize * 4 + 3 -> construct(
					string2t(string.take(tSize)),
					string2t(string.substring(tSize + 1..2 * tSize)),
					string2t(string.substring(2 * tSize + 2, 3 * tSize + 2)),
					string2t(string.substring(3 * tSize + 3, 4 * tSize + 3))
				)
				tSize * 4 -> construct(
					string2t(string.take(tSize)),
					string2t(string.substring(tSize, 2 * tSize)),
					string2t(string.substring(2 * tSize, 3 * tSize)),
					string2t(string.substring(3 * tSize, 4 * tSize))
				)
				else -> default
			}
		}

		fun string2Vec4i(string: String): Vec4i = string2Vec4t(string, 4, ::string2Int, ::Vec4i, Vec4i(0))
		fun string2Vec4ui(string: String): Vec4ui = string2Vec4t(string, 4, { string2UInt(it).toInt().toUint() }, ::Vec4ui, Vec4ui())
		fun string2Vec4(string: String): Vec4 = string2Vec4t(string, 4, ::string2Float, ::Vec4, Vec4(0f))
		fun string2Vec4d(string: String): Vec4d = string2Vec4t(string, 8, ::string2Double, ::Vec4d, Vec4d(0.0))

		fun <T: Number, V: QuatT<T>> string2QuatT(string: String, tSize: Int, string2t: (String) -> T, construct: (T, T, T, T) -> V, default: V, wLast: Boolean = true): V{

			return if(string.length == tSize * 4 + 3) {
				if(wLast){
					construct(string2t(string.substring(3 * tSize + 3, 4 * tSize + 3)), string2t(string.take(tSize)),
						string2t(string.substring(tSize + 1..2 * tSize)), string2t(string.substring(2 * tSize + 2, 3 * tSize + 2)))
				}
				else {
					construct(string2t(string.take(tSize)), string2t(string.substring(tSize + 1..2 * tSize)),
						string2t(string.substring(2 * tSize + 2, 3 * tSize + 2)), string2t(string.substring(3 * tSize + 3, 4 * tSize + 3)))
				}
			}
			else if(string.length == tSize * 4) {
				if(wLast){
					construct(string2t(string.substring(3 * tSize, 4 * tSize)), string2t(string.take(tSize)),
						string2t(string.substring(tSize, 2 * tSize)), string2t(string.substring(2 * tSize, 3 * tSize)))
				}
				else {
					construct(string2t(string.take(tSize)), string2t(string.substring(tSize, 2 * tSize)),
						string2t(string.substring(2 * tSize, 3 * tSize)), string2t(string.substring(3 * tSize, 4 * tSize)))
				}
			}
			else default
		}

		fun string2Quat(string: String, wLast: Boolean = true): Quat = string2QuatT(string, 4, ::string2Float, ::Quat, Quat())
		fun string2QuatD(string: String, wLast: Boolean = true): QuatD = string2QuatT(string, 8, ::string2Double, ::QuatD, QuatD())
	}
}