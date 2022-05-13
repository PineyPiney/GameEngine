package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.extension_functions.expand
import com.pineypiney.game_engine.visual.Deleteable
import glm_.BYTES
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL46C.*

// Meshes are made up of faces, which are in turn made up of MeshVertices.
// Mesh vertices are each associated with a position, normal and texMap,
// as well as up to 4 bone weights. The transformation of each vertex is linearly
// interpolated from these 4 bone weights in the shader

class Mesh(var id: String, var vertices: Array<MeshVertex>,
           val texture: String = "broke", val defaultAlpha: Float = 0f,
           val defaultOrder: Int = 0,
           val material: ModelMaterial = Model.brokeMaterial
): Copyable<Mesh>, Deleteable {

    constructor(id: String, faces: Array<Face>, texture: String = "broke", material: ModelMaterial = Model.brokeMaterial):
            this(id, faces.flatMap { it.vertices.toList() }.toTypedArray(), texture, material = material)

    private val VAO = glGenVertexArrays()

    var translation: Vec2 = Vec2()
    var rotation: Float = 0f
    var alpha = defaultAlpha
    var order = defaultOrder

    init {
        if(vertices.isNotEmpty()) setupMesh()
    }

    fun Draw() {
        setTextures()
        glBindVertexArray(VAO)
        glDrawArrays(GL_TRIANGLES, 0, vertices.size)
        glBindVertexArray(0)
    }

    fun DrawInstanced(shader: Shader, amount: Int, model: Mat4, vp: Mat4) {
        shader.use()

        shader.setMat4("model", model)
        shader.setMat4("vp", vp)

        setTextures()

        glBindVertexArray(VAO)
        glDrawArraysInstanced(GL_TRIANGLES, 0, vertices.size, amount)
        glBindVertexArray(0)
    }

    private fun setupMesh() {

        val VBO = glGenBuffers()
        val intVBO = glGenBuffers()

        // Bind Buffers
        glBindVertexArray(VAO)

        // Enable 5 attributes (position, normal, texCoord, boneIndices, boneWeights)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
        glEnableVertexAttribArray(2)
        glEnableVertexAttribArray(3)
        glEnableVertexAttribArray(4)

        // Buffer floats
        glBindBuffer(GL_ARRAY_BUFFER, VBO)

        // This line turns the faces into an array of float arrays
        val floatArrays = vertices.map { it.position.pos.array
            .plus(it.normal.array)
            .plus(it.texCoord.array)
            .plus(it.weights.map { w -> w.weight }.toFloatArray().expand(4)) }
        // And this line turns the array of float arrays into one large array
        val floatArray = floatArrays.flatMap { it.toList() }.toFloatArray()

        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, floatArray, GL_STATIC_DRAW)

        // Define sizes for the 3 attributes
        var bytes = Float.BYTES
        var stride = floatArrays[0].size * bytes

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * bytes)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * bytes)
        glVertexAttribPointer(4, 4, GL_FLOAT, false, stride, 8L * bytes)


        // Buffer ints
        glBindBuffer(GL_ARRAY_BUFFER, intVBO)

        // This line turns the faces into an array of int arrays
        val intArrays = vertices.map { it.weights.map { w -> w.id }.toIntArray().expand(4)}
        // And this line turns the array of int arrays into one large array
        val intArray = intArrays.flatMap { it.toList() }.toIntArray()

        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, intArray, GL_STATIC_DRAW)

        // Define sizes for the 3 attributes
        bytes = Int.BYTES
        stride = intArrays[0].size * bytes

        glVertexAttribIPointer(3, 4, GL_INT, stride, 0 )

        // Clean up
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glDeleteBuffers(intArrayOf(VBO, intVBO))
    }

    private fun setTextures() {
        TextureLoader.findTexture(texture).bind()
    }

    fun reset(){
        this.alpha = this.defaultAlpha
        this.order = this.defaultOrder
    }

    override fun copy(): Mesh{
        return Mesh(id, vertices.copy(), texture, defaultAlpha, defaultOrder, material)
    }

    override fun delete() {
        TODO("Not yet implemented")
    }

    data class MeshVertex(val position: ModelLoader.VertexPosition, val normal: Vec3 = Vec3(), val texCoord: Vec2 = Vec2(), val weights: Array<Controller.BoneWeight> = arrayOf()): Copyable<MeshVertex>, Deleteable{

        override fun copy(): MeshVertex{
            return MeshVertex(position.copy(), normal.copy(), texCoord.copy(), weights.copy())
        }

        override fun delete() {
            weights
        }

        override fun toString(): String {
            return "[$position, $normal, $texCoord]"
        }
    }
}