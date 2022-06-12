package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.bool
import org.lwjgl.opengl.GL46C.*
import java.io.InputStream

class ShaderLoader private constructor(): AbstractResourceLoader<Shader>(){

    // This map stores the bytebuffer codes of each shader file
    private val shaderMap: MutableMap<ResourceKey, SubShader> = mutableMapOf()

    fun loadShaders(streams: Map<String, InputStream>) {
        streams.forEach { (fileName, stream) ->

            val i = fileName.lastIndexOf(".")
            if (i <= 0) return@forEach
            val suf = fileName.substring(i + 1)

            val type = when(suf){
                "vs" -> GL_VERTEX_SHADER
                "fs" -> GL_FRAGMENT_SHADER
                "gs" -> GL_GEOMETRY_SHADER
                else -> 0
            }

            loadShader(fileName.removePrefix("shaders/").removeSuffix(".$suf"), stream.readBytes(), type)

            stream.close()
        }
    }

    private fun loadShader(name: String, bytes: ByteArray, type: Int){
        val code = bytes.copyOf().toString(Charsets.UTF_8)
        val shader = generateSubShader(name, code, type)
        shaderMap[ResourceKey(name)] = shader
    }

    fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): Shader{
        val vertex: SubShader = shaderMap.getOrElse(vertexKey){
            println("Could not find vertex shader $vertexKey")
            return Shader.brokeShader
        }
        val fragment: SubShader = shaderMap.getOrElse(fragmentKey){
            println("Could not find fragment shader $fragmentKey")
            return Shader.brokeShader
        }
        val geometry: SubShader? = shaderMap[geometryKey]

        return generateShader(vertexKey.key, vertex, fragmentKey.key, fragment, geometryKey?.key, geometry)
    }

    override fun delete() {
        shaderMap.clear()
    }

    companion object{
        val INSTANCE: ShaderLoader = ShaderLoader()

        fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): Shader{
            return INSTANCE.getShader(vertexKey, fragmentKey, geometryKey)
        }

        fun generateSubShader(name: String, code: String, type: Int): SubShader{

            val id = createShaderFromString(code, type, name)

            val uniforms = compileUniforms(code)

            return SubShader(id, uniforms.toMap())

        }

        fun generateShader(vName: String, vertexShader: SubShader, fName: String, fragmentShader: SubShader, gName: String? = null, geometryShader: SubShader? = null): Shader{
            val ID = glCreateProgram()

            // Shader Program
            glAttachShader(ID, vertexShader.id)
            glAttachShader(ID, fragmentShader.id)
            if (geometryShader != null) glAttachShader(ID, geometryShader.id)
            glLinkProgram(ID)

            // print linking errors if any
            checkCompileErrors(ID, GL_SHADER, "$vName x $fName" + if(gName != null) " x $gName" else "")

            // delete the shaders as they're linked into our program now and no longer necessary
            glDeleteShader(vertexShader.id)
            glDeleteShader(fragmentShader.id)
            if (geometryShader != null) glDeleteShader(geometryShader.id)

            val uniforms = vertexShader.uniforms + fragmentShader.uniforms + (geometryShader?.uniforms ?: mapOf())

            return Shader(ID, vName, fName, gName, uniforms)
        }

        fun createShaderFromString(code: String, shaderType: Int, shaderName: String): Int{

            // Create numerical handle for shader
            val shader = glCreateShader(shaderType)

            // vertex Shader
            glShaderSource(shader, code)
            glCompileShader(shader)
            checkCompileErrors(shader, shaderType, shaderName)

            return shader
        }

        fun compileUniforms(code: String): Map<String, String>{
            val uniforms = mutableMapOf<String, String>()
            for(line in code.split('\n')) {
                val parts = line.split(' ')
                if (parts[0] != "uniform") continue
                val name = parts[2].substringBefore(';')
                uniforms[name] = parts[1]
            }

            return uniforms
        }

        fun checkCompileErrors(shader: Int, shaderType: Int, shaderName: String) {
            val success: Boolean
            val infoLog: String
            if (shaderType == GL_SHADER) {
                success = glGetProgrami(shader, GL_LINK_STATUS).bool
                if (!success) {
                    infoLog = glGetProgramInfoLog(shader)
                    println("ERROR::SHADER::PROGRAM ::$shaderName::LINKING_FAILED\n$infoLog")
                }
            }

            else {

                // Type is used later on in error debugging
                val type = when(shaderType){
                    GL_VERTEX_SHADER -> "VERTEX"
                    GL_FRAGMENT_SHADER -> "FRAGMENT"
                    GL_GEOMETRY_SHADER -> "GEOMETRY"
                    else -> return
                }

                success = glGetShaderi(shader, GL_COMPILE_STATUS).bool
                if (!success) {
                    infoLog = glGetShaderInfoLog(shader)
                    println("\nERROR::SHADER::$type ::$shaderName::COMPILATION_FAILED $infoLog")
                }
            }
        }
    }
}