package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.util.BitMap3D.Companion.createByteMask
import glm_.func.common.ceil
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import kotlin.math.roundToInt

@OptIn(ExperimentalUnsignedTypes::class)
class BitMap2D(val x: Int, val y: Int) {
	constructor(size: Vec2i): this(size.x, size.y)

	val bits = x * y
	val xRowBytes = (x * .125).ceil.roundToInt()
	val numBytes = xRowBytes * y
	val bytes = UByteArray(numBytes)

	fun or(startX: Int, startY: Int, endX: Int, endY: Int){
		val firstByte = startX / 8
		val firstBit = startX % 8
		val lastByte = endX / 8
		val lastBit = endX % 8
		val numXBytes = lastByte + 1 - firstByte
		for (y in startY..endY){
			val yO = y * xRowBytes
			if(numXBytes == 1){
				or(yO + firstByte, createByteMask(firstBit, lastBit))
			}
			else {
				or(yO + firstByte, createByteMask(firstBit, 7))
				or(yO + lastByte, createByteMask(0, lastBit))
				if(numXBytes > 2){
					for(xO in yO + firstByte + 1..<yO + lastByte) bytes[xO] = 255u
				}
			}
		}
	}

	fun or(x: Int, y: Int){
		val byte = byteIndex(x, y)
		val bit = x % 8
		or(byte, (1u shl bit).toUByte())
	}

	fun or(byte: Int, value: UByte){
		bytes[byte] = bytes[byte] or value
	}

	fun andNot(startX: Int, startY: Int, endX: Int, endY: Int){
		val firstByte = startX / 8
		val firstBit = startX % 8
		val lastByte = endX / 8
		val lastBit = endX % 8
		val numXBytes = lastByte + 1 - firstByte
		for (y in startY..endY){
			val yO = y * xRowBytes
			if(numXBytes == 1){
				andNot(yO + firstByte, createByteMask(firstBit, lastBit))
			}
			else {
				andNot(yO + firstByte, createByteMask(firstBit, 7))
				andNot(yO + lastByte, createByteMask(0, lastBit))
				if(numXBytes > 2){
					for(xO in yO + firstByte + 1..<yO + lastByte) bytes[xO] = 0u
				}
			}
		}
	}

	infix fun andNot(rect: Vec4i){
		andNot(rect.x, rect.y, rect.z, rect.w)
	}

	infix fun andNot(other: BitMap2D): BitMap2D {
		if(other.x != x || other.y != y) throw IllegalArgumentException("Cannot andNot Bitmap of size ($x, $y) with Bitmap of size (${other.x}, ${other.y}, they must be the same size")

		val newMap = BitMap2D(x, y)
		for(i in 0..<numBytes) newMap.bytes[i] = bytes[i] and other.bytes[i].inv()
		return newMap
	}

	fun andNot(byte: Int, value: UByte){
		bytes[byte] = bytes[byte] and value.inv()
	}

	fun check(x: Int, y: Int): Boolean{
		val byte = byteIndex(x, y)
		val bit = x % 8
		return bytes[byte] and (1u shl bit).toUByte() > 0u
	}

	fun byteIndex(x: Int, y: Int): Int{
		return (y * xRowBytes) + (x / 8)
	}

	fun allTrue(): Set<Vec2i>{
		val set = mutableSetOf<Vec2i>()
		for(y in 0..<y){
			val yO = y * xRowBytes
			for(x in 0..<xRowBytes){
				val byte = bytes[yO + x]
				for(bit in 0..7){
					if(byte and (1u shl bit).toUByte() > 0u){
						set.add(Vec2i(x * 8 + bit, y))
					}
				}
			}
		}
		return set
	}

	fun copy(): BitMap2D{
		val bitmap = BitMap2D(x, y)
		bytes.copyInto(bitmap.bytes)
		return bitmap
	}

	fun greedyMesh(): List<Vec4i> {
		val list = mutableListOf<Vec4i>()
		var range = -1..-1
		for(x in 0..<x){
			for(y in 0..<y){
				val bit = check(x, y)
				// Measure height of next box
				if (bit) {
					if (range.first == -1) {
						// Starting new column
						range = y..-1
					}
					if (y == this.y - 1) {
						// Finish size of column to top of chunk
						range = range.first..<this.y
					}
				} else {
					if (range.first != -1) {
						// Finish size of column to y
						range = range.first..<y
					}
				}
				// Measure width of new box
				if (range.last != -1) {
					var boxX = x
					while (boxX < this.x - 1) {
						// Check each consecutive column for if all boxes in range are filled
						val column = range.all { check(boxX + 1, it) }
						if (column) boxX++
						// This is the end of the box
						else break
					}
					val rect = Vec4i(x, range.first, boxX + 1, range.last + 1)
					andNot(rect.x, rect.y, boxX, range.last)
					list.add(rect)
					range = -1..-1
				}
			}
		}

		return list
	}
}