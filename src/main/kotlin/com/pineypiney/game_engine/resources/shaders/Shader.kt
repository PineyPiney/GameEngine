package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.resources.shaders.uniforms.*
import glm_.f
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL46C.*

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

    fun setBool(name: String, value: Boolean) {
        val varLocation = getVar(name)
        glUniform1i(varLocation, value.i)
    }

    fun setBools(name: String, value: BooleanArray) {
        val varLocation = getVar(name)
        glUniform1iv(varLocation, value.map { it.i }.toIntArray())
    }

    fun setInt(name: String, value: Int) {
        val varLocation = getVar(name)
        glUniform1i(varLocation, value)
    }

    fun setInts(name: String, value: IntArray) {
        val varLocation = getVar(name)
        glUniform1iv(varLocation, value)
    }

    fun setUInt(name: String, value: Int) {
        val varLocation = getVar(name)
        glUniform1ui(varLocation, value)
    }

    fun setFloat(name: String, value: Float) {
        val varLocation = getVar(name)
        glUniform1f(varLocation, value)
    }

    fun setFloats(name: String, values: FloatArray) {
        val varLocation = getVar(name)
        glUniform1fv(varLocation, values)
    }

    fun setDouble(name: String, value: Double) {
        val varLocation = getVar(name)
        glUniform1d(varLocation, value)
    }

    fun setVec2(name: String, v: Vec2) {
        val varLocation = getVar(name)
        glUniform2f(varLocation, v.x, v.y)
    }

    fun setVec2(name: String, x: Number, y: Number) {
        val varLocation = getVar(name)
        glUniform2f(varLocation, x.f, y.f)
    }

    fun setVec2s(name: String, values: Array<Vec2>) {
        val varLocation = getVar(name)
        val floats = values.flatMap { listOf(it.x, it.y) }.toFloatArray()
        glUniform2fv(varLocation, floats)
    }

    fun setVec3(name: String, v: Vec3) {
        val varLocation = getVar(name)
        glUniform3f(varLocation, v.r, v.g, v.b)
    }

    fun setVec3(name: String, r: Number, g: Number, b: Number) {
        val varLocation = getVar(name)
        glUniform3f(varLocation, r.f, g.f, b.f)
    }

    fun setVec3s(name: String, values: Array<Vec3>) {
        val varLocation = getVar(name)
        val floats = values.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray()
        glUniform3fv(varLocation, floats)
    }

    fun setVec4(name: String, v: Vec4) {
        val varLocation = getVar(name)
        glUniform4f(varLocation, v.r, v.g, v.b, v.a)
    }

    fun setVec4(name: String, r: Number, g: Number, b: Number, a: Number) {
        val varLocation = getVar(name)
        glUniform4f(varLocation, r.f, g.f, b.f, a.f)
    }

    fun setVec4s(name: String, values: Array<Vec4>) {
        val varLocation = getVar(name)
        val floats = values.flatMap { listOf(it.x, it.y, it.z, it.w) }.toFloatArray()
        glUniform4fv(varLocation, floats)
    }

    fun setMat4(name: String, value: Mat4) {
        val varLocation = getVar(name)
        glUniformMatrix4fv(varLocation, false, value.array)
    }

    fun setMat4s(name: String, value: Array<Mat4>) {
        val varLocation = getVar(name)
        val arrays = value.flatMap { it.array.toList() }
        glUniformMatrix4fv(varLocation, false, arrays.toFloatArray())
    }

    fun compileUniforms(): Uniforms{
        val set = mutableSetOf<Uniform<*>>()
        for((name, type) in uniforms){
            if(name.contains('[') && name.contains(']')){
                val newName = name.substringBefore('[')
                when(type){
                    "bool" -> set.add(BoolsUniform(newName))
                    "int" -> set.add(IntsUniform(newName))
                    "float" -> set.add(FloatsUniform(newName))
                    "vec2" -> set.add(Vec2sUniform(newName))
                    "vec3" -> set.add(Vec3sUniform(newName))
                    "vec4" -> set.add(Vec4sUniform(newName))
                    "mat4" -> set.add(Mat4sUniform(newName))
                }
            }
            else {
                when(type){
                    "bool" -> set.add(BoolUniform(name))
                    "int" -> set.add(IntUniform(name))
                    "float" -> set.add(FloatUniform(name))
                    "vec2" -> set.add(Vec2Uniform(name))
                    "vec3" -> set.add(Vec3Uniform(name))
                    "vec4" -> set.add(Vec4Uniform(name))
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
                "#version 460 core\n" +
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
                "#version 460 core\n" +
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