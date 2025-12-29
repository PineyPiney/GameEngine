package com.pineypiney.game_engine.objects.components.widgets

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.window.WindowI
import glm_.vec4.Vec4

class CheckBoxComponent(parent: GameObject, val action: (Boolean) -> Unit) : DefaultInteractorComponent(parent) {

	var ticked = false

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		if (action == 1) toggle()
		return super.onPrimary(window, action, mods, cursorPos)
	}

	fun toggle() {
		ticked = !ticked
		action(ticked)
	}

	companion object {

		val checkboxShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/checkbox")]

		fun createCheckBox(name: String, ticked: Boolean = false, action: (Boolean) -> Unit = {}): Pair<GameObject, CheckBoxComponent>{
			val obj = GameObject(name, 1)
			val comp = CheckBoxComponent(obj, action).also { it.ticked = ticked }
			obj.components.addAll(
				comp,
				object : ColourRendererComponent(obj, Vec4(1f), checkboxShader, Mesh.cornerSquareShape) {
					override fun setUniforms() {
						super.setUniforms()
						uniforms.setBoolUniform("ticked", parent.getComponent<CheckBoxComponent>()!!::ticked)
					}
				}
			)
			return obj to comp
		}
	}
}