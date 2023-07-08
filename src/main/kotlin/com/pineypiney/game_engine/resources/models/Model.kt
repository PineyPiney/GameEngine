package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.game_objects.GameObject
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2D
import com.pineypiney.game_engine.objects.util.collision.CollisionBoxRenderer
import com.pineypiney.game_engine.objects.util.collision.SoftCollisionBox
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.models.animations.State
import com.pineypiney.game_engine.resources.shaders.Shader
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.assimp.Assimp.aiProcess_FlipUVs
import org.lwjgl.assimp.Assimp.aiProcess_Triangulate

// Implement the model class. Models are made up of a list of meshes and bones,
// which are separate sections of the model, but bones affect the rendering of meshes

// Materials are also stored in the model, and accessed by the meshes through IDs

class Model(val meshes: Array<Mesh>, val rootBone: Bone?, val animations: Array<ModelAnimation>, val name: String = "broke", val flags: Int = aiProcess_Triangulate or aiProcess_FlipUVs): Resource() {

    var collisionBox: CollisionBox2D = SoftCollisionBox(null, Vec2(), Vec2(1))

    fun Draw(parent: GameObject, view: Mat4, projection: Mat4, tickDelta: Double, shader: Shader, debug: Int = 0) {

        for(mesh in meshes) {
            shader.setFloat("alpha", mesh.alpha)
            mesh.bindAndDraw()
        }

        if(debug and DEBUG_BONES > 0) renderBones(parent, view, projection)
        if(debug and DEBUG_COLLIDER > 0) renderCollider(parent, view, projection, tickDelta)
    }

    fun DrawInstanced(amount: Int, parent: GameObject, view: Mat4, projection: Mat4, tickDelta: Double, shader: Shader, debug: Int) {

        for(mesh in meshes) {
            shader.setFloat("alpha", mesh.alpha)
            mesh.drawInstanced(amount)
        }

        if(debug and DEBUG_BONES > 0) renderBones(parent, view, projection)
        if(debug and DEBUG_COLLIDER > 0) renderCollider(parent, view, projection, tickDelta)
    }

    fun renderBones(parent: GameObject, view: Mat4, projection: Mat4){
        // Render Bones
        VertexShape.centerSquareShape2D.bind()
        val boneShader = Bone.boneShader

        boneShader.use()
        boneShader.setMat4("view", view)
        boneShader.setMat4("projection", projection)

        val bones: List<Bone> = rootBone?.getAllChildren() ?: listOf()
        for(it in bones) { it.render(boneShader, parent.transform.model) }
    }

    fun renderCollider(parent: GameObject, view: Mat4, projection: Mat4, tickDelta: Double){
        val renderer = CollisionBoxRenderer(collisionBox, parent, CollisionBoxRenderer.defaultShader)
        renderer.setUniforms()
        renderer.render(view, projection, tickDelta)
    }

    /**
     * @param name The name of a bone, e.g. head
     *
     * @return The first bone found with a name that matches [name], or null
     */
    fun findBone(name: String) = rootBone?.getChild(name)

    fun animate(states: Array<State>){
        // Get the states, or forget it
        reset()
        setStates(states)
        meshes.sortBy { it.order }
    }

    private fun setStates(states: Array<State>){
        for(state in states){
            state.applyTo(this)
        }
    }

    fun reset(){
        meshes.forEach { it.reset() }
        rootBone?.reset()
    }

    override fun delete() {}

    companion object{

        const val DEBUG_MESH = 1
        const val DEBUG_BONES = 2
        const val DEBUG_COLLIDER = 4

        val brokeMaterial = ModelMaterial("broke", mapOf(), Vec3(1))

        private val v1 = Mesh.MeshVertex(ModelLoader.VertexPosition(0, Vec2(0, 0)), Vec2(0, 0))
        private val v2 = Mesh.MeshVertex(ModelLoader.VertexPosition(1, Vec2(1, 0)), Vec2(1, 0))
        private val v3 = Mesh.MeshVertex(ModelLoader.VertexPosition(2, Vec2(1, 1)), Vec2(1, 1))
        private val v4 = Mesh.MeshVertex(ModelLoader.VertexPosition(3, Vec2(0, 1)), Vec2(0, 1))

        val brokeModel = Model(arrayOf(Mesh("brokeMesh", arrayOf(v1, v2, v3, v4), intArrayOf(0, 3, 2, 2, 1, 0))), null, arrayOf())
    }
}