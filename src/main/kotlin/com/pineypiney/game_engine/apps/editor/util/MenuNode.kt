package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.apps.editor.object_browser.ObjectNode
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import glm_.vec3.Vec3

abstract class MenuNode<T>(parent: GameObject, val obj: T) : DefaultInteractorComponent(parent) {

	var open = true

	fun position(currentX: Float, currentY: Float): Float{
		var yShift = parent.transformComponent.worldScale.y * 1.1f
		parent.transformComponent.worldPosition = Vec3(currentX, currentY - yShift, .01f)
		if(open){
			for(c in parent.children){
				c.getComponent<ObjectNode>()?.let { yShift += it.position(currentX + (parent.transformComponent.worldScale.x * .02f), currentY - yShift) }
			}
		}
		return yShift
	}
}