package com.pineypiney.game_engine.resources.models

import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.models.animations.State
import com.pineypiney.game_engine.resources.models.materials.PhongMaterial
import com.pineypiney.game_engine.util.maths.Collider
import com.pineypiney.game_engine.util.maths.Collider2D
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import glm_.vec2.Vec2

// Implement the model class. Models are made up of a list of meshes and bones,
// which are separate sections of the model, but bones affect the rendering of meshes

// Materials are also stored in the model, and accessed by the meshes through IDs

class Model(
	val name: String,
	val meshes: Array<ModelMesh> = arrayOf(ModelMesh.default),
	val rootBone: Bone? = null,
	val animations: Array<ModelAnimation> = arrayOf(),
	val box: Collider = Collider2D(Rect2D(Vec2(), Vec2(1f)))
) : Resource() {

	/**
	 * @param name The name of a bone, e.g. head
	 *
	 * @return The first bone found with a name that matches [name], or null
	 */
	fun findBone(name: String) = rootBone?.getChild(name)

	fun animate(states: Array<State>) {
		// Get the states, or forget it
		reset()
		setStates(states)
		meshes.sortBy { it.order }
	}

	private fun setStates(states: Array<State>) {
		for (state in states) {
			state.applyTo(this)
		}
	}

	fun reset() {
		meshes.forEach { it.reset() }
		rootBone?.reset()
	}

	override fun delete() {}

    override fun toString(): String {
        return "Model[$name]"
    }

	companion object {

		const val DEBUG_MESH = 1
		const val DEBUG_WIREFRAME = 2
		const val DEBUG_BONES = 16
		const val DEBUG_COLLIDER = 256

		val brokeMaterial = PhongMaterial("broke", mapOf())

		val brokeModel = Model("broke")
	}
}