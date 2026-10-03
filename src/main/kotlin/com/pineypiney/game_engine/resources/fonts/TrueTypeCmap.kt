package com.pineypiney.game_engine.resources.fonts

sealed interface TrueTypeCmap {

	fun getGlyphId(char: Int): Int

	class Format4(val starts: IntArray, val ends: IntArray, val idDeltas: IntArray, val idOffsets: IntArray, val glyphIds: IntArray) : TrueTypeCmap {
		override fun getGlyphId(char: Int): Int {
			val i = ends.indices.firstOrNull { i -> ends[i] >= char } ?: return -1
			if (starts[i] > char) return -1

			val rangeOffset = idOffsets[i] shr 1
			return if (rangeOffset == 0) char + idDeltas[i]
			else glyphIds[(char - starts[i]) + i + rangeOffset - starts.size]
		}
	}

	class Format6(val start: Int, val ids: IntArray) : TrueTypeCmap {
		override fun getGlyphId(char: Int): Int {
			return ids.getOrNull(char - start) ?: -1
		}
	}
}