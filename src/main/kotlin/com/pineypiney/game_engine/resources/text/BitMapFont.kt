package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import glm_.f
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i

class BitMapFont(val texture: Texture, private val charDimensions: Map<Char, Vec4i>, val letterWidth: Int = 32, val letterHeight: Int = 64, val characterSpacing: Int = 2, val rows: Int = 16, val columns: Int = 16, override val shader: Shader = fontShader): Font() {

    val letterSize = Vec2(letterWidth.f / texture.width, letterHeight.f / texture.height)

    // The dimension of each character is defined as Vec4(min x, min y, max x, max y)
    // Given that (0, 0) is the top left of each letter's box

    fun getDimensions(char: Char): Vec4i? = charDimensions[char]

    override fun getCharWidth(char: Char): Int = (getDimensions(char)?.z ?: 0)
    override fun getCharHeight(char: Char): Vec2i = Vec2i(getDimensions(char)?.y ?: 31, getDimensions(char)?.w ?: 31)
    override fun getPixelWidth(text: String): Int{
        // Starting at 2 accounts for the margin at the beginning of the text
        return characterSpacing + text.sumOf { getCharWidth(it) + characterSpacing }
    }

    companion object{
        val fontShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/text"))
        val defaultFont: BitMapFont; get() = FontLoader[(ResourceKey("Large Font"))] as BitMapFont
    }
}