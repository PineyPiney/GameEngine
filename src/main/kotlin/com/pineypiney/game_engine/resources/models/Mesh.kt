package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.game_objects.transforms.Transform3D
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.models.pgm.Controller
import com.pineypiney.game_engine.resources.models.pgm.Face
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.util.Copyable
import com.pineypiney.game_engine.util.extension_functions.copy
import com.pineypiney.game_engine.util.extension_functions.expand
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.f
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL31C.*

// Meshes are made up of faces, which are in turn made up of MeshVertices.
// Mesh vertices are each associated with a position, normal and texMap,
// as well as up to 4 bone weights. The transformation of each vertex is linearly
// interpolated from these 4 bone weights in the shader

class Mesh(var id: String, val vertices: Array<MeshVertex>, val indices: IntArray, val defaultAlpha: Float = 1f,
           val defaultOrder: Int = 0, val material: ModelMaterial = Model.brokeMaterial
): VertexShape() {

    constructor(id: String, faces: Array<Face>, material: ModelMaterial = Model.brokeMaterial):
            this(id, faces.flatMap { it.vertices.toList() }.toTypedArray(), (0 until faces.size * 3).toSet().toIntArray(), material = material)

    override val shape: Shape
        get() = Rect2D(Vec2(), Vec2(1f)) //TODO

    override val size: Int = vertices.size

    private val floatVBO = glGenBuffers()
    private val intVBO = glGenBuffers()
    private val EBO = glGenBuffers()

    var translation: Vec3 = Vec3()
    var rotation: Quat = Quat()
    var alpha = defaultAlpha
    var order = defaultOrder

    val transform: Transform3D get() = Transform3D(translation, rotation, Vec3(1))

    init {
        if(vertices.isNotEmpty()) setupMesh()
    }

    override fun bind() {
        super.bind()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
    }

    override fun draw(mode: Int) {
        glDrawElements(mode, indices.size, GL_UNSIGNED_INT, 0)

    }

    override fun drawInstanced(amount: Int, mode: Int) {
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


        setAttribs(mapOf(0 to Pair(GL_FLOAT, 3), 1 to Pair(GL_FLOAT, 2), 2 to Pair(GL_FLOAT, 3), 4 to Pair(GL_FLOAT, 4)))

        // Get data from each vertex and put it in one long array
        val floatArray = vertices.flatMap(MeshVertex::getFloatData).toFloatArray()
        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, floatArray, GL_STATIC_DRAW)
    }

    fun setupInts(){

        // Buffer ints
        glBindBuffer(GL_ARRAY_BUFFER, intVBO)

        setAttribs(mapOf(3 to Pair(GL_INT, 4)))

        // Get data from each vertex and put it in one long array
        val intArray = vertices.flatMap(MeshVertex::getIntData).toIntArray()
        // Send the data to the buffers
        glBufferData(GL_ARRAY_BUFFER, intArray, GL_STATIC_DRAW)
    }

    fun setupElements(){
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
    }

    fun setMaterial(shader: Shader) {
        material.apply(shader, "material")
    }

    fun setLights(shader: Shader){
        shader.setVec3("light.position", Vec2.fromAngle(Timer.frameTime.f * 2, 10f).run { Vec3(x, 2.0, y) })
        shader.setVec3("light.ambient", Vec3(0.1f))
        shader.setVec3("light.diffuse", Vec3(0.5f))
        shader.setVec3("light.specular", Vec3(1f))
    }

    fun reset(){
        this.alpha = this.defaultAlpha
        this.order = this.defaultOrder
    }

    override fun getVertices(): FloatArray {
        return getFloatBuffer(floatVBO, GL_ARRAY_BUFFER)
    }

    override fun delete() {
        super.delete()
        glDeleteBuffers(intArrayOf(floatVBO, intVBO, EBO))
    }

    companion object{

        private val v1 = MeshVertex(Vec3(0, 0, 0), Vec2(0, 0))
        private val v2 = MeshVertex(Vec3(1, 0, 0), Vec2(1, 0))
        private val v3 = MeshVertex(Vec3(1, 1, 0), Vec2(1, 1))
        private val v4 = MeshVertex(Vec3(0, 1, 0), Vec2(0, 1))

        val default = Mesh("brokeMesh", arrayOf(v1, v2, v3, v4), intArrayOf(0, 3, 2, 2, 1, 0))

        var indicesMult = 1f
    }

    data class MeshVertex(val position: Vec3, val texCoord: Vec2 = Vec2(), val normal: Vec3 = Vec3(0, 0, 1), val weights: Array<Controller.BoneWeight> = arrayOf()): Copyable<MeshVertex>,
        Deleteable {

        fun getFloatData(): List<Float>{
            return position.run{ listOf(x, y, z) } + texCoord.run{ listOf(x, y) } + normal.run { listOf(x, y, z) } + weights.map { w -> w.weight }.expand(4)
        }

        fun getIntData(): List<Int>{
            // When the shader reaches a bone index of -1 it breaks the loop
            return weights.map { w -> w.id }.expand(4, -1)
        }

        override fun copy(): MeshVertex {
            return MeshVertex(position.copy(), texCoord.copy(), normal.copy(), weights.copy())
        }

        override fun delete() {
            weights
        }

        override fun toString(): String {
            return "[$position, $texCoord]"
        }
    }
}