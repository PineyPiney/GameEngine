package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.components.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import kotlin.math.min

open class Text(
    text: String,
    val colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
    var maxWidth: Float = 2f,
    var maxHeight: Float = 2f,
    val font: Font = Font.defaultFont,
    var italic: Float = 0f,
    var underlineThickness: Float = 0f,
    var underlineOffset: Float = -0.2f,
    var underlineAmount: Float = 1f,
    var fontSize: Float = 1f,
    var alignment: Int = ALIGN_CENTER_LEFT
) : Initialisable {

    var lines = arrayOf<String>()
    var lengths = floatArrayOf()
    var size: Float = 1f

    var quads: Array<TextQuad> = arrayOf()

    // Initialise as true so the text is generated before it's first render call
    var textChanged = true
    var text: String = text
        set(value) {
            field = value
            textChanged = true
        }

    open fun setIndividualUniforms(s: Shader, quad: TextQuad){

    }

    override fun init() {

    }

    fun updateLines(parentAspect: Float){
        if(fontSize > 0) size = fontSize
        else fitWithin(Vec2(maxWidth * parentAspect, maxHeight))

        lines = generateLines(parentAspect)
        lengths = lines.map { font.getWidth(it) }.toFloatArray()
        quads = font.getQuads(lines.joinToString("\n"), false).toTypedArray()
    }

    fun getWidth(): Float{
        return lines.maxOf { font.getWidth(it) } * size
    }

    fun getWidth(s: String): Float {
        return s.split('\n').maxOf { font.getWidth(s) } * size
    }

    fun getWidth(range: IntRange): Float {
        return getWidth(text.substring(range))
    }
    fun getHeight(): Float{
        return getHeight(lines.joinToString("\n"))
    }

    fun getHeight(s: String): Float{
        return font.getHeight(s) * size
    }

    fun fitWithin(bounds: Vec2){
        val fSize = font.getSize(text)
        val fits = bounds / fSize
        size = min(fits.x, fits.y)
    }

    fun generateLines(parentAspect: Float): Array<String>{
        val maxWidth = this.maxWidth * parentAspect
        val lines = mutableListOf<String>()
        var currentText = ""
        var i = 0
        var lastBreak = -1
        while(i < text.length){
            while(i < text.length - 1 && !" \n".contains(text[i])){
                i++
            }

            val word = text.slice(lastBreak+1..i)

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
        return when(alignment and 0xf){
            ALIGN_CENTER_H -> (totalWidth - getWidth(line.trim())) * 0.5f
            ALIGN_RIGHT -> totalWidth - getWidth(line.trim())
            else -> 0f
        }
    }

    override fun delete() {
        quads.toSet().delete()
    }

    override fun toString(): String {
        return "Text[\"$text\"]"
    }

    companion object{
        const val ALIGN_CENTER_H = 1
        const val ALIGN_LEFT = 2
        const val ALIGN_RIGHT = 4
        const val ALIGN_CENTER_V = 16
        const val ALIGN_TOP = 32
        const val ALIGN_BOTTOM = 64
        const val ALIGN_CENTER = ALIGN_CENTER_H or ALIGN_CENTER_V
        const val ALIGN_CENTER_LEFT = ALIGN_CENTER_V or ALIGN_LEFT
        const val ALIGN_CENTER_RIGHT = ALIGN_CENTER_V or ALIGN_RIGHT
        const val ALIGN_TOP_CENTER = ALIGN_TOP or ALIGN_CENTER_H
        const val ALIGN_BOTTOM_CENTER = ALIGN_BOTTOM or ALIGN_CENTER_H
        const val ALIGN_BOTTOM_LEFT = ALIGN_BOTTOM or ALIGN_LEFT
        const val ALIGN_BOTTOM_RIGHT = ALIGN_BOTTOM or ALIGN_RIGHT
        const val ALIGN_TOP_LEFT = ALIGN_TOP or ALIGN_LEFT
        const val ALIGN_TOP_RIGHT = ALIGN_TOP or ALIGN_RIGHT

        fun makeMenuText(
            text: String,
            colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
            maxWidth: Float = 2f,
            maxHeight: Float = 2f,
            fontSize: Float = 1f,
            alignment: Int = ALIGN_CENTER_LEFT,
            shader: Shader = Font.fontShader,
            font: Font = Font.defaultFont,
            italic: Float = 0f,
            underlineThickness: Float = 0f,
            underlineOffset: Float = -0.2f,
            underlineAmount: Float = 1f,
        ): GameObject{
            return object : MenuItem() {

                override fun addComponents() {
                    super.addComponents()
                    components.add(TextRendererComponent(this, Text(text, colour, maxWidth, maxHeight, font, italic, underlineThickness, underlineOffset, underlineAmount, fontSize, alignment), shader))
                }
            }
        }

        fun makeGameText(
            text: String,
            colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
            maxWidth: Float = 2f,
            maxHeight: Float = 2f,
            fontSize: Float = 1f,
            alignment: Int = ALIGN_CENTER_LEFT,
            shader: Shader = TextRendererComponent.gameTextShader,
            font: Font = Font.defaultFont,
            italic: Float = 0f,
            underlineThickness: Float = 0f,
            underlineOffset: Float = -0.2f,
            underlineAmount: Float = 1f,
        ): GameObject{
            return object : GameObject() {

                override fun addComponents() {
                    super.addComponents()
                    components.add(TextRendererComponent(this, Text(text, colour, maxWidth, maxHeight, font, italic, underlineThickness, underlineOffset, underlineAmount, fontSize, alignment), shader))
                }
            }
        }
    }
}