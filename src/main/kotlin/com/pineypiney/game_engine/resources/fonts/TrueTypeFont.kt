package com.pineypiney.game_engine.resources.fonts

class TrueTypeFont(val head: Head, val glyphs: List<Glyph>, val charMap: TrueTypeCmap) {

	fun getGlyph(char: Char): Glyph {
		val glyphIndex = charMap.getGlyphId(char.code)
		return glyphs[glyphIndex]
	}


	data class Head(val shortLoca: Boolean, val unitsPerEm: Int) {
		val unitSize = 1f / unitsPerEm
	}
}