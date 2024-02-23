package com.pineypiney.game_engine.rendering.lighting

import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import glm_.vec3.Vec3

abstract class Light {

    abstract val ambient: Vec3
    abstract val diffuse: Vec3
    abstract val specular: Vec3

    open fun setShaderUniforms(uniforms: Uniforms, name: String){
        uniforms.setVec3Uniform("$name.ambient", ::ambient)
        uniforms.setVec3Uniform("$name.diffuse", ::diffuse)
        uniforms.setVec3Uniform("$name.specular", ::specular)
    }
}