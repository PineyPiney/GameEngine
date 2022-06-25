package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.Shaded
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.delete
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4

interface TextI: Initialisable, Shaded {

    val text: String
    val colour: Vec4
    val maxWidth: Float
    val maxHeight: Float

    val font: Font

    val letterIndices: List<Int>
    val letterPoints: List<Vec2i>
    val letterSize: List<Vec2>
    val quads: Array<TextQuad>

    var defaultCharHeight: Float
    var defaultCharWidth: Float

    fun setDefaults(height: Float)

    fun pixelToRelative(pixel: Int): Float

    fun getCharWidth(char: Char): Int = (font.getDimensions(char)?.z ?: 0)
    fun getCharHeight(char: Char): Vec2i = Vec2i(font.getDimensions(char)?.y ?: 31, font.getDimensions(char)?.w ?: 31)

    fun getQuad(i: Int): TextQuad?{
        return quads.getOrNull(i)
    }

    /**
     * Add the widths of all letters together in terms of pixels,
     * leaving space in between each letter according to the font
     *
     * @param text The text to find the width of
     * @return The width in pixels
     */
    fun getPixelWidth(text: String): Int

    fun setUniversalUniforms(shader: Shader){
        shader.setVec4("colour", colour)
    }

    fun setIndividualUniforms(shader: Shader, index: Int){}

    override fun delete(){
        quads.toSet().delete()
    }
}