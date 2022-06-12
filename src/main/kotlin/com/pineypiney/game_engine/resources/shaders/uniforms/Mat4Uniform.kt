package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.mat4x4.Mat4

class Mat4Uniform(name: String, default: Mat4 = Mat4(), getter: () -> Mat4? = {Mat4()}): Uniform<Mat4>(name, default, getter) {

    override fun apply(shader: Shader) {
        shader.setMat4(name, value)
    }
}