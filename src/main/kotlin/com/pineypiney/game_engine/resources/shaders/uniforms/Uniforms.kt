package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2t
import glm_.vec3.Vec3t
import glm_.vec4.Vec4t

class Uniforms(val uniforms: Array<Uniform<*>>) {

    fun setBoolUniformR(name: String, getter: (RendererI<*>) -> Boolean){
        set<BoolUniform, Boolean>(name, getter)
    }

    fun setBoolUniform(name: String, getter: () -> Boolean){
        set<BoolUniform, Boolean>(name, getter)
    }

    fun setBoolsUniformR(name: String, getter: (RendererI<*>) -> BooleanArray){
        set<BoolsUniform, BooleanArray>(name, getter)
    }

    fun setIntUniformR(name: String, getter: (RendererI<*>) -> Int){
        set<IntUniform, Int>(name, getter)
    }

    fun setIntUniform(name: String, getter: () -> Int){
        set<IntUniform, Int>(name, getter)
    }

    fun setIntsUniformR(name: String, getter: (RendererI<*>) -> IntArray){
        set<IntsUniform, IntArray>(name, getter)
    }

    fun setUIntUniformR(name: String, getter: (RendererI<*>) -> UInt){
        set<UIntUniform, UInt>(name, getter)
    }

    fun setUIntsUniformR(name: String, getter: (RendererI<*>) -> IntArray){
        set<UIntsUniform, IntArray>(name, getter)
    }

    fun setFloatUniformR(name: String, getter: (RendererI<*>) -> Float){
        set<FloatUniform, Float>(name, getter)
    }

    fun setFloatUniform(name: String, getter: () -> Float){
        set<FloatUniform, Float>(name, getter)
    }

    fun setFloatsUniformR(name: String, getter: (RendererI<*>) -> FloatArray){
        set<FloatsUniform, FloatArray>(name, getter)
    }

    fun setVec2UniformR(name: String, getter: (RendererI<*>) -> Vec2t<*>){
        set<Vec2Uniform, Vec2t<*>>(name, getter)
    }

    fun setVec2Uniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2Uniform, Vec2t<*>>(name, getter)
    }

    fun setVec2iUniformR(name: String, getter: (RendererI<*>) -> Vec2t<*>){
        set<Vec2iUniform, Vec2t<*>>(name, getter)
    }

    fun setVec2iUniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2iUniform, Vec2t<*>>(name, getter)
    }

    fun setVec2sUniformR(name: String, getter: (RendererI<*>) -> List<Vec2t<*>>){
        set<Vec2sUniform, List<Vec2t<*>>>(name, getter)
    }

    fun setVec2sUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2sUniform, List<Vec2t<*>>>(name, getter)
    }

    fun setVec2isUniformR(name: String, getter: (RendererI<*>) -> List<Vec2t<*>>){
        set<Vec2isUniform, List<Vec2t<*>>>(name, getter)
    }

    fun setVec3UniformR(name: String, getter: (RendererI<*>) -> Vec3t<*>){
        set<Vec3Uniform, Vec3t<*>>(name, getter)
    }

    fun setVec3Uniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3Uniform, Vec3t<*>>(name, getter)
    }

    fun setVec3iUniformR(name: String, getter: (RendererI<*>) -> Vec3t<*>){
        set<Vec3iUniform, Vec3t<*>>(name, getter)
    }

    fun setVec3sUniformR(name: String, getter: (RendererI<*>) -> List<Vec3t<*>>){
        set<Vec3sUniform, List<Vec3t<*>>>(name, getter)
    }

    fun setVec3sUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3sUniform, List<Vec3t<*>>>(name, getter)
    }

    fun setVec3isUniformR(name: String, getter: (RendererI<*>) -> List<Vec3t<*>>){
        set<Vec3isUniform, List<Vec3t<*>>>(name, getter)
    }

    fun setVec4UniformR(name: String, getter: (RendererI<*>) -> Vec4t<*>){
        set<Vec4Uniform, Vec4t<*>>(name, getter)
    }

    fun setVec4Uniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4Uniform, Vec4t<*>>(name, getter)
    }

    fun setVeci4UniformR(name: String, getter: (RendererI<*>) -> Vec4t<*>){
        set<Vec4iUniform, Vec4t<*>>(name, getter)
    }

    fun setVec4sUniformR(name: String, getter: (RendererI<*>) -> List<Vec4t<*>>){
        set<Vec4sUniform, List<Vec4t<*>>>(name, getter)
    }

    fun setVec4isUniformR(name: String, getter: (RendererI<*>) -> List<Vec4t<*>>){
        set<Vec4isUniform, List<Vec4t<*>>>(name, getter)
    }

    fun setMat4UniformR(name: String, getter: (RendererI<*>) -> Mat4){
        set<Mat4Uniform, Mat4>(name, getter)
    }

    fun setMat4Uniform(name: String, getter: () -> Mat4){
        set<Mat4Uniform, Mat4>(name, getter)
    }

    fun setMat4sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4>){
        set<Mat4sUniform, Array<Mat4>>(name, getter)
    }

    fun setMat4sUniform(name: String, getter: () -> Array<Mat4>){
        set<Mat4sUniform, Array<Mat4>>(name, getter)
    }

    inline fun <reified U: Uniform<E>, E> set(name: String, noinline getter: (RendererI<*>) -> E){
        val uniform = this[name] ?: return
        if(uniform is U){
            uniform.getter = getter
        }
    }

    inline fun <reified U: Uniform<E>, E> set(name: String, noinline getter: () -> E){
        val uniform = this[name] ?: return
        if(uniform is U){
            uniform.getter = { getter() }
        }
    }

    operator fun get(name: String) = uniforms.firstOrNull{ it.name == name }

    override fun toString(): String {
        return "Uniforms[" + uniforms.joinToString { it.name } + ']'
    }

    companion object{
        val default = Uniforms(arrayOf())
    }
}