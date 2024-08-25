package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import glm_.asLongBits
import kotlin.math.min

class ByteData {

	companion object {

		fun int2Bytes(i: Int, bytes: Int = 4): ByteArray {
			val numBytes = min(bytes, 4)
			return ByteArray(numBytes) { x -> ((i shr ((numBytes - (x + 1)) * 8)) and 255).toByte() }
		}

		fun int2String(i: Int, chars: Int = 4): String {
			val bytes = min(chars, 4)
			val a = CharArray(bytes) { x -> ((i shr ((bytes - (x + 1)) * 8)) and 255).toChar() }
			return String(a)
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

		fun long2Bytes(l: Long, bytes: Int = 8): ByteArray {
			val numBytes = min(bytes, 8)
			return ByteArray(numBytes) { x -> ((l shr ((numBytes - (x + 1)) * 8)) and 255).toByte() }
		}

		fun long2String(l: Long, chars: Int = 8): String {
			val bytes = min(chars, 8)
			val a = CharArray(bytes) { x -> ((l shr ((bytes - (x + 1)) * 8)) and 255).toInt().toChar() }
			return String(a)
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

		fun bytes2Float(b: ByteArray): Float {
			var i = 0
			for (a in 0..3) {
				try {
					i += b[a].toInt() shl (24 - (a * 8))
				} catch (e: StringIndexOutOfBoundsException) {
					GameEngineI.logger.warn("Couldn't parse encoded float string $b length ${b.size}")
				}
			}
			return Float.fromBits(i)
		}

		fun string2Float(s: String): Float {
			var i = 0
			for (a in 0..3) {
				try {
					i += s[a].code shl (24 - (a * 8))
				} catch (e: StringIndexOutOfBoundsException) {
					GameEngineI.logger.warn("Couldn't parse encoded float string $s length ${s.length}")
				}
			}
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

		fun string2Double(s: String): Double {
			var i = 0L
			for (a in 0..7) {
				try {
					i += s[a].code shl (56 - (a * 8))
				} catch (e: StringIndexOutOfBoundsException) {
					GameEngineI.logger.warn("Couldn't parse encoded double string $s length ${s.length}")
				}
			}
			return Double.fromBits(i)
		}
	}
}