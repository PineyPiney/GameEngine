package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.uniforms.*
import com.pineypiney.game_engine.resources.shaders.uniforms.mats.*
import com.pineypiney.game_engine.resources.shaders.uniforms.vecs.*
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.RandomHelper
import glm_.b
import glm_.i
import glm_.mat2x2.*
import glm_.mat2x3.*
import glm_.mat2x4.*
import glm_.mat3x2.*
import glm_.mat3x3.*
import glm_.mat3x4.*
import glm_.mat4x2.*
import glm_.mat4x3.*
import glm_.mat4x4.*
import glm_.vec2.*
import glm_.vec3.*
import glm_.vec4.*
import org.lwjgl.opengl.GL43C
import org.lwjgl.opengl.GL46C
import org.lwjgl.opengl.GL46C.*
import kotlin.experimental.and

class Shader(private var ID: Int, val vName: String, val fName: String, val gName: String? = null, val uniforms: Map<String, String>) : Deleteable {

    val screenMask: Byte = RandomHelper.createMask(uniforms::containsKey, "view", "projection", "guiProjection", "viewport", "viewPos").b

    val hasView get() = (screenMask and 1) > 0
    val hasProj get() = (screenMask and 2) > 0
    val hasGUI get() = (screenMask and 4) > 0
    val hasPort get() = (screenMask and 8) > 0
    val hasPos get() = (screenMask and 0x10) > 0

    val lightMask: Byte = RandomHelper.createMask(uniforms::containsKey,"dirLight.direction", "pointLight.position", "spotLight.position").b

    val hasDirL get() = (lightMask and 1) > 0
    val hasPointL get() = (lightMask and 2) > 0
    val hasSpotL get() = (lightMask and 4) > 0


    fun use() {
        glUseProgram(ID)
    }

    fun setUniforms(uniforms: Uniforms, renderer: RendererI<*>){
        for(u in uniforms.uniforms){
            u.apply(this, renderer)
        }
    }

    fun setUp(uniforms: Uniforms, renderer: RendererI<*>){
        use()
        setUniforms(uniforms, renderer)
    }

    fun setVP(view: Mat4, projection: Mat4){
        setMat4("view", view)
        setMat4("projection", projection)
    }

    fun setVP(renderer: RendererI<*>){
        setMat4("view", renderer.view)
        setMat4("projection", renderer.projection)
    }

    // Functions to set uniforms within shaders

    fun setBool(name: String, value: Boolean) = set1(name, value.i, GL46C::glUniform1i)
    fun setBools(name: String, values: BooleanArray) = set1(name, values.map { it.i }.toIntArray(), GL46C::glUniform1iv)

    fun setInt(name: String, value: Int) = set1(name, value, GL46C::glUniform1i)
    fun setInts(name: String, values: IntArray) = set1(name, values, GL46C::glUniform1iv)

    fun setUInt(name: String, value: UInt) = set1(name, value.toInt(), GL46C::glUniform1ui)
    fun setUInts(name: String, values: IntArray) = set1(name, values, GL46C::glUniform1uiv)

    fun setFloat(name: String, value: Float) = set1(name, value, GL46C::glUniform1f)
    fun setFloats(name: String, values: FloatArray) = set1(name, values, GL46C::glUniform1fv)

    fun setDouble(name: String, value: Double) = set1(name, value, GL46C::glUniform1d)
    fun setDoubles(name: String, values: DoubleArray) = set1(name, values, GL46C::glUniform1dv)

    fun setVec2i(name: String, v: Vec2t<*>) = set2(name, Vec2i(v), GL46C::glUniform2i)
    fun setVec2i(name: String, x: Number, y: Number) = set2(name, Vec2i(x, y), GL46C::glUniform2i)
    fun setVec2ui(name: String, v: Vec2t<*>) = set2(name, Vec2i(v.x.toInt(), v.y.toInt()), GL46C::glUniform2ui)
    fun setVec2ui(name: String, x: Number, y: Number) = set2(name, Vec2i(x.toInt(), y.toInt()), GL46C::glUniform2ui)

    fun setVec2(name: String, v: Vec2t<*>) = set2(name, Vec2(v), GL46C::glUniform2f)
    fun setVec2(name: String, x: Number, y: Number) = set2(name, Vec2(x, y), GL46C::glUniform2f)
    fun setVec2d(name: String, v: Vec2t<*>) = set2(name, Vec2d(v), GL46C::glUniform2d)
    fun setVec2d(name: String, x: Number, y: Number) = set2(name, Vec2d(x, y), GL46C::glUniform2d)

    fun setVec3i(name: String, v: Vec3t<*>) = set3(name, Vec3i(v), GL46C::glUniform3i)
    fun setVec3i(name: String, r: Number, g: Number, b: Number) = set3(name, Vec3i(r, g, b), GL46C::glUniform3i)
    fun setVec3ui(name: String, v: Vec3t<*>) = set3(name, Vec3i(v), GL46C::glUniform3ui)
    fun setVec3ui(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3i(x, y, z), GL46C::glUniform3ui)

    fun setVec3(name: String, v: Vec3t<*>) = set3(name, Vec3(v), GL46C::glUniform3f)
    fun setVec3(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3(x, y, z), GL46C::glUniform3f)
    fun setVec3d(name: String, v: Vec3t<*>) = set3(name, Vec3d(v), GL46C::glUniform3d)
    fun setVec3d(name: String, x: Number, y: Number, z: Number) = set3(name, Vec3d(x, y, z), GL46C::glUniform3d)

    fun setVec4i(name: String, v: Vec4t<*>) = set4(name, Vec4i(v), GL46C::glUniform4i)
    fun setVec4i(name: String, r: Number, g: Number, b: Number, a: Number) = set4(name, Vec4i(r, g, b, a), GL46C::glUniform4i)
    fun setVec4ui(name: String, v: Vec4t<*>) = set4(name, Vec4i(v), GL46C::glUniform4ui)
    fun setVec4ui(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4i(x, y, z, w), GL46C::glUniform4ui)

    fun setVec4(name: String, v: Vec4t<*>) = set4(name, Vec4(v), GL46C::glUniform4f)
    fun setVec4(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4(x, y, z, w), GL46C::glUniform4f)
    fun setVec4d(name: String, v: Vec4t<*>) = set4(name, Vec4d(v), GL46C::glUniform4d)
    fun setVec4d(name: String, x: Number, y: Number, z: Number, w: Number) = set4(name, Vec4d(x, y, z, w), GL46C::glUniform4d)

    fun <E: Vec2t<*>> setVec2is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2iv)
    fun <E: Vec2t<*>> setVec2uis(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2uiv)
    fun <E: Vec2t<*>> setVec2s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat()) }.toFloatArray(), GL46C::glUniform2fv)
    fun <E: Vec2t<*>> setVec2ds(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble()) }.toDoubleArray(), GL46C::glUniform2dv)

    fun <E: Vec2t<*>> setVec2is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2iv)
    fun <E: Vec2t<*>> setVec2uis(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt()) }.toIntArray(), GL46C::glUniform2uiv)
    fun <E: Vec2t<*>> setVec2s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat()) }.toFloatArray(), GL46C::glUniform2fv)
    fun <E: Vec2t<*>> setVec2ds(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble()) }.toDoubleArray(), GL46C::glUniform2dv)

    fun <E: Vec3t<*>> setVec3is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3iv)
    fun <E: Vec3t<*>> setVec3uis(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3uiv)
    fun <E: Vec3t<*>> setVec3s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }.toFloatArray(), GL46C::glUniform3fv)
    fun <E: Vec3t<*>> setVec3ds(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }.toDoubleArray(), GL46C::glUniform3dv)

    fun <E: Vec3t<*>> setVec3is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3iv)
    fun <E: Vec3t<*>> setVec3uis(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt()) }.toIntArray(), GL46C::glUniform3uiv)
    fun <E: Vec3t<*>> setVec3s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }.toFloatArray(), GL46C::glUniform3fv)
    fun <E: Vec3t<*>> setVec3ds(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }.toDoubleArray(), GL46C::glUniform3dv)

    fun <E: Vec4t<*>> setVec4is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4iv)
    fun <E: Vec4t<*>> setVec4uis(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4uiv)
    fun <E: Vec4t<*>> setVec4s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat(), it.w.toFloat()) }.toFloatArray(), GL46C::glUniform4fv)
    fun <E: Vec4t<*>> setVec4ds(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.w.toDouble()) }.toDoubleArray(), GL46C::glUniform4dv)

    fun <E: Vec4t<*>> setVec4is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4iv)
    fun <E: Vec4t<*>> setVec4uis(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toInt(), it.y.toInt(), it.z.toInt(), it.w.toInt()) }.toIntArray(), GL46C::glUniform4uiv)
    fun <E: Vec4t<*>> setVec4s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toFloat(), it.y.toFloat(), it.z.toFloat(), it.w.toFloat()) }.toFloatArray(), GL46C::glUniform4fv)
    fun <E: Vec4t<*>> setVec4ds(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.w.toDouble()) }.toDoubleArray(), GL46C::glUniform4dv)

    fun setMat2(name: String, value: Mat2) = setMatrix(name, value.array, GL46C::glUniformMatrix2fv)
    fun setMat2d(name: String, value: Mat2d) = setMatrix(name, value.array, GL46C::glUniformMatrix2dv)
    fun setMat2s(name: String, value: Array<Mat2>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix2fv)
    fun setMat2ds(name: String, value: Array<Mat2d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix2dv)
    fun setMat2x3(name: String, value: Mat2x3) = setMatrix(name, value.array, GL46C::glUniformMatrix2x3fv)
    fun setMat2x3d(name: String, value: Mat2x3d) = setMatrix(name, value.array, GL46C::glUniformMatrix2x3dv)
    fun setMat2x3s(name: String, value: Array<Mat2x3>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix2x3fv)
    fun setMat2x3ds(name: String, value: Array<Mat2x3d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix2x3dv)
    fun setMat2x4(name: String, value: Mat2x4) = setMatrix(name, value.array, GL46C::glUniformMatrix2x4fv)
    fun setMat2x4d(name: String, value: Mat2x4d) = setMatrix(name, value.array, GL46C::glUniformMatrix2x4dv)
    fun setMat2x4s(name: String, value: Array<Mat2x4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix2x4fv)
    fun setMat2x4ds(name: String, value: Array<Mat2x4d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix2x4dv)
    fun setMat3x2(name: String, value: Mat3x2) = setMatrix(name, value.array, GL46C::glUniformMatrix3x2fv)
    fun setMat3x2d(name: String, value: Mat3x2d) = setMatrix(name, value.array, GL46C::glUniformMatrix3x2dv)
    fun setMat3x2s(name: String, value: Array<Mat3x2>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix3x2fv)
    fun setMat3x2ds(name: String, value: Array<Mat3x2d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix3x2dv)
    fun setMat3(name: String, value: Mat3) = setMatrix(name, value.array, GL46C::glUniformMatrix3fv)
    fun setMat3d(name: String, value: Mat3d) = setMatrix(name, value.array, GL46C::glUniformMatrix3dv)
    fun setMat3s(name: String, value: Array<Mat3>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix3fv)
    fun setMat3ds(name: String, value: Array<Mat3d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix3dv)
    fun setMat3x4(name: String, value: Mat3x4) = setMatrix(name, value.array, GL46C::glUniformMatrix3x4fv)
    fun setMat3x4d(name: String, value: Mat3x4d) = setMatrix(name, value.array, GL46C::glUniformMatrix3x4dv)
    fun setMat3x4s(name: String, value: Array<Mat3x4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix3x4fv)
    fun setMat3x4ds(name: String, value: Array<Mat3x4d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix3x4dv)
    fun setMat4x2(name: String, value: Mat4x2) = setMatrix(name, value.array, GL46C::glUniformMatrix4x2fv)
    fun setMat4x2d(name: String, value: Mat4x2d) = setMatrix(name, value.array, GL46C::glUniformMatrix4x2dv)
    fun setMat4x2s(name: String, value: Array<Mat4x2>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix4x2fv)
    fun setMat4x2ds(name: String, value: Array<Mat4x2d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix4x2dv)
    fun setMat4x3(name: String, value: Mat4x3) = setMatrix(name, value.array, GL46C::glUniformMatrix4x3fv)
    fun setMat4x3d(name: String, value: Mat4x3d) = setMatrix(name, value.array, GL46C::glUniformMatrix4x3dv)
    fun setMat4x3s(name: String, value: Array<Mat4x3>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix4x3fv)
    fun setMat4x3ds(name: String, value: Array<Mat4x3d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix4x3dv)
    fun setMat4(name: String, value: Mat4) = setMatrix(name, value.array, GL46C::glUniformMatrix4fv)
    fun setMat4d(name: String, value: Mat4d) = setMatrix(name, value.array, GL46C::glUniformMatrix4dv)
    fun setMat4s(name: String, value: Array<Mat4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL46C::glUniformMatrix4fv)
    fun setMat4ds(name: String, value: Array<Mat4d>) = setMatrix(name, value.flatMap { it.array.toList() }.toDoubleArray(), GL46C::glUniformMatrix4dv)


    fun <E> set1(name: String, v: E, func: (Int, E) -> Unit) {
        val varLocation = getVar(name)
        func(varLocation, v)
    }

    fun <E: Number> set2(name: String, v: Vec2t<E>, func: (Int, E, E) -> Unit) {
        val varLocation = getVar(name)
        func(varLocation, v.x, v.y)
    }

    fun <E: Number> set3(name: String, v: Vec3t<E>, func: (Int, E, E, E) -> Unit) {
        val varLocation = getVar(name)
        func(varLocation, v.x, v.y, v.z)
    }

    fun <E: Number> set4(name: String, v: Vec4t<E>, func: (Int, E, E, E, E) -> Unit) {
        val varLocation = getVar(name)
        func(varLocation, v.x, v.y, v.z, v.w)
    }

    fun <E> setMatrix(name: String, v: E, func: (Int, Boolean, E) -> Unit) {
        val varLocation = getVar(name)
        func(varLocation, false, v)
    }

    fun compileUniforms(): Uniforms{
        val set = mutableSetOf<Uniform<*>>()
        for((name, type) in uniforms){
            if(name.contains('[') && name.contains(']')){
                val newName = name.substringBefore('[') + "[0]"
                when(type){
                    "bool" -> set.add(BoolsUniform(newName))
                    "int", "sampler2D" -> set.add(IntsUniform(newName))
                    "uint" -> set.add(UIntsUniform(newName))
                    "float" -> set.add(FloatsUniform(newName))
                    "double" -> set.add(DoublesUniform(newName))

                    "vec2" -> set.add(Vec2sUniform(newName))
                    "vec3" -> set.add(Vec3sUniform(newName))
                    "vec4" -> set.add(Vec4sUniform(newName))
                    "ivec2" -> set.add(Vec2isUniform(newName))
                    "ivec3" -> set.add(Vec3isUniform(newName))
                    "ivec4" -> set.add(Vec4isUniform(newName))
                    "uvec2" -> set.add(Vec2uisUniform(newName))
                    "uvec3" -> set.add(Vec3uisUniform(newName))
                    "uvec4" -> set.add(Vec4uisUniform(newName))
                    "dvec2" -> set.add(Vec2dsUniform(newName))
                    "dvec3" -> set.add(Vec3dsUniform(newName))
                    "dvec4" -> set.add(Vec4dsUniform(newName))

                    "mat2" -> set.add(Mat2sUniform(newName))
                    "mat2x3" -> set.add(Mat2x3sUniform(newName))
                    "mat2x4" -> set.add(Mat2x4sUniform(newName))
                    "mat3x2" -> set.add(Mat3x2sUniform(newName))
                    "mat3" -> set.add(Mat3sUniform(newName))
                    "mat3x4" -> set.add(Mat3x4sUniform(newName))
                    "mat4x2" -> set.add(Mat4x2sUniform(newName))
                    "mat4x3" -> set.add(Mat4x3sUniform(newName))
                    "mat4" -> set.add(Mat4sUniform(newName))

                    "dmat2" -> set.add(Mat2dsUniform(newName))
                    "dmat2x3" -> set.add(Mat2x3dsUniform(newName))
                    "dmat2x4" -> set.add(Mat2x4dsUniform(newName))
                    "dmat3x2" -> set.add(Mat3x2dsUniform(newName))
                    "dmat3" -> set.add(Mat3dsUniform(newName))
                    "dmat3x4" -> set.add(Mat3x4dsUniform(newName))
                    "dmat4x2" -> set.add(Mat4x2dsUniform(newName))
                    "dmat4x3" -> set.add(Mat4x3dsUniform(newName))
                    "dmat4" -> set.add(Mat4dsUniform(newName))
                }
            }
            else {
                when(type){
                    "bool" -> set.add(BoolUniform(name))
                    "int", "sampler2D" -> set.add(IntUniform(name))
                    "uint" -> set.add(UIntUniform(name))
                    "float" -> set.add(FloatUniform(name))
                    "double" -> set.add(DoubleUniform(name))

                    "vec2" -> set.add(Vec2Uniform(name))
                    "vec3" -> set.add(Vec3Uniform(name))
                    "vec4" -> set.add(Vec4Uniform(name))
                    "ivec2" -> set.add(Vec2iUniform(name))
                    "ivec3" -> set.add(Vec3iUniform(name))
                    "ivec4" -> set.add(Vec4iUniform(name))
                    "uvec2" -> set.add(Vec2uiUniform(name))
                    "uvec3" -> set.add(Vec3uiUniform(name))
                    "uvec4" -> set.add(Vec4uiUniform(name))
                    "dvec2" -> set.add(Vec2dUniform(name))
                    "dvec3" -> set.add(Vec3dUniform(name))
                    "dvec4" -> set.add(Vec4dUniform(name))

                    "mat2" -> set.add(Mat2Uniform(name))
                    "mat2x3" -> set.add(Mat2x3Uniform(name))
                    "mat2x4" -> set.add(Mat2x4Uniform(name))
                    "mat3x2" -> set.add(Mat3x2Uniform(name))
                    "mat3" -> set.add(Mat3Uniform(name))
                    "mat3x4" -> set.add(Mat3x4Uniform(name))
                    "mat4x2" -> set.add(Mat4x2Uniform(name))
                    "mat4x3" -> set.add(Mat4x3Uniform(name))
                    "mat4" -> set.add(Mat4Uniform(name))

                    "dmat2" -> set.add(Mat2dUniform(name))
                    "dmat2x3" -> set.add(Mat2x3dUniform(name))
                    "dmat2x4" -> set.add(Mat2x4dUniform(name))
                    "dmat3x2" -> set.add(Mat3x2dUniform(name))
                    "dmat3" -> set.add(Mat3dUniform(name))
                    "dmat3x4" -> set.add(Mat3x4dUniform(name))
                    "dmat4x2" -> set.add(Mat4x2dUniform(name))
                    "dmat4x3" -> set.add(Mat4x3dUniform(name))
                    "dmat4" -> set.add(Mat4dUniform(name))
                }
            }
        }

        return Uniforms(set.toTypedArray())
    }

    private fun getVar(name: String): Int = glGetUniformLocation(ID, name)

    fun getNumberAttributes() = glGetProgrami(ID, GL_ACTIVE_ATTRIBUTES)
    fun getNumberAttributesNew() = glGetProgramInterfacei(ID, GL_PROGRAM_INPUT, GL_ACTIVE_RESOURCES)
    fun getNumberUniforms() = glGetProgrami(ID, GL_ACTIVE_UNIFORMS)
    fun getNumberUniformsNew() = glGetProgramInterfacei(ID, GL_UNIFORM, GL_ACTIVE_RESOURCES)

    fun getAllAttributes(): Array<String> {
        return Array(getNumberAttributes()) { i -> GL43C.glGetProgramResourceName(ID, GL_PROGRAM_INPUT, i) }
    }
    fun getAllUniforms(): Array<String> {
        return Array(getNumberUniforms()) { i -> GL43C.glGetProgramResourceName(ID, GL_UNIFORM, i) }
    }

    override fun delete() {
        glDeleteProgram(ID)
    }

    override fun toString(): String {
        return "Shader[$vName, $fName]"
    }

    companion object{

        val vS: String
        val fS: String
        init {
            val (V, v) = GLFunc.version
            vS =
                "#version $V${v}0 core\n" +
                        "layout (location = 0) in vec2 aPos;\n" +
                        "\n" +
                        "void main(){\n" +
                        "\tgl_Position = vec4(aPos, 0.0, 1.0);\n" +
                        "}"
            fS =
            "#version $V${v}0 core\n" +
                    "\n" +
                    "out vec4 FragColour;\n" +
                    "\n" +
                    "void main(){\n" +
                    "\tFragColour = vec4(1.0, 1.0, 1.0, 1.0);\n" +
                    "}"
        }

        val brokeShader: Shader = ShaderLoader.generateShader(
            "broke", ShaderLoader.generateSubShader("broke", vS, GL_VERTEX_SHADER),
            "broke", ShaderLoader.generateSubShader("broke", fS, GL_FRAGMENT_SHADER)
        )
    }
}