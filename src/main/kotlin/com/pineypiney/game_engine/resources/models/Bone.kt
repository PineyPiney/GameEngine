package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.rotate
import com.pineypiney.game_engine.util.maths.I
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Bone(val parent: Bone?, val id: Int, val name: String, val sid: String, val parentTransform: Mat4) {

	var transform = I
	val defaultModelSpace: Mat4 = (parent?.modelSpaceTransform ?: I) * parentTransform
	var modelSpaceTransform: Mat4 = defaultModelSpace

	private val children: MutableList<Bone> = mutableListOf()

	var translation: Vec3 = Vec3()
		set(value) {
			field = value
			updateModel()
		}

	var rotation: Vec3 = Vec3()
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
			if (b.rotation != Vec3()) b.rotation = Vec3()
			if (b.translation.let { it.x != 0f || it.y != 0f || it.z != 0f }) b.translation = Vec3()
		}
	}

	private fun updateModel() {
		transform = I.translate(translation).rotate(rotation)
		modelSpaceTransform = (parent?.modelSpaceTransform ?: I) * (parent?.transform ?: I) * parentTransform
		for (it in children) {
			it.updateModel()
		}
	}

	fun translate(vector: Vec3) {
		translation = translation + vector
	}

	fun rotate(angles: Vec3) {
		rotation plusAssign angles
	}

	fun render(shader: Shader, model: Mat4) {

		shader.setMat4("model", model * this.modelSpaceTransform * this.transform * boneMatrix)
		shader.setVec4(
			"colour",
			Vec4((((this.id + 4) % 6) > 2).i, (((this.id + 2) % 6) > 2).i, (((this.id) % 6) > 2).i, 1)
		)

		Mesh.centerSquareShape.draw()

		for (it in children) {
			it.render(shader, model)
		}
	}

	fun getMeshTransform() = modelSpaceTransform * transform * defaultModelSpace.inverse()

	fun copy(copyParent: Bone? = null): Bone {
		val b = Bone(copyParent, id, name, sid, Mat4(parentTransform))
		for (child in children) {
			b.addChild(child.copy(b))
		}
		return b
	}

	override fun toString(): String {
		return "Bone $name[id: $id]"
	}

	companion object {
		val boneMatrix = I.translate(Vec3(0, 0.33, 0)).scale(Vec3(0.2, 0.6, 1))
		val boneShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/bones"))
	}
}