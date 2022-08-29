package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import glm_.bool
import org.lwjgl.opengl.GL46C.*
import java.io.InputStream

class ShaderLoader private constructor(): Deleteable{

    // This map stores the bytebuffer codes of each shader file
    private val shaderMap: MutableMap<ResourceKey, SubShader> = mutableMapOf()

    fun loadShaders(streams: Map<String, InputStream>) {
        for((fileName, stream) in streams){

            val i = fileName.lastIndexOf(".")
            if (i <= 0) continue
            val suf = fileName.substring(i + 1)

            val type = when(suf){
                "vs" -> GL_VERTEX_SHADER
                "fs" -> GL_FRAGMENT_SHADER
                "gs" -> GL_GEOMETRY_SHADER
                else -> 0
            }

            loadShader(fileName.removeSuffix(".$suf"), stream.readBytes(), type)

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
            GameEngine.logger.warn("Could not find vertex shader ${vertexKey.key}")
            return Shader.brokeShader
        }
        val fragment: SubShader = shaderMap.getOrElse(fragmentKey){
            GameEngine.logger.warn("Could not find fragment shader ${fragmentKey.key}")
            return Shader.brokeShader
        }
        val geometry: SubShader? = geometryKey?.let{
            shaderMap.getOrElse(it){
                GameEngine.logger.warn("Could not find geometry shader ${it.key}")
                return Shader.brokeShader
            }
        }

        return generateShader(vertexKey.key, vertex, fragmentKey.key, fragment, geometryKey?.key, geometry)
    }

    override fun delete() {
        shaderMap.delete()
        shaderMap.clear()
    }

    companion object{
        val INSTANCE: ShaderLoader = ShaderLoader()

        fun getShader(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null): Shader{
            return INSTANCE.getShader(vertexKey, fragmentKey, geometryKey)
        }

        operator fun get(vertexKey: ResourceKey, fragmentKey: ResourceKey, geometryKey: ResourceKey? = null) : Shader{
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
                    GameEngine.logger.warn("Could not link shaders $shaderName \n$infoLog")
                }
            }

            else {

                // Type is used later on in error debugging
                val type = when(shaderType){
                    GL_VERTEX_SHADER -> "vertex"
                    GL_FRAGMENT_SHADER -> "fragment"
                    GL_GEOMETRY_SHADER -> "geometry"
                    else -> return
                }

                success = glGetShaderi(shader, GL_COMPILE_STATUS).bool
                if (!success) {
                    infoLog = glGetShaderInfoLog(shader)
                    GameEngine.logger.warn("Could not compile $type shader $shaderName \n$infoLog")
                }
            }
        }
    }
}