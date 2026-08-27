package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.mats.*
import com.pineypiney.game_engine.resources.shaders.uniforms.vecs.*

abstract class Uniform<E : Any>(val name: String, val default: E, var getter: UniformGetter<E>) {

	abstract fun apply(shader: Shader, renderer: RendererI)

	fun getValue(renderer: RendererI) = getter(renderer) ?: default

	override fun toString(): String {
		return "Uniform<${default::class.simpleName}>[$name]"
	}

	companion object {
		fun parse(name: String, type: String): Uniform<*>? {
			return if (name.contains('[') && name.contains(']')) {
				val newName = name.substringBefore('[') + "[0]"
				when (type) {
					"bool" -> BoolsUniform(newName)
					"int", "sampler2D" -> IntsUniform(newName)
					"uint" -> UIntsUniform(newName)
					"int64_t" -> LongsUniform(name)
					"uint64_t" -> ULongsUniform(name)
					"float" -> FloatsUniform(newName)
					"double" -> DoublesUniform(newName)

					"vec2" -> Vec2sUniform(newName)
					"vec3" -> Vec3sUniform(newName)
					"vec4" -> Vec4sUniform(newName)
					"ivec2" -> Vec2isUniform(newName)
					"ivec3" -> Vec3isUniform(newName)
					"ivec4" -> Vec4isUniform(newName)
					"uvec2" -> Vec2uisUniform(newName)
					"uvec3" -> Vec3uisUniform(newName)
					"uvec4" -> Vec4uisUniform(newName)
					"dvec2" -> Vec2dsUniform(newName)
					"dvec3" -> Vec3dsUniform(newName)
					"dvec4" -> Vec4dsUniform(newName)

					"mat2" -> Mat2sUniform(newName)
					"mat2x3" -> Mat2x3sUniform(newName)
					"mat2x4" -> Mat2x4sUniform(newName)
					"mat3x2" -> Mat3x2sUniform(newName)
					"mat3" -> Mat3sUniform(newName)
					"mat3x4" -> Mat3x4sUniform(newName)
					"mat4x2" -> Mat4x2sUniform(newName)
					"mat4x3" -> Mat4x3sUniform(newName)
					"mat4" -> Mat4sUniform(newName)

					"dmat2" -> Mat2dsUniform(newName)
					"dmat2x3" -> Mat2x3dsUniform(newName)
					"dmat2x4" -> Mat2x4dsUniform(newName)
					"dmat3x2" -> Mat3x2dsUniform(newName)
					"dmat3" -> Mat3dsUniform(newName)
					"dmat3x4" -> Mat3x4dsUniform(newName)
					"dmat4x2" -> Mat4x2dsUniform(newName)
					"dmat4x3" -> Mat4x3dsUniform(newName)
					"dmat4" -> Mat4dsUniform(newName)
					else -> null
				}
			} else {
				when (type) {
					"bool" -> BoolUniform(name)
					"int" -> IntUniform(name)
					"sampler2D", "image2D" -> TextureUniform(name)
					"uint" -> UIntUniform(name)
					"int64_t" -> LongUniform(name)
					"uint64_t" -> ULongUniform(name)
					"float" -> FloatUniform(name)
					"double" -> DoubleUniform(name)

					"vec2" -> Vec2Uniform(name)
					"vec3" -> Vec3Uniform(name)
					"vec4" -> Vec4Uniform(name)
					"ivec2" -> Vec2iUniform(name)
					"ivec3" -> Vec3iUniform(name)
					"ivec4" -> Vec4iUniform(name)
					"uvec2" -> Vec2uiUniform(name)
					"uvec3" -> Vec3uiUniform(name)
					"uvec4" -> Vec4uiUniform(name)
					"dvec2" -> Vec2dUniform(name)
					"dvec3" -> Vec3dUniform(name)
					"dvec4" -> Vec4dUniform(name)

					"mat2" -> Mat2Uniform(name)
					"mat2x3" -> Mat2x3Uniform(name)
					"mat2x4" -> Mat2x4Uniform(name)
					"mat3x2" -> Mat3x2Uniform(name)
					"mat3" -> Mat3Uniform(name)
					"mat3x4" -> Mat3x4Uniform(name)
					"mat4x2" -> Mat4x2Uniform(name)
					"mat4x3" -> Mat4x3Uniform(name)
					"mat4" -> Mat4Uniform(name)

					"dmat2" -> Mat2dUniform(name)
					"dmat2x3" -> Mat2x3dUniform(name)
					"dmat2x4" -> Mat2x4dUniform(name)
					"dmat3x2" -> Mat3x2dUniform(name)
					"dmat3" -> Mat3dUniform(name)
					"dmat3x4" -> Mat3x4dUniform(name)
					"dmat4x2" -> Mat4x2dUniform(name)
					"dmat4x3" -> Mat4x3dUniform(name)
					"dmat4" -> Mat4dUniform(name)
					else -> null
				}
			}
		}
	}
}

typealias UniformGetter<E> = (renderer: RendererI) -> E?