package com.pineypiney.game_engine.resources.shaders.uniforms

import glm_.mat4x4.Mat4
import glm_.vec2.Vec2t
import glm_.vec3.Vec3t
import glm_.vec4.Vec4t

class Uniforms(val uniforms: Array<Uniform<*>>) {

    fun setBoolUniform(name: String, getter: () -> Boolean){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is BoolUniform){
            uniform.getter = getter
        }
    }

    fun setBoolsUniform(name: String, getter: () -> BooleanArray){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is BoolsUniform){
            uniform.getter = getter
        }
    }

    fun setIntUniform(name: String, getter: () -> Int){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is IntUniform){
            uniform.getter = getter
        }
    }

    fun setIntsUniform(name: String, getter: () -> IntArray){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is IntsUniform){
            uniform.getter = getter
        }
    }

    fun setFloatUniform(name: String, getter: () -> Float){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is FloatUniform){
            uniform.getter = getter
        }
    }

    fun setFloatsUniform(name: String, getter: () -> FloatArray){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is FloatsUniform){
            uniform.getter = getter
        }
    }

    fun setVec2Uniform(name: String, getter: () -> Vec2t<*>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Vec2Uniform){
            uniform.getter = getter
        }
    }

    fun setVec2sUniform(name: String, getter: () -> Array<Vec2t<*>>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Vec2sUniform){
            uniform.getter = getter
        }
    }

    fun setVec3Uniform(name: String, getter: () -> Vec3t<*>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Vec3Uniform){
            uniform.getter = getter
        }
    }

    fun setVec3sUniform(name: String, getter: () -> Array<Vec3t<*>>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Vec3sUniform){
            uniform.getter = getter
        }
    }

    fun setVec4Uniform(name: String, getter: () -> Vec4t<*>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Vec4Uniform){
            uniform.getter = getter
        }
    }

    fun setVec4sUniform(name: String, getter: () -> Array<Vec4t<*>>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Vec4sUniform){
            uniform.getter = getter
        }
    }

    fun setMat4Uniform(name: String, getter: () -> Mat4){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Mat4Uniform){
            uniform.getter = getter
        }
    }

    fun setMat4sUniform(name: String, getter: () -> Array<Mat4>){
        val uniform = uniforms.firstOrNull{it.name == name} ?: return
        if(uniform is Mat4sUniform){
            uniform.getter = getter
        }
    }

    companion object{
        val default = Uniforms(arrayOf())
    }
}