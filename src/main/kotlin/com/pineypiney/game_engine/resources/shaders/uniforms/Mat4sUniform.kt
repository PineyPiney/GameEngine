package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.mat4x4.Mat4

class Mat4sUniform(name: String, default: Array<Mat4> = arrayOf(), getter: () -> Array<Mat4>? = {arrayOf()}): Uniform<Array<Mat4>>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setMat4s(name, value)
    }
}