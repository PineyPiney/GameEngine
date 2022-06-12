package com.pineypiney.game_engine.objects

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms

interface Shaded {
    val shader: Shader
    val uniforms: Uniforms

    fun setUniforms()
}