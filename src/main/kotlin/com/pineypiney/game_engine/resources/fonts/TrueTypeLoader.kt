package com.pineypiney.game_engine.resources.fonts

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.BitMask
import com.pineypiney.game_engine.util.ByteData
import com.pineypiney.game_engine.util.extension_functions.*
import glm_.*
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import java.io.InputStream
import java.time.Instant
import java.util.*

@Suppress("UnusedVariable", "VariableNeverRead", "AssignedValueIsNeverRead")
class TrueTypeLoader {

	lateinit var head: TrueTypeFont.Head
	var numGlyphs: Int = 0
	var numLongHorMetrics: Int = 0

	fun load(stream: InputStream): TrueTypeFont? {
		val scalarType = stream.int()
		val numTables = stream.short()
		val searchRange = stream.short()
		val entrySelector = stream.short()
		val rangeShift = stream.short()
		val i = 12 + (16 * numTables)

		val tables = List(numTables) {
			Table(stream.int(), stream.int(), stream.int() - i, stream.int())
		}

		val head = tables.firstOrNull { it.tagString == "head" } ?: return null
		val hhea = tables.firstOrNull { it.tagString == "hhea" } ?: return null
		val hmtx = tables.firstOrNull { it.tagString == "hmtx" } ?: return null
		val maxp = tables.firstOrNull { it.tagString == "maxp" } ?: return null
		val loca = tables.firstOrNull { it.tagString == "loca" } ?: return null
		val glyf = tables.firstOrNull { it.tagString == "glyf" } ?: return null
		val cmap = tables.firstOrNull { it.tagString == "cmap" } ?: return null

		val bytes = stream.readAllBytes()
		this.head = head.getStream(bytes).use(::loadHEAD) ?: return null

		numGlyphs = maxp.getStream(bytes).use(::loadMAXP)
		if (numGlyphs < 0) return null

		numLongHorMetrics = hhea.getStream(bytes).use(::loadHHEA)
		hmtx.getStream(bytes).use(::loadHMTX)

		val offsets = loca.getStream(bytes).use(::loadLOCA)

		val glyphs = List(numGlyphs) { gI ->
			if (offsets[gI] == offsets[gI + 1]) return@List Glyph.Simple.EMPTY
			val glyfStream = glyf.getSubStream(bytes, offsets[gI], offsets[gI + 1] - offsets[gI])
			glyfStream.use(::loadGLYF)
		}

		val cmapStream = cmap.getStream(bytes)
		val cmapVersion = cmapStream.short()
		val numCmapTables = cmapStream.short()
		val encodingRecords = List(numCmapTables) {
			Triple(cmapStream.short(), cmapStream.short(), cmapStream.int())
		}
		val formats = encodingRecords.mapNotNull { (_, _, offset) ->
			val recordStream = cmap.getSubStream(bytes, offset)
			recordStream.use(::loadCMAP)
		}

		return TrueTypeFont(this.head, glyphs, formats.firstOrNull() ?: return null)
	}

	fun loadHEAD(stream: InputStream): TrueTypeFont.Head? {
		val version = stream.vec2us()
		val revision = stream.vec2us()
		val checksum = stream.int()
		val magic = stream.int()

		if (magic != 0x5f0f3cf5) return null

		val flags = stream.short()
		val unitsPerEm = stream.short()

		val created = stream.long(false)
		val modified = stream.long(false)
		val createdDate = Instant.ofEpochMilli(EPOCH1904 + (created * 1000L))
		val min = stream.vec2s()
		val max = stream.vec2s()
		val macStyle = stream.short()
		val lowestPixelsPerEm = stream.short()
		val fontDirectionHint = stream.short()
		val indexToLocFormat = stream.short()
		val glyphFormat = stream.short()

		return TrueTypeFont.Head(indexToLocFormat == 0, unitsPerEm)
	}

	fun loadMAXP(stream: InputStream): Int {
		val maxpVersion = stream.int()
		return when (maxpVersion) {
			0x5000 -> stream.short()
			0x10000 -> stream.short()
			else -> -1
		}
	}

	fun loadHHEA(stream: InputStream): Int {
		val version = stream.int()
		val ascent = stream.short()
		val descent = stream.short()
		val lineGap = stream.short()
		val maxAdvanceWidth = stream.short()
		val minLeftBearing = stream.short()
		val minRightBearing = stream.short()
		val maxXExtent = stream.short()
		val caretSlopeRise = stream.short()
		val caretSlopeRun = stream.short()
		val caretOffset = stream.short()
		stream.skip(8)
		val metricDataFormat = stream.short()
		val numLongHorMetrics = stream.short() // 02b5
		return numLongHorMetrics
	}

	fun loadHMTX(stream: InputStream): Array<Pair<Int, Int>> {
		val metrics = Array(numGlyphs) { id ->
			if (id < numLongHorMetrics) stream.short() to stream.short().toShort().toInt()
			else stream.short() to -1
		}
		return metrics
	}

	fun loadVHEA(stream: InputStream) {
		val version = stream.int()
		val ascent = stream.short()
		val descent = stream.short()
	}

	fun loadVMTX(stream: InputStream) {

	}

	fun loadLOCA(stream: InputStream): List<Int> {
		return List(numGlyphs + 1) { if (head.shortLoca) stream.short() shl 1 else stream.int() }
	}

	fun loadGLYF(stream: InputStream): Glyph {

		val numContours = stream.short().toShort().toInt()
		val min = Vec2(stream.vec2s()) * head.unitSize
		val max = Vec2(stream.vec2s()) * head.unitSize

		// Simple Glyph
		if (numContours >= 0) {
			val endPtsOfContours = stream.shorts(numContours)
			val instructionLength = stream.short().toUShort().toInt()
			val instructions = stream.readNBytes(instructionLength)

			val numPoints = endPtsOfContours.last() + 1
			val flags = ByteArray(numPoints)
			val points = Array(numPoints) { Vec2(0) }
			val onCurveBitmask = BitMask(numPoints)
			var flagRepeats = 0
			repeat(numPoints) { pI ->
				val flag = if (flagRepeats == 0) stream.byte()
				else flags[pI - 1]
				flags[pI] = flag

				if (flagRepeats == -1) flagRepeats = stream.read() - 1
				else if (flagRepeats > 0) flagRepeats--
				else if (flag has GLYPH_FLAG_REPEAT_FLAG) flagRepeats = -1

				onCurveBitmask.setBit(pI, flag has GLYPH_FLAG_ON_CURVE_POINT)
			}

			var prev = 0f
			repeat(numPoints) { pI ->
				val flag = flags[pI]
				if (flag has GLYPH_FLAG_X_SHORT_VECTOR) {
					val x = stream.read() * head.unitSize
					if (flag has GLYPH_FLAG_X_IS_SAME_OR_POSITIVE_X_SHORT_VECTOR) points[pI].x = prev + x
					else points[pI].x = prev - x
				} else {
					if (flag has GLYPH_FLAG_X_IS_SAME_OR_POSITIVE_X_SHORT_VECTOR) points[pI].x = prev
					else points[pI].x = prev + (stream.short().toShort().toInt() * head.unitSize)
				}
				prev = points[pI].x
			}

			prev = 0f
			repeat(numPoints) { pI ->
				val flag = flags[pI]
				if (flag has GLYPH_FLAG_Y_SHORT_VECTOR) {
					val y = stream.read() * head.unitSize
					if (flag has GLYPH_FLAG_Y_IS_SAME_OR_POSITIVE_Y_SHORT_VECTOR) points[pI].y = prev + y
					else points[pI].y = prev - y
				} else {
					if (flag has GLYPH_FLAG_Y_IS_SAME_OR_POSITIVE_Y_SHORT_VECTOR) points[pI].y = prev
					else points[pI].y = prev + (stream.short().toShort().toInt() * head.unitSize)
				}
				prev = points[pI].y
			}

			return Glyph.Simple(points, min, max, onCurveBitmask, endPtsOfContours)
		}
		// Compound Contour
		else {
			val children = mutableMapOf<Int, Vec2i>()
			var flags = MORE_COMPONENTS

			while (flags has MORE_COMPONENTS) {
				flags = stream.short()
				val index = stream.short()
				val arg1: Int
				val arg2: Int
				if (flags has ARG_1_AND_2_ARE_WORDS) {
					arg1 = stream.short()
					arg2 = stream.short()
				} else {
					arg1 = stream.read()
					arg2 = stream.read()
				}

				if (flags has WE_HAVE_A_SCALE) {
					val scale = stream.short()
				} else if (flags has WE_HAVE_AN_X_AND_Y_SCALE) {
					val scale = stream.vec2s()
				} else if (flags has WE_HAVE_A_TWO_BY_TWO) {
					val xScale = stream.short()
					val scale01 = stream.short()
					val scale10 = stream.short()
					val yScale = stream.short()
				}
			}
			if (flags has WE_HAVE_INSTRUCTIONS) {
				val instructionSize = stream.short()
				val instructions = stream.readNBytes(instructionSize)
			}


			return Glyph.Compound(children)
		}
	}

	fun loadCMAP(stream: InputStream): TrueTypeCmap? {
		val format = stream.short()
		val length: Int
		val language: Int

		if (format <= 6) {
			length = stream.short()
			language = stream.short()
			if (format == 4) {
				val segCount = stream.short() shr 1
				val searchRange = stream.short()
				val entrySelector = stream.short()
				val rangeShift = stream.short()
				val endCodes = stream.shorts(segCount)
				stream.skip(2)
				val startCodes = stream.shorts(segCount)
				val idDelta = stream.sshorts(segCount)
				val idRangeOffset = stream.shorts(segCount)
				val glyphIds = stream.shorts((length - (segCount * 8) - 14) shr 1)

				return TrueTypeCmap.Format4(startCodes, endCodes, idDelta, idRangeOffset, glyphIds)
			} else if (format == 6) {
				val firstCode = stream.short()
				val count = stream.short()
				val ids = stream.shorts(count)
				return TrueTypeCmap.Format6(firstCode, ids)
			}
		} else if (format <= 13) {
			stream.skip(2)
			length = stream.int()
			language = stream.int()
		} else {
			length = stream.int()
			language = 0
		}

		GameEngineI.logger.warn("Cannot load TTF CMAP format $format")
		return null
	}

	fun loadLTSH(stream: InputStream) {
		val version = stream.short()
		val numGlyphs = stream.short()
		val yPixels = stream.readNBytes(numGlyphs)
	}

	fun loadOS2(stream: InputStream) {
		val version = stream.short()
		val avgWidth = stream.short()
		val weightClass = stream.short()
		val widthClass = stream.short()
		val fsType = stream.short()
		val subscriptSize = stream.vec2s()
		val subscriptOffset = stream.vec2s()
		val superscriptSize = stream.vec2s()
		val superscriptOffset = stream.vec2s()
		val strikeoutSize = stream.short()
		val strikeoutPos = stream.short()
		val familyClass = stream.short() // 32 Bytes

		val panose = stream.readNBytes(10)
		val unicodeRange = stream.readNBytes(16)
		val achVendId = stream.readNBytes(4)
		val fsSelection = stream.short()
		val fsCharIndexRange = stream.vec2s() // 68 Bytes

		if (version < 1) return

		val typeAscender = stream.short()
		val typoDescender = stream.short()
		val typeLineGap = stream.short()
		val winAscent = stream.short()
		val winDescent = stream.short()
		val codePage = stream.long() // 86 Bytes

		if (version < 2) return

		val height = stream.short()
		val capHeight = stream.short()
		val defaultChar = stream.short()
		val breakChar = stream.short()
		val maxContext = stream.short() // 96 Bytes

		if (version < 5) return

		val opticalPointSizeRange = stream.vec2s()
	}

	fun loadCVT(stream: InputStream) {
		val instructions = stream.shorts(stream.available() shr 1)
	}

	fun loadFPGM(stream: InputStream) {
		val instructions = stream.readAllBytes()
	}

	fun loadHDMX(stream: InputStream, numGlyphs: Int) {
		val version = stream.short()
		val numRecords = stream.short()
		val recordSize = stream.int()
		val records = Array(numRecords) {
			Triple(stream.read(), stream.read(), stream.readNBytes(numGlyphs))
		}
	}

	fun loadVDMX(stream: InputStream) { //0bba
		val version = stream.short()
		val numGroups = stream.short()
		val numRatios = stream.short()
		val ratios = List(numRatios) { stream.int() }
		val groupOffsets = stream.vec2ss(numRatios)

		repeat(numGroups) {
			val numRecs = stream.short()
			val start = stream.read()
			val end = stream.read()
			val records = Array(numRecs) {
				Triple(stream.short(), stream.short(), stream.short())
			}
		}
	}

	fun loadGASP(stream: InputStream) {
		val version = stream.short()
		val size = stream.short()
		val ranges = stream.vec2ss(size)
	}

	data class Table(val tag: Int, val checkSum: Int, val offset: Int, val length: Int) {
		val tagString = ByteData.int2Bytes(tag).decodeToString()
		fun getStream(bytes: ByteArray) = bytes.inputStream(offset, length)
		fun getSubStream(bytes: ByteArray, o: Int, l: Int = bytes.size - offset - o) = bytes.inputStream(offset + o, l)
	}

	companion object {

		const val GLYPH_FLAG_ON_CURVE_POINT = 0x01
		const val GLYPH_FLAG_X_SHORT_VECTOR = 0x02
		const val GLYPH_FLAG_Y_SHORT_VECTOR = 0x04
		const val GLYPH_FLAG_REPEAT_FLAG = 0x08
		const val GLYPH_FLAG_X_IS_SAME_OR_POSITIVE_X_SHORT_VECTOR = 0x10
		const val GLYPH_FLAG_Y_IS_SAME_OR_POSITIVE_Y_SHORT_VECTOR = 0x20
		const val GLYPH_FLAG_OVERLAP_SIMPLE = 0x40

		const val ARG_1_AND_2_ARE_WORDS = 0x0001
		const val ARGS_ARE_XY_VALUES = 0x0002
		const val ROUND_XY_TO_GRID = 0x0004
		const val WE_HAVE_A_SCALE = 0x0008
		const val MORE_COMPONENTS = 0x0020
		const val WE_HAVE_AN_X_AND_Y_SCALE = 0x0040
		const val WE_HAVE_A_TWO_BY_TWO = 0x0080
		const val WE_HAVE_INSTRUCTIONS = 0x0100
		const val USE_MY_METRICS = 0x0200
		const val OVERLAP_COMPOUND = 0x0400
		const val SCALED_COMPONENT_OFFSET = 0x0800
		const val UNSCALED_COMPONENT_OFFSET = 0x1000

		val EPOCH1904 = GregorianCalendar(1904, 0, 1).timeInMillis
	}
}