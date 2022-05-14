package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import glm_.f
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import kool.lib.iterator
import org.lwjgl.opengl.GL46C.*
import java.nio.FloatBuffer

class Shader(private var ID: Int, val vName: String, val fName: String, val gName: String? = null) : Deleteable {

    fun use() {
        glUseProgram(ID)
    }

    // Functions to set uniforms within shaders

    fun setBool(name: String, value: Boolean) {
        val varLocation = getVar(name)
        glUniform1i(varLocation, if (value) 1 else 0)
    }

    fun setInt(name: String, value: Int) {
        val varLocation = getVar(name)
        glUniform1i(varLocation, value)
    }

    fun setUInt(name: String, value: Int) {
        val varLocation = getVar(name)
        glUniform1ui(varLocation, value)
    }

    fun setFloat(name: String, value: Float) {
        val varLocation = getVar(name)
        glUniform1f(varLocation, value)
    }

    fun setDouble(name: String, value: Double) {
        val varLocation = getVar(name)
        glUniform1d(varLocation, value)
    }

    fun setFloats(name: String, values: FloatArray) {
        val varLocation = getVar(name)
        glUniform1fv(varLocation, values)
    }

    fun setVec2s(name: String, values: Array<Vec2>) {
        val varLocation = getVar(name)
        val floats = values.flatMap { listOf(it.x, it.y) }.toFloatArray()
        glUniform2fv(varLocation, floats)
    }

    fun setVec2(name: String, v: Vec2) {
        val varLocation = getVar(name)
        glUniform2f(varLocation, v.x, v.y)
    }

    fun setVec2(name: String, x: Number, y: Number) {
        val varLocation = getVar(name)
        glUniform2f(varLocation, x.f, y.f)
    }

    fun setVec3(name: String, v: Vec3) {
        setVec3(name, v.r, v.g, v.b)
    }

    fun setVec3(name: String, r: Number, g: Number, b: Number) {
        val varLocation = getVar(name)
        glUniform3f(varLocation, r.f, g.f, b.f)
    }

    fun setVec4(name: String, v: Vec4) {
        setVec4(name, v.x, v.y, v.z, v.w)
    }

    fun setVec4(name: String, r: Number, g: Number, b: Number, a: Number) {
        val varLocation = getVar(name)
        glUniform4f(varLocation, r.f, g.f, b.f, a.f)
    }

    fun setMat4(name: String, value: Mat4) {
        val varLocation = getVar(name)
        val buffer = FloatBuffer.allocate(16)
        val array = FloatArray(16)
        value to buffer
        for(f in buffer){
            value to array
        }
        glUniformMatrix4fv(varLocation, false, array)
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

        val brokeShader: Shader = ShaderLoader.generateShader("broke", vS, "broke", fS)
    }
}