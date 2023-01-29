package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.util.shapes.TextQuad
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

abstract class Font {

    abstract val shader: Shader

    abstract fun getCharWidth(char: Char): Float
    abstract fun getCharHeight(char: Char): Vec2

    abstract fun getWidth(text: String): Float
    abstract fun getHeight(text: String): Float
    fun getSize(text: String): Vec2 = Vec2(getWidth(text), getHeight(text))

    abstract fun getQuads(text: String, line: Int): Array<TextQuad>

    companion object{
        val fontShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/text"))
        val defaultFont: Font; get() = FontLoader[ResourceKey(GameEngineI.defaultFont)]
    }
}