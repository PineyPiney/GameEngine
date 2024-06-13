package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.uniforms.mats.*
import com.pineypiney.game_engine.resources.shaders.uniforms.vecs.*
import glm_.mat2x2.Mat2
import glm_.mat2x2.Mat2d
import glm_.mat2x3.Mat2x3
import glm_.mat2x3.Mat2x3d
import glm_.mat2x4.Mat2x4
import glm_.mat2x4.Mat2x4d
import glm_.mat3x2.Mat3x2
import glm_.mat3x2.Mat3x2d
import glm_.mat3x3.Mat3
import glm_.mat3x3.Mat3d
import glm_.mat3x4.Mat3x4
import glm_.mat3x4.Mat3x4d
import glm_.mat4x2.Mat4x2
import glm_.mat4x2.Mat4x2d
import glm_.mat4x3.Mat4x3
import glm_.mat4x3.Mat4x3d
import glm_.mat4x4.Mat4
import glm_.mat4x4.Mat4d
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

    fun setBoolsUniform(name: String, getter: () -> BooleanArray){
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

    fun setIntsUniform(name: String, getter: () -> IntArray){
        set<IntsUniform, IntArray>(name, getter)
    }

    fun setUIntUniformR(name: String, getter: (RendererI<*>) -> UInt){
        set<UIntUniform, UInt>(name, getter)
    }

    fun setUIntUniform(name: String, getter: () -> UInt){
        set<UIntUniform, UInt>(name, getter)
    }

    fun setUIntsUniformR(name: String, getter: (RendererI<*>) -> IntArray){
        set<UIntsUniform, IntArray>(name, getter)
    }

    fun setUIntsUniform(name: String, getter: () -> IntArray){
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

    fun setFloatsUniform(name: String, getter: () -> FloatArray){
        set<FloatsUniform, FloatArray>(name, getter)
    }

    fun setDoubleUniformR(name: String, getter: (RendererI<*>) -> Double){
        set<DoubleUniform, Double>(name, getter)
    }

    fun setDoubleUniform(name: String, getter: () -> Double){
        set<DoubleUniform, Double>(name, getter)
    }

    fun setDoublesUniformR(name: String, getter: (RendererI<*>) -> DoubleArray){
        set<DoublesUniform, DoubleArray>(name, getter)
    }

    fun setDoublesUniform(name: String, getter: () -> DoubleArray){
        set<DoublesUniform, DoubleArray>(name, getter)
    }


    fun setVec2iUniformR(name: String, getter: (RendererI<*>) -> Vec2t<*>){
        set<Vec2iUniform, Vec2t<*>>(name, getter)
    }
    fun setVec2iUniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2iUniform, Vec2t<*>>(name, getter)
    }
    fun setVec2uiUniformR(name: String, getter: (RendererI<*>) -> Vec2t<*>){
        set<Vec2uiUniform, Vec2t<*>>(name, getter)
    }
    fun setVec2uiUniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2uiUniform, Vec2t<*>>(name, getter)
    }
    fun setVec2UniformR(name: String, getter: (RendererI<*>) -> Vec2t<*>){
        set<Vec2Uniform, Vec2t<*>>(name, getter)
    }
    fun setVec2Uniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2Uniform, Vec2t<*>>(name, getter)
    }
    fun setVec2dUniformR(name: String, getter: (RendererI<*>) -> Vec2t<*>){
        set<Vec2dUniform, Vec2t<*>>(name, getter)
    }
    fun setVec2dUniform(name: String, getter: () -> Vec2t<*>){
        set<Vec2dUniform, Vec2t<*>>(name, getter)
    }
    fun setVec2isUniformR(name: String, getter: (RendererI<*>) -> List<Vec2t<*>>){
        set<Vec2isUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2isUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2isUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2uisUniformR(name: String, getter: (RendererI<*>) -> List<Vec2t<*>>){
        set<Vec2uisUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2uisUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2uisUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2sUniformR(name: String, getter: (RendererI<*>) -> List<Vec2t<*>>){
        set<Vec2sUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2sUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2sUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2dsUniformR(name: String, getter: (RendererI<*>) -> List<Vec2t<*>>){
        set<Vec2dsUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec2dsUniform(name: String, getter: () -> List<Vec2t<*>>){
        set<Vec2dsUniform, List<Vec2t<*>>>(name, getter)
    }
    fun setVec3iUniformR(name: String, getter: (RendererI<*>) -> Vec3t<*>){
        set<Vec3iUniform, Vec3t<*>>(name, getter)
    }
    fun setVec3iUniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3iUniform, Vec3t<*>>(name, getter)
    }
    fun setVec3uiUniformR(name: String, getter: (RendererI<*>) -> Vec3t<*>){
        set<Vec3uiUniform, Vec3t<*>>(name, getter)
    }
    fun setVec3uiUniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3uiUniform, Vec3t<*>>(name, getter)
    }
    fun setVec3UniformR(name: String, getter: (RendererI<*>) -> Vec3t<*>){
        set<Vec3Uniform, Vec3t<*>>(name, getter)
    }
    fun setVec3Uniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3Uniform, Vec3t<*>>(name, getter)
    }
    fun setVec3dUniformR(name: String, getter: (RendererI<*>) -> Vec3t<*>){
        set<Vec3dUniform, Vec3t<*>>(name, getter)
    }
    fun setVec3dUniform(name: String, getter: () -> Vec3t<*>){
        set<Vec3dUniform, Vec3t<*>>(name, getter)
    }
    fun setVec3isUniformR(name: String, getter: (RendererI<*>) -> List<Vec3t<*>>){
        set<Vec3isUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3isUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3isUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3uisUniformR(name: String, getter: (RendererI<*>) -> List<Vec3t<*>>){
        set<Vec3uisUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3uisUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3uisUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3sUniformR(name: String, getter: (RendererI<*>) -> List<Vec3t<*>>){
        set<Vec3sUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3sUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3sUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3dsUniformR(name: String, getter: (RendererI<*>) -> List<Vec3t<*>>){
        set<Vec3dsUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec3dsUniform(name: String, getter: () -> List<Vec3t<*>>){
        set<Vec3dsUniform, List<Vec3t<*>>>(name, getter)
    }
    fun setVec4iUniformR(name: String, getter: (RendererI<*>) -> Vec4t<*>){
        set<Vec4iUniform, Vec4t<*>>(name, getter)
    }
    fun setVec4iUniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4iUniform, Vec4t<*>>(name, getter)
    }
    fun setVec4uiUniformR(name: String, getter: (RendererI<*>) -> Vec4t<*>){
        set<Vec4uiUniform, Vec4t<*>>(name, getter)
    }
    fun setVec4uiUniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4uiUniform, Vec4t<*>>(name, getter)
    }
    fun setVec4UniformR(name: String, getter: (RendererI<*>) -> Vec4t<*>){
        set<Vec4Uniform, Vec4t<*>>(name, getter)
    }
    fun setVec4Uniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4Uniform, Vec4t<*>>(name, getter)
    }
    fun setVec4dUniformR(name: String, getter: (RendererI<*>) -> Vec4t<*>){
        set<Vec4dUniform, Vec4t<*>>(name, getter)
    }
    fun setVec4dUniform(name: String, getter: () -> Vec4t<*>){
        set<Vec4dUniform, Vec4t<*>>(name, getter)
    }
    fun setVec4isUniformR(name: String, getter: (RendererI<*>) -> List<Vec4t<*>>){
        set<Vec4isUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4isUniform(name: String, getter: () -> List<Vec4t<*>>){
        set<Vec4isUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4uisUniformR(name: String, getter: (RendererI<*>) -> List<Vec4t<*>>){
        set<Vec4uisUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4uisUniform(name: String, getter: () -> List<Vec4t<*>>){
        set<Vec4uisUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4sUniformR(name: String, getter: (RendererI<*>) -> List<Vec4t<*>>){
        set<Vec4sUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4sUniform(name: String, getter: () -> List<Vec4t<*>>){
        set<Vec4sUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4dsUniformR(name: String, getter: (RendererI<*>) -> List<Vec4t<*>>){
        set<Vec4dsUniform, List<Vec4t<*>>>(name, getter)
    }
    fun setVec4dsUniform(name: String, getter: () -> List<Vec4t<*>>){
        set<Vec4dsUniform, List<Vec4t<*>>>(name, getter)
    }




    fun setMat2UniformR(name: String, getter: (RendererI<*>) -> Mat2){
        set<Mat2Uniform, Mat2>(name, getter)
    }
    fun setMat2Uniform(name: String, getter: () -> Mat2){
        set<Mat2Uniform, Mat2>(name, getter)
    }
    fun setMat2dUniformR(name: String, getter: (RendererI<*>) -> Mat2d){
        set<Mat2dUniform, Mat2d>(name, getter)
    }
    fun setMat2dUniform(name: String, getter: () -> Mat2d){
        set<Mat2dUniform, Mat2d>(name, getter)
    }
    fun setMat2sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat2>){
        set<Mat2sUniform, Array<Mat2>>(name, getter)
    }
    fun setMat2sUniform(name: String, getter: () -> Array<Mat2>){
        set<Mat2sUniform, Array<Mat2>>(name, getter)
    }
    fun setMat2dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat2d>){
        set<Mat2dsUniform, Array<Mat2d>>(name, getter)
    }
    fun setMat2dsUniform(name: String, getter: () -> Array<Mat2d>){
        set<Mat2dsUniform, Array<Mat2d>>(name, getter)
    }
    fun setMat2x3UniformR(name: String, getter: (RendererI<*>) -> Mat2x3){
        set<Mat2x3Uniform, Mat2x3>(name, getter)
    }
    fun setMat2x3Uniform(name: String, getter: () -> Mat2x3){
        set<Mat2x3Uniform, Mat2x3>(name, getter)
    }
    fun setMat2x3dUniformR(name: String, getter: (RendererI<*>) -> Mat2x3d){
        set<Mat2x3dUniform, Mat2x3d>(name, getter)
    }
    fun setMat2x3dUniform(name: String, getter: () -> Mat2x3d){
        set<Mat2x3dUniform, Mat2x3d>(name, getter)
    }
    fun setMat2x3sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat2x3>){
        set<Mat2x3sUniform, Array<Mat2x3>>(name, getter)
    }
    fun setMat2x3sUniform(name: String, getter: () -> Array<Mat2x3>){
        set<Mat2x3sUniform, Array<Mat2x3>>(name, getter)
    }
    fun setMat2x3dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat2x3d>){
        set<Mat2x3dsUniform, Array<Mat2x3d>>(name, getter)
    }
    fun setMat2x3dsUniform(name: String, getter: () -> Array<Mat2x3d>){
        set<Mat2x3dsUniform, Array<Mat2x3d>>(name, getter)
    }
    fun setMat2x4UniformR(name: String, getter: (RendererI<*>) -> Mat2x4){
        set<Mat2x4Uniform, Mat2x4>(name, getter)
    }
    fun setMat2x4Uniform(name: String, getter: () -> Mat2x4){
        set<Mat2x4Uniform, Mat2x4>(name, getter)
    }
    fun setMat2x4dUniformR(name: String, getter: (RendererI<*>) -> Mat2x4d){
        set<Mat2x4dUniform, Mat2x4d>(name, getter)
    }
    fun setMat2x4dUniform(name: String, getter: () -> Mat2x4d){
        set<Mat2x4dUniform, Mat2x4d>(name, getter)
    }
    fun setMat2x4sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat2x4>){
        set<Mat2x4sUniform, Array<Mat2x4>>(name, getter)
    }
    fun setMat2x4sUniform(name: String, getter: () -> Array<Mat2x4>){
        set<Mat2x4sUniform, Array<Mat2x4>>(name, getter)
    }
    fun setMat2x4dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat2x4d>){
        set<Mat2x4dsUniform, Array<Mat2x4d>>(name, getter)
    }
    fun setMat2x4dsUniform(name: String, getter: () -> Array<Mat2x4d>){
        set<Mat2x4dsUniform, Array<Mat2x4d>>(name, getter)
    }
    fun setMat3x2UniformR(name: String, getter: (RendererI<*>) -> Mat3x2){
        set<Mat3x2Uniform, Mat3x2>(name, getter)
    }
    fun setMat3x2Uniform(name: String, getter: () -> Mat3x2){
        set<Mat3x2Uniform, Mat3x2>(name, getter)
    }
    fun setMat3x2dUniformR(name: String, getter: (RendererI<*>) -> Mat3x2d){
        set<Mat3x2dUniform, Mat3x2d>(name, getter)
    }
    fun setMat3x2dUniform(name: String, getter: () -> Mat3x2d){
        set<Mat3x2dUniform, Mat3x2d>(name, getter)
    }
    fun setMat3x2sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat3x2>){
        set<Mat3x2sUniform, Array<Mat3x2>>(name, getter)
    }
    fun setMat3x2sUniform(name: String, getter: () -> Array<Mat3x2>){
        set<Mat3x2sUniform, Array<Mat3x2>>(name, getter)
    }
    fun setMat3x2dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat3x2d>){
        set<Mat3x2dsUniform, Array<Mat3x2d>>(name, getter)
    }
    fun setMat3x2dsUniform(name: String, getter: () -> Array<Mat3x2d>){
        set<Mat3x2dsUniform, Array<Mat3x2d>>(name, getter)
    }
    fun setMat3UniformR(name: String, getter: (RendererI<*>) -> Mat3){
        set<Mat3Uniform, Mat3>(name, getter)
    }
    fun setMat3Uniform(name: String, getter: () -> Mat3){
        set<Mat3Uniform, Mat3>(name, getter)
    }
    fun setMat3dUniformR(name: String, getter: (RendererI<*>) -> Mat3d){
        set<Mat3dUniform, Mat3d>(name, getter)
    }
    fun setMat3dUniform(name: String, getter: () -> Mat3d){
        set<Mat3dUniform, Mat3d>(name, getter)
    }
    fun setMat3sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat3>){
        set<Mat3sUniform, Array<Mat3>>(name, getter)
    }
    fun setMat3sUniform(name: String, getter: () -> Array<Mat3>){
        set<Mat3sUniform, Array<Mat3>>(name, getter)
    }
    fun setMat3dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat3d>){
        set<Mat3dsUniform, Array<Mat3d>>(name, getter)
    }
    fun setMat3dsUniform(name: String, getter: () -> Array<Mat3d>){
        set<Mat3dsUniform, Array<Mat3d>>(name, getter)
    }
    fun setMat3x4UniformR(name: String, getter: (RendererI<*>) -> Mat3x4){
        set<Mat3x4Uniform, Mat3x4>(name, getter)
    }
    fun setMat3x4Uniform(name: String, getter: () -> Mat3x4){
        set<Mat3x4Uniform, Mat3x4>(name, getter)
    }
    fun setMat3x4dUniformR(name: String, getter: (RendererI<*>) -> Mat3x4d){
        set<Mat3x4dUniform, Mat3x4d>(name, getter)
    }
    fun setMat3x4dUniform(name: String, getter: () -> Mat3x4d){
        set<Mat3x4dUniform, Mat3x4d>(name, getter)
    }
    fun setMat3x4sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat3x4>){
        set<Mat3x4sUniform, Array<Mat3x4>>(name, getter)
    }
    fun setMat3x4sUniform(name: String, getter: () -> Array<Mat3x4>){
        set<Mat3x4sUniform, Array<Mat3x4>>(name, getter)
    }
    fun setMat3x4dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat3x4d>){
        set<Mat3x4dsUniform, Array<Mat3x4d>>(name, getter)
    }
    fun setMat3x4dsUniform(name: String, getter: () -> Array<Mat3x4d>){
        set<Mat3x4dsUniform, Array<Mat3x4d>>(name, getter)
    }
    fun setMat4x2UniformR(name: String, getter: (RendererI<*>) -> Mat4x2){
        set<Mat4x2Uniform, Mat4x2>(name, getter)
    }
    fun setMat4x2Uniform(name: String, getter: () -> Mat4x2){
        set<Mat4x2Uniform, Mat4x2>(name, getter)
    }
    fun setMat4x2dUniformR(name: String, getter: (RendererI<*>) -> Mat4x2d){
        set<Mat4x2dUniform, Mat4x2d>(name, getter)
    }
    fun setMat4x2dUniform(name: String, getter: () -> Mat4x2d){
        set<Mat4x2dUniform, Mat4x2d>(name, getter)
    }
    fun setMat4x2sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4x2>){
        set<Mat4x2sUniform, Array<Mat4x2>>(name, getter)
    }
    fun setMat4x2sUniform(name: String, getter: () -> Array<Mat4x2>){
        set<Mat4x2sUniform, Array<Mat4x2>>(name, getter)
    }
    fun setMat4x2dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4x2d>){
        set<Mat4x2dsUniform, Array<Mat4x2d>>(name, getter)
    }
    fun setMat4x2dsUniform(name: String, getter: () -> Array<Mat4x2d>){
        set<Mat4x2dsUniform, Array<Mat4x2d>>(name, getter)
    }
    fun setMat4x3UniformR(name: String, getter: (RendererI<*>) -> Mat4x3){
        set<Mat4x3Uniform, Mat4x3>(name, getter)
    }
    fun setMat4x3Uniform(name: String, getter: () -> Mat4x3){
        set<Mat4x3Uniform, Mat4x3>(name, getter)
    }
    fun setMat4x3dUniformR(name: String, getter: (RendererI<*>) -> Mat4x3d){
        set<Mat4x3dUniform, Mat4x3d>(name, getter)
    }
    fun setMat4x3dUniform(name: String, getter: () -> Mat4x3d){
        set<Mat4x3dUniform, Mat4x3d>(name, getter)
    }
    fun setMat4x3sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4x3>){
        set<Mat4x3sUniform, Array<Mat4x3>>(name, getter)
    }
    fun setMat4x3sUniform(name: String, getter: () -> Array<Mat4x3>){
        set<Mat4x3sUniform, Array<Mat4x3>>(name, getter)
    }
    fun setMat4x3dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4x3d>){
        set<Mat4x3dsUniform, Array<Mat4x3d>>(name, getter)
    }
    fun setMat4x3dsUniform(name: String, getter: () -> Array<Mat4x3d>){
        set<Mat4x3dsUniform, Array<Mat4x3d>>(name, getter)
    }
    fun setMat4UniformR(name: String, getter: (RendererI<*>) -> Mat4){
        set<Mat4Uniform, Mat4>(name, getter)
    }
    fun setMat4Uniform(name: String, getter: () -> Mat4){
        set<Mat4Uniform, Mat4>(name, getter)
    }
    fun setMat4dUniformR(name: String, getter: (RendererI<*>) -> Mat4d){
        set<Mat4dUniform, Mat4d>(name, getter)
    }
    fun setMat4dUniform(name: String, getter: () -> Mat4d){
        set<Mat4dUniform, Mat4d>(name, getter)
    }
    fun setMat4sUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4>){
        set<Mat4sUniform, Array<Mat4>>(name, getter)
    }
    fun setMat4sUniform(name: String, getter: () -> Array<Mat4>){
        set<Mat4sUniform, Array<Mat4>>(name, getter)
    }
    fun setMat4dsUniformR(name: String, getter: (RendererI<*>) -> Array<Mat4d>){
        set<Mat4dsUniform, Array<Mat4d>>(name, getter)
    }
    fun setMat4dsUniform(name: String, getter: () -> Array<Mat4d>){
        set<Mat4dsUniform, Array<Mat4d>>(name, getter)
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

    fun allValues(rendererI: RendererI<*>): Map<String, Any?>{
        return uniforms.associate { it.name to it.getter(rendererI) }
    }

    companion object{
        val default = Uniforms(arrayOf())
    }
}