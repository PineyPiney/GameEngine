package com.pineypiney.game_engine.resources.shaders.uniforms

import glm_.mat4x4.Mat4
import glm_.vec2.Vec2t
import glm_.vec3.Vec3t
import glm_.vec4.Vec4t

class Uniforms(val uniforms: Array<Uniform<*>>) {

    fun setBoolUniform(name: String, getter: () -> Boolean){
        set<BoolUniform, Boolean>(name, getter)
    }

    fun setBoolsUniform(name: String, getter: () -> BooleanArray){
        set<BoolsUniform, BooleanArray>(name, getter)
    }

    fun setIntUniform(name: String, getter: () -> Int){
        set<IntUniform, Int>(name, getter)
    }

    fun setIntsUniform(name: String, getter: () -> IntArray){
        set<IntsUniform, IntArray>(name, getter)
    }

    fun setUIntUniform(name: String, getter: () -> UInt){
        set<UIntUniform, UInt>(name, getter)
    }

    fun setUIntsUniform(name: String, getter: () -> IntArray){
        set<UIntsUniform, IntArray>(name, getter)
    }

    fun setFloatUniform(name: String, getter: () -> Float){
        set<FloatUniform, Float>(name, getter)
    }

    fun setFloatsUniform(name: String, getter: () -> FloatArray){
        set<FloatsUniform, FloatArray>(name, getter)
    }

    fun setVec2Uniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2Uniform, Vec2t<*>>(name, getter)
    }

    fun setVec2iUniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2iUniform, Vec2t<*>>(name, getter)
    }

    fun setVec2sUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2sUniform, List<Vec2t<*>>>(name, getter)
    }

    fun setVec2isUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2isUniform, List<Vec2t<*>>>(name, getter)
    }

    fun setVec3Uniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3Uniform, Vec3t<*>>(name, getter)
    }

    fun setVec3iUniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3iUniform, Vec3t<*>>(name, getter)
    }

    fun setVec3sUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3sUniform, List<Vec3t<*>>>(name, getter)
    }

    fun setVec3isUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3isUniform, List<Vec3t<*>>>(name, getter)
    }

    fun setVec4Uniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4Uniform, Vec4t<*>>(name, getter)
    }

    fun setVeci4Uniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4iUniform, Vec4t<*>>(name, getter)
    }

    fun setVec4sUniform(name: String, getter: () -> List<Vec4t<*>>){
        set<Vec4sUniform, List<Vec4t<*>>>(name, getter)
    }

    fun setVec4isUniform(name: String, getter: () -> List<Vec4t<*>>){
        set<Vec4isUniform, List<Vec4t<*>>>(name, getter)
    }

    fun setMat4Uniform(name: String, getter: () -> Mat4){
        set<Mat4Uniform, Mat4>(name, getter)
    }

    fun setMat4sUniform(name: String, getter: () -> Array<Mat4>){
        set<Mat4sUniform, Array<Mat4>>(name, getter)
    }

    inline fun <reified U: Uniform<E>, E> set(name: String, noinline getter: () -> E){
        val uniform = this[name] ?: return
        if(uniform is U){
            uniform.getter = getter
        }
    }


    operator fun get(name: String) = uniforms.firstOrNull{ it.name == name }

    companion object{
        val default = Uniforms(arrayOf())
    }
}