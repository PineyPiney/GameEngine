package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.resources.shaders.Shader

class LightComponent(parent: GameObject, val light: Light = PointLight()) : Component(parent) {

	var on: Boolean
		get() = light.on
		set(value) { light.on = value }

	fun setShaderUniforms(shader: Shader, name: String) {


		val directionName = name.indexOf('[').let {
			if(it == -1) "${name}Position"
			else name.substring(0, it) + "Positions" + name.substring(it)
		}
		shader.setVec3(directionName, parent.position)

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