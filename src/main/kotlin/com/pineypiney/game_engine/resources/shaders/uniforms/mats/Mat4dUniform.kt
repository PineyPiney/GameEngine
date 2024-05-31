package com.pineypiney.game_engine.resources.shaders.uniforms.mats

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import glm_.mat4x4.Mat4d

class Mat4dUniform(name: String, default: Mat4d = Mat4d(), getter: (RendererI<*>) -> Mat4d? = {Mat4d()}): Uniform<Mat4d>(name, default, getter) {

    override fun apply(shader: Shader, renderer: RendererI<*>) {
        shader.setMat4d(name, getValue(renderer))
    }
}