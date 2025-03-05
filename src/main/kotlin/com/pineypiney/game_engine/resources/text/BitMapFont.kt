package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.objects.util.shapes.TextMesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.sumOf
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import kotlin.math.max
import kotlin.math.min

class BitMapFont(
	override val name: String,
	val texture: Texture,
	val boldTexture: Texture? = null,
	private val charDimensions: Map<Char, Vec4i>,
	private val boldCharDimensions: Map<Char, Vec4i>?,
	val letterWidth: Int = 32,
	val letterHeight: Int = 64,
	val characterSpacing: Float = 0.0625f,
	val lineSpacing: Float = 1.2f,
	val firstLetter: Int = 32,
	override val shader: Shader = fontShader
) : Font() {

	val columns = texture.width / letterWidth

	// The dimension of each character is defined as Vec4(min x, min y, max x, max y)
	// Given that (0, 0) is the top left of each letter's box
	fun getDimensions(char: Char): Vec4i? = (boldCharDimensions ?: charDimensions)[char]

	override fun getCharWidth(char: Char): Float = (getDimensions(char)?.run { z - x }?.f ?: 0f) / letterWidth
	override fun getCharHeight(char: Char): Vec2 =
		getDimensions(char)?.let { Vec2(it.y, it.w) / letterHeight } ?: Vec2(0.5f)

	// Get the width of a string on the scale of 1 being the width of an entire column
	override fun getWidth(text: String): Float {
		return text.split('\n').maxOf { line ->
			(characterSpacing * (line.length + 1)) + line.sumOf { getCharWidth(it) }
		}
	}

	// Get the height of a string on the scale of 1 being the width of an entire column
	override fun getHeight(text: String): Float {
		return 1f + lineSpacing * (text.count { it == '\n' })
	}

	override fun getShape(text: String, bold: Boolean, bounds: Vec2, alignment: Int): TextMesh{
		val dimensions = getPixelSize(text)
		var letterX = characterSpacing
		var letterY = lineSpacing * text.count { it == '\n' }
		val quads = mutableSetOf<TextMesh.CharacterMesh>()

		val (alignX, alignY) = getAlignmentOffset(text, bounds, alignment)
		var line = 0

		for (char in text) {
			if (char == '\n') {
				line++
				letterX = characterSpacing
				letterY -= lineSpacing
				continue
			}
			val offset = Vec2(letterX + alignX[line], letterY + alignY)
			val quad = createTextVertices(char, dimensions.w, dimensions.y, offset)
			letterX += getCharWidth(char) + characterSpacing
			quads.add(quad)
		}
		return TextMesh(quads.toTypedArray(), if(bold) boldTexture?:texture else texture)
	}

	fun createTextVertices(char: Char, top: Float, bottom: Float, offset: Vec2): TextMesh.CharacterMesh {
		val pixelHeight = top - bottom

		// Index of letter within bitmap, where 0 is the letter in the top left, counting along the rows
		val letterIndex = char.i - firstLetter
		// The top left corner of the quad containing this letter in the texture in pixels
		val letterPoint =
			Vec2i((letterIndex % columns) * letterWidth, texture.height - ((letterIndex / columns) * letterHeight))
		// Size of this letter relative to the whole texture
		val letterSize = Vec2(getCharWidth(char).f / columns, pixelHeight / texture.height)

		val texturePos = Vec2(letterPoint.x.f / texture.width, (letterPoint.y - top) / texture.height)

		val height = pixelHeight / letterWidth
		return TextMesh.CharacterMesh(offset, Vec2(getCharWidth(char), height) + offset, texturePos, texturePos + letterSize)
	}

	private fun getPixelSize(text: String): Vec4 {
		val w = getPixelWidth(text)
		var min = letterHeight
		var max = 0
		for (it in text) {
			val v = getDimensions(it) ?: continue
			min = min(min, v.y)
			max = max(max, v.w)
		}

		return Vec4(0, min - 5, w, max + 5)
	}

	fun getPixelWidth(text: String): Int {
		// Starting at 2 accounts for the margin at the beginning of the text
		return (getWidth(text) * letterWidth).i
	}
}