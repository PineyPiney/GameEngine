package com.pineypiney.game_engine.objects.util.shapes

import org.lwjgl.opengl.GL31C.*

open class IndicesShape(vertices: FloatArray, parts: IntArray, indices: IntArray): Shape() {

    private val VBO = glGenBuffers()
    private val EBO = glGenBuffers()
    override val size: Int = indices.size

    init{
        glBindVertexArray(VAO)

        glBindBuffer(GL_ARRAY_BUFFER, VBO)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        // How to read non-indices array
        setAttribs(parts)

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    override fun bind() {
        super.bind()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
    }

    override fun draw(mode: Int) {
        glDrawElements(mode, size, GL_UNSIGNED_INT, 0)
    }

    override fun drawInstanced(amount: Int, mode: Int) {
        glDrawElementsInstanced(mode, size, GL_UNSIGNED_INT, 0, amount)
    }

    fun getVertices() = getBuffer(VBO)
    fun getElements() = getBuffer(EBO, GL_ELEMENT_ARRAY_BUFFER)

    override fun delete() {
        super.delete()
        glDeleteBuffers(VBO)
        glDeleteBuffers(EBO)
    }
}
