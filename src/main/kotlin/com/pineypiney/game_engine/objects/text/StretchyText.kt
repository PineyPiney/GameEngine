package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.text.Font
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import kotlin.math.max
import kotlin.math.min

abstract class StretchyText(text: String, colour: Vec4 = Vec4(1, 1, 1, 1),
                            textMaxWidth: Float = 2f, textMaxHeight: Float = 2f,
                            font: Font = Font.defaultFont,
                            shader: Shader = gameTextShader):
    Text(text, colour, textMaxWidth, textMaxHeight, font, shader), StretchyTextI {

    // dimensions is Vec4(0, min y, pixel width, max y). min y and max y are used so that all letters are lined up vertically
    private var dimensions = Vec4()
    var size: Vec2 = Vec2()

    override var pixelHeight = 0f

    final override var letterIndices: List<Int> = listOf()
    override var letterPoints: List<Vec2i> = listOf()
    override var letterSize: List<Vec2> = listOf()
    override var quads: Array<TextQuad> = arrayOf()

    override fun init() {

        dimensions = getPixelSize()
        size = Vec2(dimensions.z, dimensions.w - dimensions.y)
        pixelHeight = dimensions.w - dimensions.y

        letterIndices = text.map{ it.i - 32 }
        letterPoints = letterIndices.map { index ->
            Vec2i((index%font.columns) * font.letterWidth, font.texture.height - ((index/font.columns) * font.letterHeight))
        }
        letterSize = text.map{ char ->
            Vec2(getCharWidth(char).f / font.texture.width, -pixelHeight.f/ font.texture.height)
        }
        quads = Array(text.length) { i ->
            val texturePos = Vec2(letterPoints[i].x.f / font.texture.width, (letterPoints[i].y - dimensions.y) / font.texture.height)
            TextQuad(texturePos, texturePos + (letterSize[i]))
        }
    }

    private fun getPixelSize(): Vec4 {
        val w = getPixelWidth(text)
        var min = font.letterHeight
        var max = 0
        text.forEach  ret@{
            val v = font.getDimensions(it)?: return@ret
            min = min(min, v.y)
            max = max(max, v.w)
        }

        return Vec4(0, min - 5, w, max + 5)
    }
}