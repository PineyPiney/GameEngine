package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.resources.shaders.uniforms.*
import glm_.f
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec2.Vec2t
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec3.Vec3t
import glm_.vec4.Vec4
import glm_.vec4.Vec4i
import glm_.vec4.Vec4t
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL40
import org.lwjgl.opengl.GL40.*

class Shader(private var ID: Int, val vName: String, val fName: String, val gName: String? = null, val uniforms: Map<String, String>) : Deleteable {

    fun use() {
        glUseProgram(ID)
    }

    fun setUniforms(uniforms: Uniforms){
        for(u in uniforms.uniforms){
            u.apply(this)
        }
    }

    // Functions to set uniforms within shaders

    fun setBool(name: String, value: Boolean) = set1(name, value.i, GL20::glUniform1i)
    fun setBools(name: String, values: BooleanArray) = set1(name, values.map { it.i }.toIntArray(), GL20::glUniform1iv)

    fun setInt(name: String, value: Int) = set1(name, value, GL20::glUniform1i)
    fun setInts(name: String, values: IntArray) = set1(name, values, GL20::glUniform1iv)

    fun setUInt(name: String, value: UInt) = set1(name, value.toInt(), GL30::glUniform1ui)
    fun setUInts(name: String, values: IntArray) = set1(name, values, GL30::glUniform1uiv)

    fun setFloat(name: String, value: Float) = set1(name, value, GL30::glUniform1f)
    fun setFloats(name: String, values: FloatArray) = set1(name, values, GL30::glUniform1fv)

    fun setDouble(name: String, value: Double) = set1(name, value, GL40::glUniform1d)
    fun setDoubles(name: String, values: DoubleArray) = set1(name, values, GL40::glUniform1dv)

    fun setVec2(name: String, v: Vec2t<*>) = set2(name, Vec2(v), GL20::glUniform2f)
    fun setVec2(name: String, x: Number, y: Number) = set2(name, Vec2(x, y), GL20::glUniform2f)
    fun setVec2i(name: String, v: Vec2t<*>) = set2(name, Vec2i(v), GL20::glUniform2i)
    fun setVec2i(name: String, x: Number, y: Number) = set2(name, Vec2i(x, y), GL20::glUniform2i)

    fun setVec3(name: String, v: Vec3t<*>) = set3(name, Vec3(v), GL20::glUniform3f)
    fun setVec3(name: String, r: Number, g: Number, b: Number) = set3(name, Vec3(r, g, b), GL20::glUniform3f)
    fun setVec3i(name: String, v: Vec3t<*>) = set3(name, Vec3i(v), GL20::glUniform3i)
    fun setVec3i(name: String, r: Number, g: Number, b: Number) = set3(name, Vec3i(r, g, b), GL20::glUniform3i)

    fun setVec4(name: String, v: Vec4t<*>) = set4(name, Vec4(v), GL20::glUniform4f)
    fun setVec4(name: String, r: Number, g: Number, b: Number, a: Number) = set4(name, Vec4(r, g, b, a), GL20::glUniform4f)
    fun setVec4i(name: String, v: Vec4t<*>) = set4(name, Vec4i(v), GL20::glUniform4i)
    fun setVec4i(name: String, r: Number, g: Number, b: Number, a: Number) = set4(name, Vec4i(r, g, b, a), GL20::glUniform4i)

    fun <E: Vec2t<*>> setVec2s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f) }.toFloatArray(), GL20::glUniform2fv)
    fun <E: Vec2t<*>> setVec2is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i) }.toIntArray(), GL20::glUniform2iv)
    fun <E: Vec3t<*>> setVec3s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f) }.toFloatArray(), GL20::glUniform3fv)
    fun <E: Vec3t<*>> setVec3is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i) }.toIntArray(), GL20::glUniform3iv)
    fun <E: Vec4t<*>> setVec4s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f, it.w.f) }.toFloatArray(), GL20::glUniform4fv)
    fun <E: Vec4t<*>> setVec4is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i, it.w.i) }.toIntArray(), GL20::glUniform4iv)

    fun <E: Vec2t<*>> setVec2s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f) }.toFloatArray(), GL20::glUniform2fv)
    fun <E: Vec2t<*>> setVec2is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i) }.toIntArray(), GL20::glUniform2iv)
    fun <E: Vec3t<*>> setVec3s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f) }.toFloatArray(), GL20::glUniform3fv)
    fun <E: Vec3t<*>> setVec3is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i) }.toIntArray(), GL20::glUniform3iv)
    fun <E: Vec4t<*>> setVec4s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f, it.w.f) }.toFloatArray(), GL20::glUniform4fv)
    fun <E: Vec4t<*>> setVec4is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i, it.w.i) }.toIntArray(), GL20::glUniform4iv)

    fun setMat4(name: String, value: Mat4) = setMatrix(name, value.array, GL20::glUniformMatrix4fv)
    fun setMat4s(name: String, value: Array<Mat4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL20::glUniformMatrix4fv)

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
                val newName = name.substringBefore('[')
                when(type){
                    "bool" -> set.add(BoolsUniform(newName))
                    "int" -> set.add(IntsUniform(newName))
                    "uint" -> set.add(UIntsUniform(newName))
                    "float" -> set.add(FloatsUniform(newName))
                    "vec2" -> set.add(Vec2sUniform(newName))
                    "vec3" -> set.add(Vec3sUniform(newName))
                    "vec4" -> set.add(Vec4sUniform(newName))
                    "vec2i" -> set.add(Vec2isUniform(newName))
                    "vec3i" -> set.add(Vec3isUniform(newName))
                    "vec4i" -> set.add(Vec4isUniform(newName))
                    "mat4" -> set.add(Mat4sUniform(newName))
                }
            }
            else {
                when(type){
                    "bool" -> set.add(BoolUniform(name))
                    "int" -> set.add(IntUniform(name))
                    "uint" -> set.add(UIntUniform(name))
                    "float" -> set.add(FloatUniform(name))
                    "vec2" -> set.add(Vec2Uniform(name))
                    "vec3" -> set.add(Vec3Uniform(name))
                    "vec4" -> set.add(Vec4Uniform(name))
                    "vec2i" -> set.add(Vec2iUniform(name))
                    "vec3i" -> set.add(Vec3iUniform(name))
                    "vec4i" -> set.add(Vec4iUniform(name))
                    "mat4" -> set.add(Mat4Uniform(name))
                }
            }
        }

        return Uniforms(set.toTypedArray())
    }

    private fun getVar(name: String) = glGetUniformLocation(ID, name)

    override fun delete() {
        glDeleteProgram(ID)
    }

    override fun toString(): String {
        return "Shader[$vName, $fName]"
    }

    companion object{

        const val vS: String =
                "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "layout (location = 2) in vec2 aTexCoord;\n" +
                "\n" +
                "out vec2 texCoords;\n" +
                "\n" +
                "void main(){\n" +
                "\tgl_Position = vec4(aPos, 1.0);\n" +
                "\ttexCoords = aTexCoord;\n" +
                "}"
        const val fS: String =
                "#version 330 core\n" +
                "\n" +
                "in vec2 texCoords;\n" +
                "\n" +
                "uniform sampler2D ourTexture;\n" +
                "\n" +
                "out vec4 FragColour;\n" +
                "\n" +
                "void main(){\n" +
                "\tvec4 colour = texture(ourTexture, texCoords);\n" +
                "\tif(colour.a == 0) discard;\n" +
                "\tFragColour = colour;\n" +
                "}"

        val brokeShader: Shader = ShaderLoader.generateShader(
            "broke", ShaderLoader.generateSubShader("broke", vS, GL_VERTEX_SHADER),
            "broke", ShaderLoader.generateSubShader("broke", fS, GL_FRAGMENT_SHADER)
        )
    }
}