package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL46C.*

class IndicesMesh(var vertices: Array<MeshVertex> = arrayOf(),
                  var indices: IntArray = intArrayOf(),
                  var textures: Array<Texture> = arrayOf()
) {

    private val VAO = glGenVertexArrays()
    private var EBO = glGenBuffers()

    init {
        setupMesh()
    }

    fun Draw(shader: Shader) {
        shader.use()
        setTextures()

        glBindVertexArray(VAO)
        glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
    }

    fun DrawInstanced(shader: Shader, amount: Int) {
        shader.use()
        setTextures()

        glBindVertexArray(VAO)
        glDrawElementsInstanced(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0, amount)
        glBindVertexArray(0)
    }

    private fun setupMesh() {

        val VBO = glGenBuffers()

        // Bind Buffers
        glBindVertexArray(VAO)
        glBindBuffer(GL_ARRAY_BUFFER, VBO)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)

        // This line turns the vertices into an array of float arrays
        val arrays = vertices.map { (it.position.array.plus(floatArrayOf(0f, 0f, 0f)).plus(it.texCoord.array)) }
        // And this line turns the arrays of float arrays into one large array
        val array = arrays.flatMap { it.toList() }.toFloatArray()

        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, array, GL_STATIC_DRAW)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)


        // Enable 3 attributes (position, normal, texCoord)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
        glEnableVertexAttribArray(2)


        // Define sizes for the 3 attributes
        val bytes = 4 //Float.BYTES
        val stride = vertices.size

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride * bytes, 0)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride * bytes, 3L * bytes)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride * bytes, 6L * bytes)

        // Clean up
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        glDeleteBuffers(VBO)
    }

    private fun setTextures() {
        glActiveTexture(GL_TEXTURE0)
        textures[0].bind()
    }

    class MeshVertex(p: Vec3 = Vec3(), t: Vec2 = Vec2()) {

        var position = Vec3()
        var texCoord = Vec2()

        init{
            position = p
            texCoord = t
        }
    }
}