package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.bool
import org.lwjgl.opengl.GL46C.*
import java.io.InputStream

class ShaderLoader private constructor(): AbstractResourceLoader<Shader>(){

    // This map stores the bytebuffer codes of each shader file
    private val shaderMap: MutableMap<ResourceKey, String> = mutableMapOf()

    fun loadShaders(streams: Map<String, InputStream>) {
        streams.forEach { (fileName, stream) ->

            val i = fileName.lastIndexOf(".")
            if (i <= 0) return@forEach
            val type = fileName.substring(i + 1)

            loadShader(fileName.removePrefix("shaders/").removeSuffix(".$type"), stream.readBytes())

            stream.close()
        }
    }

    private fun loadShader(name: String, bytes: ByteArray){
        shaderMap[ResourceKey(name)] = bytes.copyOf().toString(Charsets.UTF_8)
    }

    fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): Shader{
        val vertex: String = shaderMap.getOrElse(vertexKey){
            println("Could not find vertex shader $vertexKey")
            return Shader.emptyShader
        }
        val fragment: String = shaderMap.getOrElse(fragmentKey){
            println("Could not find fragment shader $fragmentKey")
            return Shader.emptyShader
        }
        val geometry: String? = shaderMap[geometryKey]

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

        fun generateShader(vName: String, vertexString: String, fName: String, fragmentString: String, gName: String? = null, geometryString: String? = null): Shader{
            val ID = glCreateProgram()
            val hasGeometry: Boolean = geometryString?.isNotEmpty() ?: false

            val vertexShader =
                if(vertexString.isNotEmpty()) createShaderFromString(vertexString, GL_VERTEX_SHADER, vName)
                else createShaderFromString(Shader.vS, GL_VERTEX_SHADER, vName)

            val fragmentShader =
                if(fragmentString.isNotEmpty()) createShaderFromString(fragmentString, GL_FRAGMENT_SHADER, fName)
                else createShaderFromString(Shader.fS, GL_FRAGMENT_SHADER, fName)

            val geometryShader =
                if (hasGeometry) createShaderFromString(geometryString!!, GL_GEOMETRY_SHADER, gName ?: "")
                else 0


            // Shader Program
            glAttachShader(ID, vertexShader)
            glAttachShader(ID, fragmentShader)
            if (hasGeometry) glAttachShader(ID, geometryShader)
            glLinkProgram(ID)

            // print linking errors if any
            checkCompileErrors(ID, GL_SHADER, "$vName x $fName" + if(gName != null) " x $gName" else "")

            // delete the shaders as they're linked into our program now and no longer necessary
            glDeleteShader(vertexShader)
            glDeleteShader(fragmentShader)
            if (hasGeometry) glDeleteShader(geometryShader)

            return Shader(ID, vName, fName, gName)
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