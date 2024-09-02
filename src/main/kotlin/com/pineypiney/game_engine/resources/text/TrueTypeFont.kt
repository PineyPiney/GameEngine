package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.util.shapes.TextMesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import glm_.c
import glm_.f
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import glm_.vec4.swizzle.xy
import glm_.vec4.swizzle.zw
import kool.ByteBuffer
import org.lwjgl.opengl.GL11C
import java.awt.Shape
import java.awt.font.FontRenderContext
import java.awt.Font as JavaFont

class TrueTypeFont(
	override val name: String,
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

	override fun getShape(text: String, bold: Boolean, alignment: Int): TextMesh {
		val list = mutableListOf<TextMesh.CharacterMesh>()
		val glyph = font.createGlyphVector(ctx, text)
		val (alignX, alignY) = getAlignmentOffset(text, alignment)
		val (texture, dimensions) = createTexture(text.toSet())
		var line = 0
		for (i in text.indices) {
			if(text[i] == '\n') line++
			val shape = glyph.getGlyphOutline(i)
			list.add(createChar(shape, Vec2(alignX[line], alignY), dimensions[text[i]] ?: continue))
		}

		return TextMesh(list.toTypedArray(), texture, true)
	}

	fun getOutline(string: String): Shape {
		return font.createGlyphVector(ctx, string).outline
	}

	fun getOutline(char: Char) = getOutline(char.toString())

	fun createTexture(chars: Set<Char>): Pair<Texture, Map<Char, Vec4>>{
		if(chars.isEmpty()) return Texture.broke to emptyMap()

		val textures = chars.associateWith { getTexture(it) }
		val width = textures.values.sumOf { it.width }
		val height = textures.maxOf { it.value.height }
		val invWidth = 1f / width
		val invHeight = 1f / height
		val texture = Texture("", TextureLoader.createTexture(ByteBuffer(width * height * 3), width, height, GL11C.GL_RGB))
		var x = 0
		val dimensions = mutableMapOf<Char, Vec4>()

		for((c, t) in textures){
			try {
				texture.setSubData(t.getData(), x, 0, t.width, t.height, t.format)
			}
			catch (e: Exception){
				GameEngineI.logger.error("Couldn't get texture data for TrueTypeFont $name, character $c")
			}
			dimensions[c] = Vec4(x * invWidth, 0f, (x + t.width) * invWidth, t.height * invHeight)
			x += t.width
		}

		return texture to dimensions
	}

	fun createChar(shape: Shape, offset: Vec2, textureDimensions: Vec4): TextMesh.CharacterMesh {
		val width = shape.bounds2D.width.f.let { if (it == 0f) 0.2f else it }
		val height = shape.bounds2D.height.f.let { if (it == 0f) 0.2f else it }
		val pos = Vec2(shape.bounds2D.x, -(shape.bounds2D.y + shape.bounds2D.height)) + offset
		return TextMesh.CharacterMesh(pos, Vec2(width, height) + pos, textureDimensions.xy, textureDimensions.zw)
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