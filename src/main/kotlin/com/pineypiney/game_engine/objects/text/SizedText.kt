package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.c
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4

abstract class SizedText(text: String, final override val fontSize: Int = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                maxWidth: Float = 2f, maxHeight: Float = 2f,
                override val separation: Float = 0.6f, font: Font = Font.defaultFont,
                shader: Shader = font.shader):
    Text(text, colour, maxWidth, maxHeight, font, shader), SizedTextI {

    final override var letterIndices: List<Int> = listOf()
    override var letterPoints: List<Vec2i> = listOf()
    override var letterSize: List<Vec2> = listOf()
    override var quads: Array<TextQuad> = arrayOf()

    override var lines = arrayOf<String>()
    override var lengths = floatArrayOf()

    override fun init() {
        super.init()
        letterIndices = text.replace("\n", "").map{ it.i - 32 }
        letterPoints = letterIndices.map { index ->
            Vec2i((index%font.columns) * font.letterWidth, font.texture.height - ((index/font.columns) * font.letterHeight))
        }
        letterSize = letterIndices.map{ char ->
            Vec2(getCharWidth((char + 32).c).f / font.texture.width, -font.letterSize.y)
        }
        quads = Array(letterIndices.size) { i ->
            val texturePos = Vec2(letterPoints[i]) / font.texture.size
            TextQuad(texturePos, texturePos + (letterSize[i]))
        }
        updateLines()
    }

    abstract fun updateLines()

    final override fun generateLines(): Array<String>{
        val lines = mutableListOf<String>()
        var currentText = ""
        var i = 0
        var lastBreak = -1
        while(i < text.length){
            while(i < text.length - 1 && !" \n".contains(text[i])){
                i++
            }

            val word = text.slice(lastBreak+1  ..  i)
            val lineWidth = getPixelWidth(currentText + word.removeSuffix(" "))

            val screenWidth = pixelToRelative(lineWidth)
            if(screenWidth > maxWidth) {
                lines.add(currentText)
                currentText = ""
            }

            if(text[i] == '\n'){
                lines.add(currentText + word.substring(0, word.length - 1))
                currentText = ""
            }
            else{
                currentText += word
            }

            lastBreak = i
            i++
        }
        lines.add(currentText)
        lines.removeAll { it.replaceWhiteSpaces() == "" }
        return lines.toTypedArray()
    }
}