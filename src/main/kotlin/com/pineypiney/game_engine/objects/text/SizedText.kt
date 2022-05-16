package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.text.Font
import glm_.c
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4

abstract class SizedText(text: String, final override val fontSize: Int = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                maxWidth: Float = 2f, maxHeight: Float = 2f,
                override val separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = font.shader, window: Window = Window.INSTANCE):
    Text(text, colour, maxWidth, maxHeight, font, shader, window), SizedTextI {

    final override val letterIndices: List<Int> = text.replace("\n", "").map{ it.i - 32 }
    override val letterPoints: List<Vec2i> = letterIndices.map { index ->
        Vec2i((index%font.columns) * font.letterWidth, font.texture.height - ((index/font.columns) * font.letterHeight))
    }
    override val letterSize: List<Vec2> = letterIndices.map{ char ->
        Vec2(getCharWidth((char + 32).c).f / font.texture.width, -font.letterSize.y)
    }
    override val quads: Array<TextQuad> = Array(letterIndices.size) { i ->
        val texturePos = Vec2(letterPoints[i]) / font.texture.size
        TextQuad(texturePos, texturePos + (letterSize[i]))
    }

    override var lines = arrayOf<String>()
    override var lengths = floatArrayOf()

}