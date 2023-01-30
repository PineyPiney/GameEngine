package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.reduce
import com.pineypiney.game_engine.util.extension_functions.sumOf
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import kotlin.math.max
import kotlin.math.min

class BitMapFont(val texture: Texture, private val charDimensions: Map<Char, Vec4i>, val letterWidth: Int = 32, val letterHeight: Int = 64, val characterSpacing: Float = 0.0625f, val rows: Int = 16, val columns: Int = 16, override val shader: Shader = fontShader): Font() {

    val letterRatio = letterWidth.f / letterHeight.f

    // The dimension of each character is defined as Vec4(min x, min y, max x, max y)
    // Given that (0, 0) is the top left of each letter's box
    fun getDimensions(char: Char): Vec4i? = charDimensions[char]

    override fun getCharWidth(char: Char): Float = (getDimensions(char)?.z ?: 0).f / letterWidth
    override fun getCharHeight(char: Char): Vec2 = getDimensions(char)?.let { Vec2(it.y, it.w) / letterHeight } ?: Vec2(0.5f)

    // Get the width of a string on the scale of 1 being the width of an entire column
    override fun getWidth(text: String): Float {
        return (characterSpacing * (text.length + 1)) + text.sumOf { getCharWidth(it) }
    }

    // Get the height of a string on the scale of 1 being the width of an entire column
    override fun getHeight(text: String): Float {
        val bounds = text.reduce(Vec2(1f, 0f)){ acc, char ->
            val height = getCharHeight(char)
            if(height.x < acc.x) acc.x = height.x
            if(height.y > acc.y) acc.y = height.y
            acc
        }
        // Divide by the aspect ratio of a letter to put it on the same scale as getWidth
        return (bounds.y - bounds.x) / letterRatio
    }

    override fun getQuads(text: String, line: Int): Array<TextQuad> {
        val dimensions = getPixelSize(text)
        var xOffset = characterSpacing
        var yOffset = -line
        val quads = mutableSetOf<TextQuad>()
        for(char in text){
            if(char == '\n'){
                xOffset = characterSpacing
                yOffset--
                continue
            }
            val quad = TextQuad(createTextVertices(char, dimensions.w, dimensions.y), texture, Vec2(xOffset, yOffset))
            xOffset += getCharWidth(char) + characterSpacing
            quads.add(quad)
        }
        return quads.toTypedArray()
    }

    fun createTextVertices(char: Char, top: Float, bottom: Float) : FloatArray {
        val pixelHeight = top - bottom

        val letterIndex = char.i - 32
        val letterPoint = Vec2i((letterIndex%columns) * letterWidth, texture.height - ((letterIndex/columns) * letterHeight))
        val letterSize = Vec2(getCharWidth(char).f / columns, pixelHeight/ texture.height)
        val texturePos = Vec2(letterPoint.x.f / texture.width, (letterPoint.y - top) / texture.height)

        val height = pixelHeight / letterWidth
        return createTextVertices(texturePos, texturePos + letterSize, getCharWidth(char), height)
    }

    fun createTextVertices(topLeft: Vec2, bottomRight: Vec2, width: Float, height: Float) : FloatArray {
        return Shape.floatArrayOf(
            // Positions    Texture
            0.0f, 0.0f, topLeft.x, topLeft.y,
            width, 0.0f, bottomRight.x, topLeft.y,
            width, height, bottomRight.x, bottomRight.y,
            0.0f, height, topLeft.x, bottomRight.y,
        )
    }

    private fun getPixelSize(text: String): Vec4 {
        val w = getPixelWidth(text)
        var min = letterHeight
        var max = 0
        for(it in text){
            val v = getDimensions(it) ?: continue
            min = min(min, v.y)
            max = max(max, v.w)
        }

        return Vec4(0, min - 5, w, max + 5)
    }

    fun getPixelWidth(text: String): Int{
        // Starting at 2 accounts for the margin at the beginning of the text
        return (getWidth(text) * letterWidth).i
    }
}