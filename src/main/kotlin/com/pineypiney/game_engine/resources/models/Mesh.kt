package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.extension_functions.expand
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL31C.*

// Meshes are made up of faces, which are in turn made up of MeshVertices.
// Mesh vertices are each associated with a position, normal and texMap,
// as well as up to 4 bone weights. The transformation of each vertex is linearly
// interpolated from these 4 bone weights in the shader

class Mesh(var id: String, val vertices: Array<MeshVertex>, val indices: IntArray,
           val texture: Texture = Texture.broke, val defaultAlpha: Float = 1f,
           val defaultOrder: Int = 0, val material: ModelMaterial = Model.brokeMaterial
): Shape() {

    constructor(id: String, vertices: Array<MeshVertex>, indices: IntArray, texture: String, defaultAlpha: Float = 0f, defaultOrder: Int = 0, material: ModelMaterial = Model.brokeMaterial):
            this(id, vertices, indices, TextureLoader.findTexture(texture), defaultAlpha, defaultOrder, material)

    constructor(id: String, faces: Array<Face>, texture: String = "broke", material: ModelMaterial = Model.brokeMaterial):
            this(id, faces.flatMap { it.vertices.toList() }.toTypedArray(), (0 until faces.size * 3).toSet().toIntArray(), texture, material = material)

    override val size: Int = vertices.size

    private val floatVBO = glGenBuffers()
    private val intVBO = glGenBuffers()
    private val EBO = glGenBuffers()

    var translation: Vec2 = Vec2()
    var rotation: Float = 0f
    var alpha = defaultAlpha
    var order = defaultOrder

    init {
        if(vertices.isNotEmpty()) setupMesh()
    }

    override fun bind() {
        super.bind()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
    }

    override fun draw(mode: Int) {
        setTextures()
        glDrawElements(mode, indices.size, GL_UNSIGNED_INT, 0)
    }

    override fun drawInstanced(amount: Int, mode: Int) {
        setTextures()
        glDrawElementsInstanced(mode, indices.size, GL_UNSIGNED_INT, 0, amount)
    }

    private fun setupMesh() {

        // Bind Buffers
        glBindVertexArray(VAO)

        setupFloats()
        setupInts()
        setupElements()

        // Clean up
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun setupFloats(){

        // Buffer floats
        glBindBuffer(GL_ARRAY_BUFFER, floatVBO)

        // Get data from each vertex and put it in one long array
        val floatArray = vertices.flatMap(MeshVertex::getFloatData).toFloatArray()
        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, floatArray, GL_STATIC_DRAW)

        setAttribs(arrayOf(Vec2i(0, 2), Vec2i(1, 2), Vec2i(3, 4)), GL_FLOAT)
    }

    fun setupInts(){

        // Buffer ints
        glBindBuffer(GL_ARRAY_BUFFER, intVBO)

        // Get data from each vertex and put it in one long array
        val intArray = vertices.flatMap(MeshVertex::getIntData).toIntArray()
        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, intArray, GL_STATIC_DRAW)

        setAttribs(arrayOf(Vec2i(2, 4)), GL_INT)
    }

    fun setupElements(){
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
    }

    private fun setTextures() {
        texture.bind()
    }

    fun reset(){
        this.alpha = this.defaultAlpha
        this.order = this.defaultOrder
    }

    override fun delete() {
        super.delete()
        glDeleteBuffers(intArrayOf(floatVBO, intVBO, EBO))
    }

    companion object{
    }

    data class MeshVertex(val position: ModelLoader.VertexPosition, val texCoord: Vec2 = Vec2(), val weights: Array<Controller.BoneWeight> = arrayOf()): Copyable<MeshVertex>,
        Deleteable {

        fun getFloatData(): List<Float>{
            return position.pos.run{ listOf(x, y) } + texCoord.run{ listOf(x, y) } + weights.map { w -> w.weight }.expand(4)
        }

        fun getIntData(): List<Int>{
            // When the shader reaches a bone index of -1 it breaks the loop
            return weights.map { w -> w.id }.expand(4, -1)
        }

        override fun copy(): MeshVertex{
            return MeshVertex(position.copy(), texCoord.copy(), weights.copy())
        }

        override fun delete() {
            weights
        }

        override fun toString(): String {
            return "[$position, $texCoord]"
        }
    }
}