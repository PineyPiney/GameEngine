package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.CheckBoxComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
import glm_.vec4.Vec4

class CheckBox(name: String, ticked: Boolean = false, action: (Boolean) -> Unit = {}) : MenuItem(name) {

	val boxComp = CheckBoxComponent(this, action).also { it.ticked = ticked }
	val colRend = object :
		ColourRendererComponent(this@CheckBox, Vec4(1f), checkboxShader, Mesh.cornerSquareShape) {
		override fun setUniforms() {
			super.setUniforms()
			uniforms.setBoolUniform("ticked", getComponent<CheckBoxComponent>()!!::ticked)
		}
	}

	override fun addComponents() {
		super.addComponents()
		components.addAll(boxComp, colRend)
	}

	companion object {
		val checkboxShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/checkbox")]
	}
}