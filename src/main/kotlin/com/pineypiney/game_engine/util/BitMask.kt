package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.BitMap3D.Companion.createByteMask
import glm_.func.common.ceil
import glm_.vec2.Vec2i
import kotlin.math.roundToInt

@OptIn(ExperimentalUnsignedTypes::class)
class BitMask(val bits: Int) {

	val numBytes = (bits * .125f).ceil.roundToInt()
	val bytes = UByteArray(numBytes)

	fun orRange(startX: Int, endX: Int) {
		val firstByte = startX / 8
		val firstBit = startX % 8
		val lastByte = endX / 8
		val lastBit = endX % 8
		val numXBytes = lastByte + 1 - firstByte

		if (numXBytes == 1) {
			orByte(firstByte, createByteMask(firstBit, lastBit))
		} else {
			orByte(firstByte, createByteMask(firstBit, 7))
			orByte(lastByte, createByteMask(0, lastBit))
			if (numXBytes > 2) {
				for (xO in firstByte + 1..<lastByte) bytes[xO] = 255u
			}
		}
	}

	fun or(x: Int) {
		val byte = byteIndex(x)
		val bit = x % 8
		orByte(byte, (1u shl bit).toUByte())
	}

	fun orByte(byte: Int, value: UByte) {
		bytes[byte] = bytes[byte] or value
	}

	fun andNotRange(startX: Int, endX: Int) {
		val firstByte = startX / 8
		val firstBit = startX % 8
		val lastByte = endX / 8
		val lastBit = endX % 8
		val numXBytes = lastByte + 1 - firstByte

		if (numXBytes == 1) {
			andNotByte(firstByte, createByteMask(firstBit, lastBit))
		} else {
			andNotByte(firstByte, createByteMask(firstBit, 7))
			andNotByte(lastByte, createByteMask(0, lastBit))
			if (numXBytes > 2) {
				for (xO in firstByte + 1..<lastByte) bytes[xO] = 0u
			}
		}
	}

	infix fun andNotRange(rect: Vec2i) {
		andNotRange(rect.x, rect.y)
	}

	infix fun andNot(other: BitMask): BitMask {
		if (other.bits != bits) throw IllegalArgumentException("Cannot andNot Bitmask of size ($bits) with Bitmap of size (${other.bits}), they must be the same size")

		val newMap = BitMask(bits)
		for (i in 0..<numBytes) newMap.bytes[i] = bytes[i] and other.bytes[i].inv()
		return newMap
	}

	fun andNot(x: Int) {
		val byte = byteIndex(x)
		val bit = x % 8
		andNotByte(byte, (1u shl bit).toUByte())
	}

	fun andNotByte(byte: Int, value: UByte) {
		bytes[byte] = bytes[byte] and value.inv()
	}

	fun setBit(x: Int, on: Boolean) {
		if (on) or(x)
		else andNot(x)
	}

	operator fun get(bit: Int): Boolean {
		val byte = byteIndex(bit)
		val bit = bit % 8
		return bytes[byte] and (1u shl bit).toUByte() > 0u
	}

	fun byteIndex(x: Int): Int {
		return x shr 3
	}

	fun allTrue(): Set<Int> {
		val set = mutableSetOf<Int>()
		for (x in 0..<numBytes) {
			val byte = bytes[x]
			for (bit in 0..7) {
				if (byte and (1u shl bit).toUByte() > 0u) {
					set.add(x * 8 + bit)
				}
			}
		}
		return set
	}

	fun copy(): BitMask {
		val bitmap = BitMask(bits)
		bytes.copyInto(bitmap.bytes)
		return bitmap
	}
}