package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.resources.textures.Texture
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
import glm_.vec2.*
import glm_.vec3.*
import glm_.vec4.*
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer

interface Shader : Deletable {

	fun use(api: RenderingApi)

	fun setUniforms(uniforms: Uniforms, renderer: RendererI) {
		for (u in uniforms.uniforms) {
			u.apply(this, renderer)
		}
	}

	fun endUniforms(api: RenderingApi) {}

	fun setUp(uniforms: Uniforms, renderer: RendererI) {
		use(renderer.getRenderingApi())
		setUniforms(uniforms, renderer)
	}

	fun setVP(view: Mat4, projection: Mat4) {
		setMat4("view", view)
		setMat4("projection", projection)
	}

	fun setVP(renderer: RendererI) {
		setMat4("view", renderer.view)
		setMat4("projection", renderer.projection)
	}

	fun getModule(stage: ShaderStage): ShaderModule?

	// Functions to set uniforms within shaders

	fun setTexture(name: String, texture: Texture)

	fun setTexture(binding: Int, texture: Texture)

	fun setMesh(name: String, mesh: Mesh)

	fun setBool(name: String, value: Boolean)
	fun setBools(name: String, values: BooleanArray)

	fun setInt(name: String, value: Int)
	fun setInts(name: String, values: IntBuffer)

	fun setUInt(name: String, value: UInt)
	fun setUInts(name: String, values: IntBuffer)

	fun setLong(name: String, value: Long)
	fun setLongs(name: String, values: LongBuffer)

	fun setULong(name: String, value: ULong)
	fun setULongs(name: String, values: LongBuffer)

	fun setFloat(name: String, value: Float)
	fun setFloats(name: String, values: FloatBuffer)

	fun setDouble(name: String, value: Double)
	fun setDoubles(name: String, values: DoubleBuffer)

	fun setVec2i(name: String, v: Vec2t<*>)
	fun setVec2i(name: String, x: Number, y: Number)
	fun setVec2ui(name: String, v: Vec2t<*>)
	fun setVec2ui(name: String, x: Number, y: Number)

	fun setVec2(name: String, v: Vec2t<*>)
	fun setVec2(name: String, x: Number, y: Number)
	fun setVec2d(name: String, v: Vec2t<*>)
	fun setVec2d(name: String, x: Number, y: Number)

	fun setVec3i(name: String, v: Vec3t<*>)
	fun setVec3i(name: String, x: Number, y: Number, z: Number)
	fun setVec3ui(name: String, v: Vec3t<*>)
	fun setVec3ui(name: String, x: Number, y: Number, z: Number)

	fun setVec3(name: String, v: Vec3t<*>)
	fun setVec3(name: String, x: Number, y: Number, z: Number)
	fun setVec3d(name: String, v: Vec3t<*>)
	fun setVec3d(name: String, x: Number, y: Number, z: Number)

	fun setVec4i(name: String, v: Vec4t<*>)
	fun setVec4i(name: String, x: Number, y: Number, z: Number, w: Number)
	fun setVec4ui(name: String, v: Vec4t<*>)
	fun setVec4ui(name: String, x: Number, y: Number, z: Number, w: Number)

	fun setVec4(name: String, v: Vec4t<*>)
	fun setVec4(name: String, x: Number, y: Number, z: Number, w: Number)
	fun setVec4d(name: String, v: Vec4t<*>)
	fun setVec4d(name: String, x: Number, y: Number, z: Number, w: Number)

	fun setVec2is(name: String, values: Array<Vec2t<*>>)
	fun setVec2uis(name: String, values: Array<Vec2t<*>>)
	fun setVec2s(name: String, values: Array<Vec2t<*>>)
	fun setVec2ds(name: String, values: Array<Vec2t<*>>)

	fun setVec2is(name: String, values: List<Vec2t<*>>)
	fun setVec2uis(name: String, values: List<Vec2t<*>>)
	fun setVec2s(name: String, values: List<Vec2t<*>>)
	fun setVec2ds(name: String, values: List<Vec2t<*>>)

	fun setVec3is(name: String, values: Array<Vec3t<*>>)
	fun setVec3uis(name: String, values: Array<Vec3t<*>>)
	fun setVec3s(name: String, values: Array<Vec3t<*>>)
	fun setVec3ds(name: String, values: Array<Vec3t<*>>)

	fun setVec3is(name: String, values: List<Vec3t<*>>)
	fun setVec3uis(name: String, values: List<Vec3t<*>>)
	fun setVec3s(name: String, values: List<Vec3t<*>>)
	fun setVec3ds(name: String, values: List<Vec3t<*>>)

	fun setVec4is(name: String, values: Array<Vec4t<*>>)
	fun setVec4uis(name: String, values: Array<Vec4t<*>>)
	fun setVec4s(name: String, values: Array<Vec4t<*>>)
	fun setVec4ds(name: String, values: Array<Vec4t<*>>)

	fun setVec4is(name: String, values: List<Vec4t<*>>)
	fun setVec4uis(name: String, values: List<Vec4t<*>>)
	fun setVec4s(name: String, values: List<Vec4t<*>>)
	fun setVec4ds(name: String, values: List<Vec4t<*>>)


	fun setMat2(name: String, value: Mat2)
	fun setMat2d(name: String, value: Mat2d)
	fun setMat2s(name: String, value: Array<Mat2>)
	fun setMat2ds(name: String, value: Array<Mat2d>)

	fun setMat2x3(name: String, value: Mat2x3)
	fun setMat2x3d(name: String, value: Mat2x3d)
	fun setMat2x3s(name: String, value: Array<Mat2x3>)
	fun setMat2x3ds(name: String, value: Array<Mat2x3d>)

	fun setMat2x4(name: String, value: Mat2x4)
	fun setMat2x4d(name: String, value: Mat2x4d)
	fun setMat2x4s(name: String, value: Array<Mat2x4>)
	fun setMat2x4ds(name: String, value: Array<Mat2x4d>)

	fun setMat3x2(name: String, value: Mat3x2)
	fun setMat3x2d(name: String, value: Mat3x2d)
	fun setMat3x2s(name: String, value: Array<Mat3x2>)
	fun setMat3x2ds(name: String, value: Array<Mat3x2d>)

	fun setMat3(name: String, value: Mat3)
	fun setMat3d(name: String, value: Mat3d)
	fun setMat3s(name: String, value: Array<Mat3>)
	fun setMat3ds(name: String, value: Array<Mat3d>)

	fun setMat3x4(name: String, value: Mat3x4)
	fun setMat3x4d(name: String, value: Mat3x4d)
	fun setMat3x4s(name: String, value: Array<Mat3x4>)
	fun setMat3x4ds(name: String, value: Array<Mat3x4d>)

	fun setMat4x2(name: String, value: Mat4x2)
	fun setMat4x2d(name: String, value: Mat4x2d)
	fun setMat4x2s(name: String, value: Array<Mat4x2>)
	fun setMat4x2ds(name: String, value: Array<Mat4x2d>)

	fun setMat4x3(name: String, value: Mat4x3)
	fun setMat4x3d(name: String, value: Mat4x3d)
	fun setMat4x3s(name: String, value: Array<Mat4x3>)
	fun setMat4x3ds(name: String, value: Array<Mat4x3d>)

	fun setMat4(name: String, value: Mat4)
	fun setMat4d(name: String, value: Mat4d)
	fun setMat4s(name: String, value: Array<Mat4>)
	fun setMat4ds(name: String, value: Array<Mat4d>)


	fun getBool(name: String): Boolean

	fun getInt(name: String): Int
	fun getInts(name: String, dst: IntBuffer)

	fun getUInt(name: String): UInt
	fun getUInts(name: String, dst: IntBuffer)

	fun getLong(name: String): Long
	fun getLongs(name: String, dst: LongBuffer)

	fun getFloat(name: String): Float
	fun getFloats(name: String, dst: FloatBuffer)

	fun getDouble(name: String): Double
	fun getDoubles(name: String, dst: DoubleBuffer)

	fun getVec2i(name: String): Vec2i
	fun getVec2ui(name: String): Vec2ui
	fun getVec2(name: String): Vec2
	fun getVec2d(name: String): Vec2d
	fun getVec2is(name: String, size: Int): Array<Vec2i>
	fun getVec2uis(name: String, size: Int): Array<Vec2ui>
	fun getVec2s(name: String, size: Int): Array<Vec2>
	fun getVec2ds(name: String, size: Int): Array<Vec2d>
	fun getVec3i(name: String): Vec3i
	fun getVec3ui(name: String): Vec3ui
	fun getVec3(name: String): Vec3
	fun getVec3d(name: String): Vec3d
	fun getVec3is(name: String, size: Int): Array<Vec3i>
	fun getVec3uis(name: String, size: Int): Array<Vec3ui>
	fun getVec3s(name: String, size: Int): Array<Vec3>
	fun getVec3ds(name: String, size: Int): Array<Vec3d>
	fun getVec4i(name: String): Vec4i
	fun getVec4ui(name: String): Vec4ui
	fun getVec4(name: String): Vec4
	fun getVec4d(name: String): Vec4d
	fun getVec4is(name: String, size: Int): Array<Vec4i>
	fun getVec4uis(name: String, size: Int): Array<Vec4ui>
	fun getVec4s(name: String, size: Int): Array<Vec4>
	fun getVec4ds(name: String, size: Int): Array<Vec4d>
	fun getMat2(name: String): Mat2
	fun getMat2d(name: String): Mat2d
	fun getMat2s(name: String, size: Int): Array<Mat2>
	fun getMat2ds(name: String, size: Int): Array<Mat2d>
	fun getMat2x3(name: String): Mat2x3
	fun getMat2x3d(name: String): Mat2x3d
	fun getMat2x3s(name: String, size: Int): Array<Mat2x3>
	fun getMat2x3ds(name: String, size: Int): Array<Mat2x3d>
	fun getMat2x4(name: String): Mat2x4
	fun getMat2x4d(name: String): Mat2x4d
	fun getMat2x4s(name: String, size: Int): Array<Mat2x4>
	fun getMat2x4ds(name: String, size: Int): Array<Mat2x4d>
	fun getMat3x2(name: String): Mat3x2
	fun getMat3x2d(name: String): Mat3x2d
	fun getMat3x2s(name: String, size: Int): Array<Mat3x2>
	fun getMat3x2ds(name: String, size: Int): Array<Mat3x2d>
	fun getMat3(name: String): Mat3
	fun getMat3d(name: String): Mat3d
	fun getMat3s(name: String, size: Int): Array<Mat3>
	fun getMat3ds(name: String, size: Int): Array<Mat3d>
	fun getMat3x4(name: String): Mat3x4
	fun getMat3x4d(name: String): Mat3x4d
	fun getMat3x4s(name: String, size: Int): Array<Mat3x4>
	fun getMat3x4ds(name: String, size: Int): Array<Mat3x4d>
	fun getMat4x2(name: String): Mat4x2
	fun getMat4x2d(name: String): Mat4x2d
	fun getMat4x2s(name: String, size: Int): Array<Mat4x2>
	fun getMat4x2ds(name: String, size: Int): Array<Mat4x2d>
	fun getMat4x3(name: String): Mat4x3
	fun getMat4x3d(name: String): Mat4x3d
	fun getMat4x3s(name: String, size: Int): Array<Mat4x3>
	fun getMat4x3ds(name: String, size: Int): Array<Mat4x3d>
	fun getMat4(name: String): Mat4
	fun getMat4d(name: String): Mat4d
	fun getMat4s(name: String, size: Int): Array<Mat4>
	fun getMat4ds(name: String, size: Int): Array<Mat4d>

	fun compileUniforms(): Uniforms
}