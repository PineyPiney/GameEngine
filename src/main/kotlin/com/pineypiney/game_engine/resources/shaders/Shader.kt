package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.uniforms.*
import glm_.b
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
import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL20C.*
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL40C
import kotlin.experimental.and

class Shader(private var ID: Int, val vName: String, val fName: String, val gName: String? = null, val uniforms: Map<String, String>) : Deleteable {

    val vp: Byte = (
            (if(uniforms.containsKey("view")) 1 else 0) or
            (if(uniforms.containsKey("projection")) 2 else 0) or
            (if(uniforms.containsKey("viewport")) 4 else 0) or
            (if(uniforms.containsKey("viewPos")) 8 else 0)
        ).b
    val hasView get() = (vp and 1) > 0
    val hasProj get() = (vp and 2) > 0
    val hasPort get() = (vp and 4) > 0
    val hasPos get() = (vp and 8) > 0

    val lightMask: Byte = (
            (if(uniforms.containsKey("dirLight")) 1 else 0) or
            (if(uniforms.containsKey("pointLight")) 2 else 0) or
            (if(uniforms.containsKey("spotLight")) 4 else 0)
        ).b
    val hasDirL get() = (vp and 1) > 0
    val hasPointL get() = (vp and 2) > 0
    val hasSpotL get() = (vp and 4) > 0


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

    fun setBool(name: String, value: Boolean) = set1(name, value.i, GL20C::glUniform1i)
    fun setBools(name: String, values: BooleanArray) = set1(name, values.map { it.i }.toIntArray(), GL20C::glUniform1iv)

    fun setInt(name: String, value: Int) = set1(name, value, GL20C::glUniform1i)
    fun setInts(name: String, values: IntArray) = set1(name, values, GL20C::glUniform1iv)

    fun setUInt(name: String, value: UInt) = set1(name, value.toInt(), GL30C::glUniform1ui)
    fun setUInts(name: String, values: IntArray) = set1(name, values, GL30C::glUniform1uiv)

    fun setFloat(name: String, value: Float) = set1(name, value, GL30C::glUniform1f)
    fun setFloats(name: String, values: FloatArray) = set1(name, values, GL30C::glUniform1fv)

    fun setDouble(name: String, value: Double) = set1(name, value, GL40C::glUniform1d)
    fun setDoubles(name: String, values: DoubleArray) = set1(name, values, GL40C::glUniform1dv)

    fun setVec2(name: String, v: Vec2t<*>) = set2(name, Vec2(v), GL20C::glUniform2f)
    fun setVec2(name: String, x: Number, y: Number) = set2(name, Vec2(x, y), GL20C::glUniform2f)
    fun setVec2i(name: String, v: Vec2t<*>) = set2(name, Vec2i(v), GL20C::glUniform2i)
    fun setVec2i(name: String, x: Number, y: Number) = set2(name, Vec2i(x, y), GL20C::glUniform2i)

    fun setVec3(name: String, v: Vec3t<*>) = set3(name, Vec3(v), GL20C::glUniform3f)
    fun setVec3(name: String, r: Number, g: Number, b: Number) = set3(name, Vec3(r, g, b), GL20C::glUniform3f)
    fun setVec3i(name: String, v: Vec3t<*>) = set3(name, Vec3i(v), GL20C::glUniform3i)
    fun setVec3i(name: String, r: Number, g: Number, b: Number) = set3(name, Vec3i(r, g, b), GL20C::glUniform3i)

    fun setVec4(name: String, v: Vec4t<*>) = set4(name, Vec4(v), GL20C::glUniform4f)
    fun setVec4(name: String, r: Number, g: Number, b: Number, a: Number) = set4(name, Vec4(r, g, b, a), GL20C::glUniform4f)
    fun setVec4i(name: String, v: Vec4t<*>) = set4(name, Vec4i(v), GL20C::glUniform4i)
    fun setVec4i(name: String, r: Number, g: Number, b: Number, a: Number) = set4(name, Vec4i(r, g, b, a), GL20C::glUniform4i)

    fun <E: Vec2t<*>> setVec2s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f) }.toFloatArray(), GL20C::glUniform2fv)
    fun <E: Vec2t<*>> setVec2is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i) }.toIntArray(), GL20C::glUniform2iv)
    fun <E: Vec3t<*>> setVec3s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f) }.toFloatArray(), GL20C::glUniform3fv)
    fun <E: Vec3t<*>> setVec3is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i) }.toIntArray(), GL20C::glUniform3iv)
    fun <E: Vec4t<*>> setVec4s(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f, it.w.f) }.toFloatArray(), GL20C::glUniform4fv)
    fun <E: Vec4t<*>> setVec4is(name: String, values: Array<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i, it.w.i) }.toIntArray(), GL20C::glUniform4iv)

    fun <E: Vec2t<*>> setVec2s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f) }.toFloatArray(), GL20C::glUniform2fv)
    fun <E: Vec2t<*>> setVec2is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i) }.toIntArray(), GL20C::glUniform2iv)
    fun <E: Vec3t<*>> setVec3s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f) }.toFloatArray(), GL20C::glUniform3fv)
    fun <E: Vec3t<*>> setVec3is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i) }.toIntArray(), GL20C::glUniform3iv)
    fun <E: Vec4t<*>> setVec4s(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.f, it.y.f, it.z.f, it.w.f) }.toFloatArray(), GL20C::glUniform4fv)
    fun <E: Vec4t<*>> setVec4is(name: String, values: List<E>) = set1(name, values.flatMap { listOf(it.x.i, it.y.i, it.z.i, it.w.i) }.toIntArray(), GL20C::glUniform4iv)

    fun setMat4(name: String, value: Mat4) = setMatrix(name, value.array, GL20C::glUniformMatrix4fv)
    fun setMat4s(name: String, value: Array<Mat4>) = setMatrix(name, value.flatMap { it.array.toList() }.toFloatArray(), GL20C::glUniformMatrix4fv)

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
                    "ivec2" -> set.add(Vec2isUniform(newName))
                    "ivec3" -> set.add(Vec3isUniform(newName))
                    "ivec4" -> set.add(Vec4isUniform(newName))
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
                    "ivec2" -> set.add(Vec2iUniform(name))
                    "ivec3" -> set.add(Vec3iUniform(name))
                    "ivec4" -> set.add(Vec4iUniform(name))
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