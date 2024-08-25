package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.CheckBoxComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec4.Vec4

abstract class CheckBox(name: String) : MenuItem(name) {

	abstract val action: (Boolean) -> Unit

	override fun addComponents() {
		super.addComponents()
		components.add(CheckBoxComponent(this, action))
		components.add(object :
			ColourRendererComponent(this@CheckBox, Vec4(1f), checkboxShader, Mesh.cornerSquareShape) {
			override fun setUniforms() {
				super.setUniforms()
				uniforms.setBoolUniform("ticked", getComponent<CheckBoxComponent>()!!::ticked)
			}
		})
	}

	companion object {
		val checkboxShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/checkbox")]
	}
}