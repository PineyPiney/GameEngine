package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.vec2.Vec2i

abstract class Font {

    abstract val shader: Shader

    abstract fun getCharWidth(char: Char): Int
    abstract fun getCharHeight(char: Char): Vec2i
    abstract fun getPixelWidth(text: String): Int
}