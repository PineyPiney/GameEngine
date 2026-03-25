package com.pineypiney.game_engine.objects.components.widgets.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec4.Vec4

class ScrollBarComponent(parent: GameObject) : DefaultInteractorComponent(parent) {

	override fun onDrag(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onDrag(window, cursorPos, cursorDelta, ray)
		forceUpdate = true
		parent.parent!!.getComponent<ScrollListComponent>()?.onDragBar(window, cursorDelta.position.y, ray)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		val p = super.onPrimary(window, action, mods, cursorPos)

		if (!pressed) forceUpdate = false

		return p
	}

	companion object {
		fun createBasic(name: String): ScrollBarComponent {
			val obj = GameObject(name, 1)
			val scroller = ScrollBarComponent(obj)
			obj.components.addAll(
				scroller,
				ColourRendererComponent(
					obj,
					Vec4(0x00, 0xBF, 0xFF, 0xFF) / 255,
					ColourRendererComponent.menuShader,
					Mesh.cornerSquareShape
				)
			)
			return scroller
		}
	}
}