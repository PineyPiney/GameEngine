package com.pineypiney.game_engine.util

import glm_.func.common.ceil
import glm_.vec3.Vec3i
import kotlin.math.roundToInt

@OptIn(ExperimentalUnsignedTypes::class)
class BitMap3D(val x: Int, val y: Int, val z: Int) {

	constructor(size: Vec3i): this(size.x, size.y, size.z)

	val bits = x * y * z
	val xRowBytes = (x * .125).ceil.roundToInt()
	val numBytes = xRowBytes * y * z
	val bytes = UByteArray(numBytes)

	fun or(startX: Int, startY: Int, startZ: Int, endX: Int, endY: Int, endZ: Int){
		val firstByte = startX / 8
		val firstBit = startX % 8
		val lastByte = endX / 8
		val lastBit = endX % 8
		val numXBytes = lastByte + 1 - firstByte
		for(z in startZ..endZ){
			val zO = z * xRowBytes * y
			for (y in startY..endY){
				val yO = zO + y * xRowBytes
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
	}

	fun or(x: Int, y: Int, z: Int){
		val byte = byteIndex(x, y, z)
		val bit = x % 8
		or(byte, (1u shl bit).toUByte())
	}

	fun or(byte: Int, value: UByte){
		bytes[byte] = bytes[byte] or value
	}

	fun check(x: Int, y: Int, z: Int): Boolean{
		val byte = byteIndex(x, y, z)
		val bit = x % 8
		return bytes[byte] and (1u shl bit).toUByte() > 0u
	}

	fun byteIndex(x: Int, y: Int, z: Int): Int{
		return ((z * this.y) + y) * xRowBytes + (x / 8)
	}

	fun allTrue(): Set<Vec3i>{
		val set = mutableSetOf<Vec3i>()
		for(z in 0..<z){
			val zO = z * xRowBytes * y
			for(y in 0..<y){
				val yO = zO + y * xRowBytes
				for(x in 0..<xRowBytes){
					val byte = bytes[yO + x]
					for(bit in 0..7){
						if(byte and (1u shl bit).toUByte() > 0u){
							set.add(Vec3i(x * 8 + bit, y, z))
						}
					}
				}
			}
		}
		return set
	}

	fun sliceXY(z: Int): BitMap2D {
		val map = BitMap2D(x, y)
		val index = byteIndex(0, 0, z)
		bytes.copyInto(map.bytes, 0, index, index + map.numBytes)
		return map
	}

	fun sliceXZ(y: Int): BitMap2D {
		val map = BitMap2D(x, z)
		val firstIndex = byteIndex(0, y, 0)

		for(z in 0..<z) {
			val index = firstIndex + (z * this.y * xRowBytes)
			bytes.copyInto(map.bytes, z * map.xRowBytes, index, index + map.xRowBytes)
		}

		return map
	}

	fun sliceYZ(x: Int): BitMap2D {
		val map = BitMap2D(y, z)

		for(z in 0..<z) {
			for(y in 0..<y){
				if(check(x, y, z)) map.or(y, z)
			}
		}

		return map
	}

	companion object {
		fun createByteMask(start: Int, end: Int): UByte{
			return if(start == end) (1u shl end).toUByte()
			else ((1u shl (end + 1)) - (1u shl start)).toUByte()
		}
	}
}