package com.pineypiney.game_engine.resources.shaders.opengl

import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.opengl.OpenGlMesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture
import glm_.i
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
import org.lwjgl.opengl.*
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer

abstract class OpenGlShader(private var ID: Int, val uniforms: Map<String, String>) : Shader {

	override fun use(api: RenderingApi) {
		GL20C.glUseProgram(ID)
	}

	// Functions to set uniforms within shaders

	override fun setTexture(name: String, texture: Texture) {
		if (texture is OpenGlTexture) {
			setInt(name, texture.binding)
			texture.bind()
		}
	}

	override fun setTexture(binding: Int, texture: Texture) {
		(texture as OpenGlTexture).bind(binding)
	}

	override fun setMesh(name: String, mesh: Mesh) {
		(mesh as OpenGlMesh).bind()
	}

	override fun setBool(name: String, value: Boolean) = set1(name, value.i, GL46C::glUniform1i)
	override fun setBools(name: String, values: BooleanArray) = set1(name, values.map { it.i }.toIntArray(), GL46C::glUniform1iv)

	override fun setInt(name: String, value: Int) = set1(name, value, GL46C::glUniform1i)
	override fun setInts(name: String, values: IntBuffer) = set1(name, values, GL46C::glUniform1iv)

	override fun setUInt(name: String, value: UInt) = set1(name, value.toInt(), GL46C::glUniform1ui)
	override fun setUInts(name: String, values: IntBuffer) = set1(name, values, GL46C::glUniform1uiv)

	override fun setLong(name: String, value: Long) = set1(name, value, ARBGPUShaderInt64::glUniform1i64ARB)
	override fun setLongs(name: String, values: LongBuffer) = set1(name, values, ARBGPUShaderInt64::glUniform1i64vARB)

	override fun setULong(name: String, value: ULong) = set1(name, value.toLong(), ARBGPUShaderInt64::glUniform1ui64ARB)
	override fun setULongs(name: String, values: LongBuffer) = set1(name, values, ARBGPUShaderInt64::glUniform1ui64vARB)

	override fun setFloat(name: String, value: Float) = set1(name, value, GL46C::glUniform1f)
	override fun setFloats(name: String, values: FloatBuffer) = set1(name, values, GL46C::glUniform1fv)

	override fun setDouble(name: String, value: Double) = set1(name, value, GL46C::glUniform1d)
	override fun setDoubles(name: String, values: DoubleBuffer) = set1(name, values, GL46C::glUniform1dv)

	override fun setVec2i(name: String, v: Vec2t<*>) = set2(name, Vec2i(v), GL46C::glUniform2i)
	override fun setVec2i(name: String, x: Number, y: Number) = set2(name, Vec2i(x, y), GL46C::glUniform2i)
	override fun setVec2ui(name: String, v: Vec2t<*>) = set2(name, Vec2i(v.x.toInt(), v.y.toInt()), GL46C::glUniform2ui)
	override fun setVec2ui(name: String, x: Number, y: Number) = set2(name, Vec2i(x.toInt(), y.toInt()), GL46C::glUniform2ui)

	override fun setVec2(name: String, v: Vec2t<*>) = set2(name, Vec2(v), GL46C::glUniform2f)
	override fun setVec2(name: String, x: Number, y: Number) = set2(name, Vec2(x, y), GL46C::glUniform2f)
	override fun setVec2d(name: String, v: Vec2t<*>) = set2(name, Vec2d(v), GL46C::glUniform2d)
	override fun setVec2d(name: String, x: Number, y: Number) = set2(name, Vec2d(x, y), GL46C::glUniform2d)

	override fun setVec3i(name: String, v: Vec3t<*>) = set3(name, Vec3i(v), GL46C::glUniform3i)
	override fun setVec3i(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3i(x, y, z), GL46C::glUniform3i)
	override fun setVec3ui(name: String, v: Vec3t<*>) = set3(name, Vec3i(v), GL46C::glUniform3ui)
	override fun setVec3ui(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3i(x, y, z), GL46C::glUniform3ui)

	override fun setVec3(name: String, v: Vec3t<*>) = set3(name, Vec3(v), GL46C::glUniform3f)
	override fun setVec3(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3(x, y, z), GL46C::glUniform3f)
	override fun setVec3d(name: String, v: Vec3t<*>) = set3(name, Vec3d(v), GL46C::glUniform3d)
	override fun setVec3d(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3d(x, y, z), GL46C::glUniform3d)

	override fun setVec4i(name: String, v: Vec4t<*>) = set4(name, Vec4i(v), GL46C::glUniform4i)
	override fun setVec4i(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4i(x, y, z, w), GL46C::glUniform4i)
	override fun setVec4ui(name: String, v: Vec4t<*>) = set4(name, Vec4i(v), GL46C::glUniform4ui)
	override fun setVec4ui(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4i(x, y, z, w), GL46C::glUniform4ui)

	override fun setVec4(name: String, v: Vec4t<*>) = set4(name, Vec4(v), GL46C::glUniform4f)
	override fun setVec4(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4(x, y, z, w), GL46C::glUniform4f)
	override fun setVec4d(name: String, v: Vec4t<*>) = set4(name, Vec4d(v), GL46C::glUniform4d)
	override fun setVec4d(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4d(x, y, z, w), GL46C::glUniform4d)

	override fun setVec2is(name: String, values: Array<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2iv)
	override fun setVec2uis(name: String, values: Array<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2uiv)
	override fun setVec2s(name: String, values: Array<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat()) }.toFloatArray(), GL46C::glUniform2fv)
	override fun setVec2ds(name: String, values: Array<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble()) }.toDoubleArray(), GL46C::glUniform2dv)

	override fun setVec2is(name: String, values: List<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2iv)
	override fun setVec2uis(name: String, values: List<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2uiv)
	override fun setVec2s(name: String, values: List<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat()) }.toFloatArray(), GL46C::glUniform2fv)
	override fun setVec2ds(name: String, values: List<Vec2t<*>>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble()) }.toDoubleArray(), GL46C::glUniform2dv)

	override fun setVec3is(name: String, values: Array<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3iv)
	override fun setVec3uis(name: String, values: Array<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3uiv)
	override fun setVec3s(name: String, values: Array<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }.toFloatArray(), GL46C::glUniform3fv)
	override fun setVec3ds(name: String, values: Array<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }.toDoubleArray(), GL46C::glUniform3dv)

	override fun setVec3is(name: String, values: List<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3iv)
	override fun setVec3uis(name: String, values: List<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3uiv)
	override fun setVec3s(name: String, values: List<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }.toFloatArray(), GL46C::glUniform3fv)
	override fun setVec3ds(name: String, values: List<Vec3t<*>>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }.toDoubleArray(), GL46C::glUniform3dv)

	override fun setVec4is(name: String, values: Array<Vec4t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4iv)
	override fun setVec4uis(name: String, values: Array<Vec4t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4uiv)
	override fun setVec4s(name: String, values: Array<Vec4t<*>>) =
		set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat(), it.w.toFloat()) }.toFloatArray(), GL46C::glUniform4fv)

	override fun setVec4ds(name: String, values: Array<Vec4t<*>>) =
		set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.w.toDouble()) }.toDoubleArray(), GL46C::glUniform4dv)

	override fun setVec4is(name: String, values: List<Vec4t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4iv)
	override fun setVec4uis(name: String, values: List<Vec4t<*>>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4uiv)
	override fun setVec4s(name: String, values: List<Vec4t<*>>) =
		set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat(), it.w.toFloat()) }.toFloatArray(), GL46C::glUniform4fv)

	override fun setVec4ds(name: String, values: List<Vec4t<*>>) =
		set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.w.toDouble()) }.toDoubleArray(), GL46C::glUniform4dv)

	override fun setMat2(name: String, value: Mat2) = setMatrix(name, value.array, GL46C::glUniformMatrix2fv)
	override fun setMat2d(name: String, value: Mat2d) = setMatrix(name, value.array, GL46C::glUniformMatrix2dv)
	override fun setMat2s(name: String, value: Array<Mat2>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix2fv)
	override fun setMat2ds(name: String, value: Array<Mat2d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix2dv)

	override fun setMat2x3(name: String, value: Mat2x3) = setMatrix(name, value.array, GL46C::glUniformMatrix2x3fv)
	override fun setMat2x3d(name: String, value: Mat2x3d) = setMatrix(name, value.array, GL46C::glUniformMatrix2x3dv)
	override fun setMat2x3s(name: String, value: Array<Mat2x3>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix2x3fv)
	override fun setMat2x3ds(name: String, value: Array<Mat2x3d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix2x3dv)

	override fun setMat2x4(name: String, value: Mat2x4) = setMatrix(name, value.array, GL46C::glUniformMatrix2x4fv)
	override fun setMat2x4d(name: String, value: Mat2x4d) = setMatrix(name, value.array, GL46C::glUniformMatrix2x4dv)
	override fun setMat2x4s(name: String, value: Array<Mat2x4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix2x4fv)
	override fun setMat2x4ds(name: String, value: Array<Mat2x4d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix2x4dv)

	override fun setMat3x2(name: String, value: Mat3x2) = setMatrix(name, value.array, GL46C::glUniformMatrix3x2fv)
	override fun setMat3x2d(name: String, value: Mat3x2d) = setMatrix(name, value.array, GL46C::glUniformMatrix3x2dv)
	override fun setMat3x2s(name: String, value: Array<Mat3x2>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix3x2fv)
	override fun setMat3x2ds(name: String, value: Array<Mat3x2d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix3x2dv)

	override fun setMat3(name: String, value: Mat3) = setMatrix(name, value.array, GL46C::glUniformMatrix3fv)
	override fun setMat3d(name: String, value: Mat3d) = setMatrix(name, value.array, GL46C::glUniformMatrix3dv)
	override fun setMat3s(name: String, value: Array<Mat3>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix3fv)
	override fun setMat3ds(name: String, value: Array<Mat3d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix3dv)

	override fun setMat3x4(name: String, value: Mat3x4) = setMatrix(name, value.array, GL46C::glUniformMatrix3x4fv)
	override fun setMat3x4d(name: String, value: Mat3x4d) = setMatrix(name, value.array, GL46C::glUniformMatrix3x4dv)
	override fun setMat3x4s(name: String, value: Array<Mat3x4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix3x4fv)
	override fun setMat3x4ds(name: String, value: Array<Mat3x4d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix3x4dv)

	override fun setMat4x2(name: String, value: Mat4x2) = setMatrix(name, value.array, GL46C::glUniformMatrix4x2fv)
	override fun setMat4x2d(name: String, value: Mat4x2d) = setMatrix(name, value.array, GL46C::glUniformMatrix4x2dv)
	override fun setMat4x2s(name: String, value: Array<Mat4x2>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix4x2fv)
	override fun setMat4x2ds(name: String, value: Array<Mat4x2d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix4x2dv)

	override fun setMat4x3(name: String, value: Mat4x3) = setMatrix(name, value.array, GL46C::glUniformMatrix4x3fv)
	override fun setMat4x3d(name: String, value: Mat4x3d) = setMatrix(name, value.array, GL46C::glUniformMatrix4x3dv)
	override fun setMat4x3s(name: String, value: Array<Mat4x3>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix4x3fv)
	override fun setMat4x3ds(name: String, value: Array<Mat4x3d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix4x3dv)

	override fun setMat4(name: String, value: Mat4) = setMatrix(name, value.array, GL46C::glUniformMatrix4fv)
	override fun setMat4d(name: String, value: Mat4d) = setMatrix(name, value.array, GL46C::glUniformMatrix4dv)
	override fun setMat4s(name: String, value: Array<Mat4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix4fv)
	override fun setMat4ds(name: String, value: Array<Mat4d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix4dv)


	fun <E> set1(name: String, v: E, func: (Int, E) -> Unit) {
		val varLocation = getVar(name)
		func(varLocation, v)
	}

	fun <E : Number> set2(name: String, v: Vec2t<E>, func: (Int, E, E) -> Unit) {
		val varLocation = getVar(name)
		func(varLocation, v.x, v.y)
	}

	fun <E : Number> set3(name: String, v: Vec3t<E>, func: (Int, E, E, E) -> Unit) {
		val varLocation = getVar(name)
		func(varLocation, v.x, v.y, v.z)
	}

	fun <E : Number> set4(name: String, v: Vec4t<E>, func: (Int, E, E, E, E) -> Unit) {
		val varLocation = getVar(name)
		func(varLocation, v.x, v.y, v.z, v.w)
	}

	fun <E> setMatrix(name: String, v: E, func: (Int, Boolean, E) -> Unit) {
		val varLocation = getVar(name)
		func(varLocation, false, v)
	}

	override fun getBool(name: String): Boolean = GL20C.glGetUniformi(ID, getVar(name)) != 0

	override fun getInt(name: String) = get1(name, GL20C::glGetUniformi)
	override fun getInts(name: String, dst: IntBuffer) = get1s(name, dst, GL20C::glGetUniformiv)

	override fun getUInt(name: String) = get1(name, GL30C::glGetUniformui).toUInt()
	override fun getUInts(name: String, dst: IntBuffer) = get1s(name, dst, GL20C::glGetUniformiv)

	override fun getLong(name: String) = get1(name, ARBGPUShaderInt64::glGetUniformi64vARB)
	override fun getLongs(name: String, dst: LongBuffer) = get1s(name, dst, ARBGPUShaderInt64::glGetUniformi64vARB)

	override fun getFloat(name: String) = get1(name, GL20C::glGetUniformf)
	override fun getFloats(name: String, dst: FloatBuffer) = get1s(name, dst, GL20C::glGetUniformfv)

	override fun getDouble(name: String) = get1(name, GL40C::glGetUniformd)
	override fun getDoubles(name: String, dst: DoubleBuffer) = get1s(name, dst, GL40C::glGetUniformdv)

	override fun getVec2i(name: String): Vec2i = Vec2i(getIN(name, 2))
	override fun getVec2ui(name: String): Vec2ui = Vec2ui(getUIN(name, 2))
	override fun getVec2(name: String): Vec2 = Vec2(getFN(name, 2))
	override fun getVec2d(name: String): Vec2d = Vec2d(getDN(name, 2))
	override fun getVec2is(name: String, size: Int): Array<Vec2i> = getIN(name, 2 * size).let { a -> Array(size) { i -> Vec2i(a[i * 2], a[i * 2 + 1]) } }
	override fun getVec2uis(name: String, size: Int): Array<Vec2ui> = getUIN(name, 2 * size).let { a -> Array(size) { i -> Vec2ui(a[i * 2], a[i * 2 + 1]) } }
	override fun getVec2s(name: String, size: Int): Array<Vec2> = getFN(name, 2 * size).let { a -> Array(size) { i -> Vec2(a[i * 2], a[i * 2 + 1]) } }
	override fun getVec2ds(name: String, size: Int): Array<Vec2d> = getDN(name, 2 * size).let { a -> Array(size) { i -> Vec2d(a[i * 2], a[i * 2 + 1]) } }
	override fun getVec3i(name: String): Vec3i = Vec3i(getIN(name, 3))
	override fun getVec3ui(name: String): Vec3ui = Vec3ui(getUIN(name, 3))
	override fun getVec3(name: String): Vec3 = Vec3(getFN(name, 3))
	override fun getVec3d(name: String): Vec3d = Vec3d(getDN(name, 3))
	override fun getVec3is(name: String, size: Int): Array<Vec3i> = getIN(name, 3 * size).let { a -> Array(size) { i -> Vec3i(a[i * 3], a[i * 3 + 1], a[i * 3 + 2]) } }
	override fun getVec3uis(name: String, size: Int): Array<Vec3ui> = getUIN(name, 3 * size).let { a -> Array(size) { i -> Vec3ui(a[i * 3], a[i * 3 + 1], a[i * 3 + 2]) } }
	override fun getVec3s(name: String, size: Int): Array<Vec3> = getFN(name, 3 * size).let { a -> Array(size) { i -> Vec3(a[i * 3], a[i * 3 + 1], a[i * 3 + 2]) } }
	override fun getVec3ds(name: String, size: Int): Array<Vec3d> = getDN(name, 3 * size).let { a -> Array(size) { i -> Vec3d(a[i * 3], a[i * 3 + 1], a[i * 3 + 2]) } }
	override fun getVec4i(name: String): Vec4i = Vec4i(getIN(name, 4))
	override fun getVec4ui(name: String): Vec4ui = Vec4ui(getUIN(name, 4))
	override fun getVec4(name: String): Vec4 = Vec4(getFN(name, 4))
	override fun getVec4d(name: String): Vec4d = Vec4d(getDN(name, 4))
	override fun getVec4is(name: String, size: Int): Array<Vec4i> = getIN(name, 4 * size).let { a -> Array(size) { i -> Vec4i(a[i * 4], a[i * 4 + 1], a[i * 4 + 2], a[i * 4 + 3]) } }
	override fun getVec4uis(name: String, size: Int): Array<Vec4ui> = getUIN(name, 4 * size).let { a -> Array(size) { i -> Vec4ui(a[i * 4], a[i * 4 + 1], a[i * 4 + 2], a[i * 4 + 3]) } }
	override fun getVec4s(name: String, size: Int): Array<Vec4> = getFN(name, 4 * size).let { a -> Array(size) { i -> Vec4(a[i * 4], a[i * 4 + 1], a[i * 4 + 2], a[i * 4 + 3]) } }
	override fun getVec4ds(name: String, size: Int): Array<Vec4d> = getDN(name, 4 * size).let { a -> Array(size) { i -> Vec4d(a[i * 4], a[i * 4 + 1], a[i * 4 + 2], a[i * 4 + 3]) } }
	override fun getMat2(name: String): Mat2 = Mat2(getFN(name, 4))
	override fun getMat2d(name: String): Mat2d = Mat2d(getDN(name, 4))
	override fun getMat2s(name: String, size: Int): Array<Mat2> = getFN(name, 4 * size).let { a -> Array(size) { i -> Mat2(a.sliceArray((i * 4)..(i * 4 + 3))) } }
	override fun getMat2ds(name: String, size: Int): Array<Mat2d> = getDN(name, 4 * size).let { a -> Array(size) { i -> Mat2d(a.sliceArray((i * 4)..(i * 4 + 3))) } }
	override fun getMat2x3(name: String): Mat2x3 = Mat2x3(getFN(name, 6))
	override fun getMat2x3d(name: String): Mat2x3d = Mat2x3d(getDN(name, 6))
	override fun getMat2x3s(name: String, size: Int): Array<Mat2x3> = getFN(name, 6 * size).let { a -> Array(size) { i -> Mat2x3(a.sliceArray((i * 6)..(i * 6 + 5))) } }
	override fun getMat2x3ds(name: String, size: Int): Array<Mat2x3d> = getDN(name, 6 * size).let { a -> Array(size) { i -> Mat2x3d(a.sliceArray((i * 6)..(i * 6 + 5))) } }
	override fun getMat2x4(name: String): Mat2x4 = Mat2x4(getFN(name, 8))
	override fun getMat2x4d(name: String): Mat2x4d = Mat2x4d(getDN(name, 8))
	override fun getMat2x4s(name: String, size: Int): Array<Mat2x4> = getFN(name, 8 * size).let { a -> Array(size) { i -> Mat2x4(a.sliceArray((i * 8)..(i * 8 + 7))) } }
	override fun getMat2x4ds(name: String, size: Int): Array<Mat2x4d> = getDN(name, 8 * size).let { a -> Array(size) { i -> Mat2x4d(a.sliceArray((i * 8)..(i * 8 + 7))) } }
	override fun getMat3x2(name: String): Mat3x2 = Mat3x2(getFN(name, 6))
	override fun getMat3x2d(name: String): Mat3x2d = Mat3x2d(getDN(name, 6))
	override fun getMat3x2s(name: String, size: Int): Array<Mat3x2> = getFN(name, 6 * size).let { a -> Array(size) { i -> Mat3x2(a.sliceArray((i * 6)..(i * 6 + 5))) } }
	override fun getMat3x2ds(name: String, size: Int): Array<Mat3x2d> = getDN(name, 6 * size).let { a -> Array(size) { i -> Mat3x2d(a.sliceArray((i * 6)..(i * 6 + 5))) } }
	override fun getMat3(name: String): Mat3 = Mat3(getFN(name, 9))
	override fun getMat3d(name: String): Mat3d = Mat3d(getDN(name, 9))
	override fun getMat3s(name: String, size: Int): Array<Mat3> = getFN(name, 9 * size).let { a -> Array(size) { i -> Mat3(a.sliceArray((i * 9)..(i * 9 + 8))) } }
	override fun getMat3ds(name: String, size: Int): Array<Mat3d> = getDN(name, 9 * size).let { a -> Array(size) { i -> Mat3d(a.sliceArray((i * 9)..(i * 9 + 8))) } }
	override fun getMat3x4(name: String): Mat3x4 = Mat3x4(getFN(name, 12))
	override fun getMat3x4d(name: String): Mat3x4d = Mat3x4d(getDN(name, 12))
	override fun getMat3x4s(name: String, size: Int): Array<Mat3x4> = getFN(name, 12 * size).let { a -> Array(size) { i -> Mat3x4(a.sliceArray((i * 12)..(i * 12 + 11))) } }
	override fun getMat3x4ds(name: String, size: Int): Array<Mat3x4d> = getDN(name, 12 * size).let { a -> Array(size) { i -> Mat3x4d(a.sliceArray((i * 12)..(i * 12 + 11))) } }
	override fun getMat4x2(name: String): Mat4x2 = Mat4x2(getFN(name, 8))
	override fun getMat4x2d(name: String): Mat4x2d = Mat4x2d(getDN(name, 8))
	override fun getMat4x2s(name: String, size: Int): Array<Mat4x2> = getFN(name, 8 * size).let { a -> Array(size) { i -> Mat4x2(a.sliceArray((i * 8)..(i * 8 + 7))) } }
	override fun getMat4x2ds(name: String, size: Int): Array<Mat4x2d> = getDN(name, 8 * size).let { a -> Array(size) { i -> Mat4x2d(a.sliceArray((i * 8)..(i * 8 + 7))) } }
	override fun getMat4x3(name: String): Mat4x3 = Mat4x3(getFN(name, 12))
	override fun getMat4x3d(name: String): Mat4x3d = Mat4x3d(getDN(name, 12))
	override fun getMat4x3s(name: String, size: Int): Array<Mat4x3> = getFN(name, 12 * size).let { a -> Array(size) { i -> Mat4x3(a.sliceArray((i * 12)..(i * 12 + 11))) } }
	override fun getMat4x3ds(name: String, size: Int): Array<Mat4x3d> = getDN(name, 12 * size).let { a -> Array(size) { i -> Mat4x3d(a.sliceArray((i * 12)..(i * 12 + 11))) } }
	override fun getMat4(name: String): Mat4 = Mat4(getFN(name, 16))
	override fun getMat4d(name: String): Mat4d = Mat4d(getDN(name, 16))
	override fun getMat4s(name: String, size: Int): Array<Mat4> = getFN(name, 16 * size).let { a -> Array(size) { i -> Mat4(a.sliceArray((i * 16)..(i * 16 + 15))) } }
	override fun getMat4ds(name: String, size: Int): Array<Mat4d> = getDN(name, 16 * size).let { a -> Array(size) { i -> Mat4d(a.sliceArray((i * 16)..(i * 16 + 15))) } }

	fun <E> get1(name: String, func: (Int, Int) -> E): E {
		return func(ID, getVar(name))
	}

	fun <E> get1s(name: String, array: E, func: (Int, Int, E) -> Unit) {
		func(ID, getVar(name), array)
	}

	fun getIN(name: String, num: Int): IntArray {
		val array = IntArray(num)
		GL20C.glGetUniformiv(ID, getVar(name), array)
		return array
	}

	fun getUIN(name: String, num: Int): IntArray {
		val array = IntArray(num)
		GL30C.glGetUniformuiv(ID, getVar(name), array)
		return array
	}

	fun getFN(name: String, num: Int): FloatArray {
		val array = FloatArray(num)
		GL20C.glGetUniformfv(ID, getVar(name), array)
		return array
	}

	fun getDN(name: String, num: Int): DoubleArray {
		val array = DoubleArray(num)
		GL40C.glGetUniformdv(ID, getVar(name), array)
		return array
	}

	override fun compileUniforms(): Uniforms {
		val set = mutableSetOf<Uniform<*>>()
		for ((name, type) in uniforms) {
			set.add(Uniform.parse(name, type) ?: continue)
		}
		return Uniforms(set.toTypedArray())
	}

	private fun getVar(name: String): Int = GL20C.glGetUniformLocation(ID, name)

	fun getNumberAttributes() = GL20C.glGetProgrami(ID, GL20C.GL_ACTIVE_ATTRIBUTES)
	fun getNumberAttributesNew() = GL43C.glGetProgramInterfacei(ID, GL43C.GL_PROGRAM_INPUT, GL43C.GL_ACTIVE_RESOURCES)
	fun getNumberUniforms() = GL20C.glGetProgrami(ID, GL20C.GL_ACTIVE_UNIFORMS)
	fun getNumberUniformsNew() = GL43C.glGetProgramInterfacei(ID, GL43C.GL_UNIFORM, GL43C.GL_ACTIVE_RESOURCES)

	fun getAllAttributes(): Array<String> {
		return Array(getNumberAttributes()) { i -> GL43C.glGetProgramResourceName(ID, GL43C.GL_PROGRAM_INPUT, i) }
	}

	fun getAllUniforms(): Array<String> {
		return Array(getNumberUniforms()) { i -> GL43C.glGetProgramResourceName(ID, GL43C.GL_UNIFORM, i) }
	}

	override fun delete() {
		GL20C.glDeleteProgram(ID)
	}
}