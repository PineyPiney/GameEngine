package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.c
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.normal
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Bone(val parent: Bone?, val id: Int, val name: String, val sid: String, val parentTransform: Mat4) {

    var transform = I
    val defaultModelSpace: Mat4 = (parent?.modelSpaceTransform ?: I) * parentTransform
    var modelSpaceTransform: Mat4 = defaultModelSpace

    private val children: MutableList<Bone> = mutableListOf()

    var translation: Vec2 = Vec2()
        set(value) {
            field = value
            updateModel()
        }

    var rotation: Float = 0f
        set(value) {
            field = value
            updateModel()
        }

    fun addChild(newBone: Bone){
        children.add(newBone)
    }

    fun getAllChildren(): List<Bone>{
        val list: MutableList<Bone> = mutableListOf(this)
        children.forEach { list.addAll(it.getAllChildren()) }

        return list
    }

    fun getChild(name: String) = getAllChildren().firstOrNull { it.name == name }

    fun getRoot(): Bone{
        return this.parent ?: this
    }

    fun reset(){
        getAllChildren().forEach {b ->
            if(b.rotation != 0f) b.rotation = 0f
            if(b.translation.let { it.x != 0f || it.y != 0f}) b.translation = Vec2()
        }
    }

    private fun updateModel(){
        transform = I.translate(Vec3(translation)).rotate(rotation, normal)
        modelSpaceTransform = (parent?.modelSpaceTransform ?: I) * (parent?.transform ?: I) * parentTransform
        children.forEach { it.updateModel() }
    }

    fun translate(vector: Vec2){
        translation = translation + vector
    }

    fun rotate(angle: Float){
        rotation += angle
    }

    fun render(shader: Shader, model: Mat4) {

        shader.setMat4("model", model * this.modelSpaceTransform * this.transform * boneMatrix)
        shader.setVec4("colour", Vec4((((this.id + 4) % 6) > 2).i, (((this.id + 2) % 6) > 2).i, (((this.id) % 6) > 2).i, 1))

        Shape.centerSquareShape2D.draw()

        children.forEach { it.render(shader, model) }
    }

    fun getMeshTransform() = modelSpaceTransform * transform * defaultModelSpace.inverse()

    fun copy(copyParent: Bone? = null): Bone{
        val b = Bone(copyParent, id, name, sid, parentTransform.c)
        children.forEach { c -> b.addChild(c.copy(b)) }
        return b
    }

    override fun toString(): String {
        return "Bone $name[id: $id]"
    }

    companion object{
        val boneMatrix = I.translate(Vec3(0, 0.33, 0)).scale(Vec3(0.2, 0.6, 1))
        val boneShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/bones"))
    }
}