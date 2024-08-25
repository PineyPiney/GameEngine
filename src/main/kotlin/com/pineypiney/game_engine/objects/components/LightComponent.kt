package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.resources.shaders.Shader

class LightComponent(parent: GameObject, val light: Light) : Component(parent, "LGT") {

	var on: Boolean
		get() = light.on
		set(value) {
			light.on = value
		}

	override val fields: Array<Field<*>> = arrayOf()

	fun setShaderUniforms(shader: Shader, name: String) {
		shader.setVec3("$name.position", parent.position)
		light.setShaderUniforms(shader, name)
	}

	fun toggle() {
		on = !on
	}


	/*
	open class LightField<E: Light>(): Field<E>("lgh")

	abstract class LightFieldEditor<L: Light, F: LightField<L>>(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit) : FieldEditor<L, F>(component, id, origin, size){

	}

	class DirectionalLightEditor(component: Component, id: String, origin: Vec2, size: Vec2, callback: (String, String) -> Unit): LightFieldEditor<DirectionalLight, LightField<DirectionalLight>>(component, id, origin, size, callback){

		//val directionField = Vec3FieldEditor(component)

		override fun addChildren() {
			super.addChildren()
		}

		override fun update() {

		}
	}

	 */
}