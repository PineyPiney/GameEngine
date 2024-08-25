package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollBarComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import glm_.vec4.Vec4

open class ScrollBarItem(parentName: String) : MenuItem("$parentName ScrollBar") {

	var colour: Vec4
		get() = getComponent<ColourRendererComponent>()!!.colour
		set(value) {
			getComponent<ColourRendererComponent>()!!.colour = value
		}

	override fun addComponents() {
		super.addComponents()
		components.add(ScrollBarComponent(this))
		components.add(
			ColourRendererComponent(
				this,
				Vec4(0x00, 0xBF, 0xFF, 0xFF) / 255,
				ColourRendererComponent.menuShader,
				Mesh.cornerSquareShape
			)
		)
	}
}