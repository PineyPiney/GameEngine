package com.pineypiney.game_engine.rendering.lighting

import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import glm_.vec3.Vec3

class PointLight(val position: Vec3, override val ambient: Vec3 = Vec3(0.1f), override val diffuse: Vec3 = Vec3(0.5f), override val specular: Vec3 = Vec3(1f), val constant: Float = 1f, val linear: Float = .09f, val quadratic: Float = .032f): Light() {

    override fun setShaderUniforms(uniforms: Uniforms, name: String) {
        super.setShaderUniforms(uniforms, name)
        uniforms.setVec3Uniform("$name.position", ::position)
        uniforms.setFloatUniform("$name.constant", ::constant)
        uniforms.setFloatUniform("$name.linear", ::linear)
        uniforms.setFloatUniform("$name.quadratic", ::quadratic)
    }
}