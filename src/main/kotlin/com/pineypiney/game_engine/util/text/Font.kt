package com.pineypiney.game_engine.util.text

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4i

class Font(fontKey: ResourceKey, val letterWidth: Int = 32, val letterHeight: Int = 64, val characterSpacing: Int = 2, val rows: Int = 16, val columns: Int = 16, val shader: Shader? = null) {

    val texture = TextureLoader.getTexture(fontKey)

    // The dimension of each character is defined as Vec4(min x, min y, max x, max y)
    // Given that (0, 0) is the top left of each letter's box
    private val charDimensions: MutableMap<Char, Vec4i> = mutableMapOf()

    fun setChar(char: Char, dimensions: Vec4i){
        charDimensions[char] = dimensions
    }

    fun getDimensions(char: Char): Vec4i? = charDimensions[char]

    companion object{
        val emptyFont = Font(ResourceKey("fonts\\broke"), 64, 16, 4, 1, 1)
        val defaultFont: Font; get() = FontLoader.getFont(ResourceKey("fonts\\Large Font"))
        val fontShader = ShaderLoader.getShader(ResourceKey("vertex\\menu"), ResourceKey("fragment\\text"))
    }
}