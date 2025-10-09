package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.maths.I
import glm_.mat4x4.Mat4
import glm_.quat.Quat
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Bone(val parent: Bone?, val id: Int, val name: String, val sid: String, val defaultRelativeTransform: Mat4) {

    val defaultModelTransform: Mat4 = (parent?.defaultModelTransform ?: I) * defaultRelativeTransform


    val parentTransform get() = (parent?.modelTransform ?: I)
    var relativeTransform = defaultRelativeTransform
	var modelTransform: Mat4 = defaultModelTransform

	private val children: MutableList<Bone> = mutableListOf()

	var translation: Vec3 = defaultRelativeTransform.getTranslation()
		set(value) {
			field = value
			updateModel()
		}

	var rotation: Quat = defaultRelativeTransform.getRotation()
		set(value) {
			field = value
			updateModel()
		}

	fun addChild(newBone: Bone) {
		children.add(newBone)
	}

	fun getAllChildren(): List<Bone> {
		return listOf(this) + children.flatMap { it.getAllChildren() }
	}

	fun getChild(name: String) = getAllChildren().firstOrNull { it.name == name }

	fun getRoot(): Bone {
		return this.parent ?: this
	}

	fun reset() {
		for (b in getAllChildren()) {
			if (b.rotation != Vec3()) b.rotation = Quat()
			if (b.translation.let { it.x != 0f || it.y != 0f || it.z != 0f }) b.translation = Vec3()
		}
	}

	private fun updateModel() {
		relativeTransform = I.translate(translation) * rotation.toMat4()
		modelTransform = parentTransform * relativeTransform
		for (it in children) {
			it.updateModel()
		}
	}

	fun translate(vector: Vec3) {
		translation = translation + vector
	}

	fun rotate(quat: Quat) {
		rotation = rotation * quat
	}

	fun render(shader: Shader, model: Mat4) {

		shader.setMat4("model", model * modelTransform * boneMatrix)
		shader.setVec4(
			"colour",
			calculateColour()
		)

		Mesh.centerSquareShape.draw()

		for (it in children) {
			it.render(shader, model)
		}
	}

	fun getMeshTransform() = modelTransform * defaultModelTransform.inverse()

	fun copy(copyParent: Bone? = null): Bone {
		val b = Bone(copyParent, id, name, sid, Mat4(defaultRelativeTransform))
		for (child in children) {
			b.addChild(child.copy(b))
		}
		return b
	}

    fun calculateColour(): Vec4{
        val v = (id % 2) * .5f
        return Vec4(.5f * ((id % 16) / 8) + v, .5f * ((id % 8) / 4) + v, .5f * ((id % 4) / 2) + v, 1f)
    }

	override fun toString(): String {
		return "Bone $name[id: $id]"
	}

	companion object {
		val boneMatrix = I.translate(Vec3(0, 0.33, 0)).scale(Vec3(0.2, 0.6, 1))
		val boneShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/bones"))

//		fun generateBoneMesh(): IndicesMesh {
//
//		}
	}
}