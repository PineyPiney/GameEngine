package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.apps.editor.object_browser.ObjectNode
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kotlin.math.roundToInt

abstract class MenuNode<T>(parent: GameObject, val obj: T) : DefaultInteractorComponent(parent) {

	var open = true

	fun position(currentY: Int): Int{
		val transform = (parent.transformComponent as? PixelTransformComponent) ?: return 0
		var yShift = (transform.pixelScale.y * -1.1f).roundToInt()

		transform.pixelPos = Vec2i(transform.pixelPos.x, currentY + yShift)
		transform.origin = Vec2(0f, 1f)


		if(open) for(c in parent.children) c.getComponent<ObjectNode>()?.let{ yShift += it.getPixelHeight() }
		return yShift
	}

	fun getPixelHeight(): Int{
		val transform = (parent.transformComponent as? PixelTransformComponent) ?: return 0
		var yShift = (transform.pixelScale.y * -1.1f).roundToInt()
		if(open){
			for(c in parent.children){
				c.getComponent<ObjectNode>()?.let { yShift += it.getPixelHeight() }
			}
		}
		return yShift
	}
}