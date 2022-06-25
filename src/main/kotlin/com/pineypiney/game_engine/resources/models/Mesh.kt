package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.extension_functions.expand
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
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
): Shape(), Copyable<Mesh> {

    constructor(id: String, faces: Array<Face>, texture: String = "broke", material: ModelMaterial = Model.brokeMaterial):
            this(id, faces.flatMap { it.vertices.toList() }.toTypedArray(), texture, material = material)

    override val size: Int = vertices.size

    var translation: Vec2 = Vec2()
    var rotation: Float = 0f
    var alpha = defaultAlpha
    var order = defaultOrder

    init {
        if(vertices.isNotEmpty()) setupMesh()
    }

    override fun draw(mode: Int) {
        setTextures()
        bind()
        glDrawArrays(mode, 0, size)
    }

    override fun drawInstanced(amount: Int, mode: Int) {
        setTextures()
        bind()
        glDrawArraysInstanced(mode, 0, size, amount)
    }

    private fun setupMesh() {

        val floatVBO = glGenBuffers()
        val intVBO = glGenBuffers()

        // Bind Buffers
        glBindVertexArray(VAO)

        setupFloats(floatVBO)
        setupInts(intVBO)

        // Clean up
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        glDeleteBuffers(intArrayOf(floatVBO, intVBO))
    }

    fun setupFloats(floatVBO: Int){

        // Buffer floats
        glBindBuffer(GL_ARRAY_BUFFER, floatVBO)

        // Get data from each vertex and put it in one long array
        val floatArray = vertices.flatMap(MeshVertex::getFloatData).toFloatArray()
        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, floatArray, GL_STATIC_DRAW)

        setAttribs(arrayOf(Vec2i(0, 3), Vec2i(1, 3), Vec2i(2, 2), Vec2i(4, 4)), GL_FLOAT)
    }

    fun setupInts(intVBO: Int){

        // Buffer ints
        glBindBuffer(GL_ARRAY_BUFFER, intVBO)

        // Get data from each vertex and put it in one long array
        val intArray = vertices.flatMap(MeshVertex::getIntData).toIntArray()
        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, intArray, GL_STATIC_DRAW)

        setAttribs(arrayOf(Vec2i(3, 4)), GL_INT)
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

    companion object{
    }

    data class MeshVertex(val position: ModelLoader.VertexPosition, val normal: Vec3 = Vec3(), val texCoord: Vec2 = Vec2(), val weights: Array<Controller.BoneWeight> = arrayOf()): Copyable<MeshVertex>,
        Deleteable {

        fun getFloatData(): List<Float>{
            return (position.pos.array + normal.array + texCoord.array).toList() + weights.map { w -> w.weight }.expand(4)
        }

        fun getIntData(): List<Int>{
            return weights.map { w -> w.id }.expand(4)
        }

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