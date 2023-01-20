package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.vec4.Vec4

abstract class SizedText(text: String, override val fontSize: Int = 100, colour: Vec4 = Vec4(1, 1, 1, 1),
                         maxWidth: Float = 2f, maxHeight: Float = 2f,
                         override val separation: Float = 0.6f, font: Font = Font.defaultFont,
                         shader: Shader = font.shader):
    Text(text, colour, maxWidth, maxHeight, font, shader), SizedTextI {

    override var quads: Array<TextQuad> = arrayOf()

    override var lines = arrayOf<String>()
    override var lengths = floatArrayOf()
    override var alignment: Int = ALIGN_LEFT

    override fun init() {
        super.init()
        updateLines()
        quads = lines.mapIndexed { i, l -> font.getQuads(l, i).toList() }.flatten().toTypedArray()
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

            val width = getWidth(currentText + word.removeSuffix(" "))

            if(width > maxWidth) {
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

    fun getUnderlineOf(line: Int): Float{
        val underlineStart = lengths.copyOf(line).sum() / lengths.sum()
        val underlineEnd = underlineStart + (lengths[line] / lengths.sum())
        if(underlineAmount <= underlineStart) return 0f
        if(underlineAmount >= underlineEnd) return 1f
        return (underlineAmount - underlineStart) / (underlineEnd - underlineStart)
    }

    fun getAlignment(line: String, totalWidth: Float): Float{
        return when(alignment){
            ALIGN_CENTER -> (totalWidth - getWidth(line.trim())) * 0.5f
            ALIGN_RIGHT -> totalWidth - getWidth(line.trim())
            else -> 0f
        }
    }

    companion object{
        const val ALIGN_CENTER = 0
        const val ALIGN_LEFT = -1
        const val ALIGN_RIGHT = 1
    }
}