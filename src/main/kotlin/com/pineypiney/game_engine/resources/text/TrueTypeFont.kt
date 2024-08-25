package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.c
import glm_.f
import glm_.vec2.Vec2
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.Font as JavaFont

class TrueTypeFont(
	val font: JavaFont,
	val textures: Map<Char, Texture>,
	val ctx: FontRenderContext = FontRenderContext(null, true, true),
	override val shader: Shader = fontShader
) : Font() {

	val res = 200
	val missing = textures[127.c] ?: Texture.broke

	// This is the width of a space character
	val space = calcSpace()

	override fun getCharWidth(char: Char): Float {
		val outline = getOutline(char)
		return outline.bounds2D.width.f
	}

	override fun getCharHeight(char: Char): Vec2 {
		val bounds = getOutline(char).bounds2D
		return Vec2(bounds.y, bounds.y + bounds.height)
	}

	override fun getWidth(text: String): Float {
		var width = getOutline(text).bounds2D.width.f
		var i = text.lastIndex
		while (i >= 0 && text[i] == ' ') {
			i--
			width += space
		}
		return width
	}

	override fun getHeight(text: String): Float {
		val bounds = getOutline(text).bounds2D
		return bounds.height.f
	}

	override fun getQuads(text: String, bold: Boolean): Collection<TextQuad> {
		val list = mutableListOf<TextQuad>()
		val glyph = font.createGlyphVector(ctx, text)
		for (i in text.indices) {
			val shape = glyph.getGlyphOutline(i)
			list.add(createQuad(text[i], shape))
		}

		return list
	}

	fun getOutline(string: String): Shape {
		return font.createGlyphVector(ctx, string).outline
	}

	fun getOutline(char: Char) = getOutline(char.toString())

	fun createQuad(char: Char, shape: Shape): TextQuad {
		return TextQuad(
			createVertices(shape),
			getTexture(char),
			Vec2(shape.bounds2D.x, -(shape.bounds2D.y + shape.bounds2D.height))
		)
	}

	fun createVertices(shape: Shape): Array<Vec2> {
		val width = shape.bounds2D.width.f.let { if (it == 0f) 0.2f else it }
		val height = shape.bounds2D.height.f.let { if (it == 0f) 0.2f else it }
		return arrayOf(
			// Positions    Texture
			Vec2(0.0f, 0.0f), Vec2(width, height), Vec2(0f, 0f), Vec2(1f, 1f)
		)
	}

	fun getTexture(char: Char): Texture {
		return textures[char] ?: missing
	}

	fun calcSpace(): Float {
		val glyph = font.createGlyphVector(ctx, "! !")
		val shapeA = glyph.getGlyphOutline(2)
		val shapeB = glyph.getGlyphOutline(0)
		val space = shapeA.bounds2D.x - shapeB.bounds2D.run { x + width }
		return space.f
	}
}