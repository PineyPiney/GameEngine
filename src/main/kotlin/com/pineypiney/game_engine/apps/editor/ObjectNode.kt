package com.pineypiney.game_engine.apps.editor

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW

class ObjectNode(parent: GameObject, val obj: GameObject): DefaultInteractorComponent(parent, "OBN") {

	var open = true

	val head: Boolean get() = parent.parent?.name == "Object List"
	val browser: ObjectBrowser? get() = parent.getAncestor(-1).getComponent<ObjectBrowser>()

	fun addChild(obj: GameObject){
		this.obj.addChild(obj)
		browser?.addChildObject(this, obj)
	}

	fun position(currentX: Float, currentY: Float): Int{
		parent.position = Vec3(currentX, currentY, 0f)
		var yShift = 1
		val size = if(head) 1.1f else (parent.scale.y * 1.1f)
		if(open){
			for(c in parent.children){
				c.getComponent<ObjectNode>()?.let { yShift += it.position(currentX, -(yShift * size)) }
			}
		}
		return yShift
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)

		if(action == GLFW.GLFW_PRESS){
			val browser = browser ?: return action
			browser.selected = this
			browser.screen.editingObject = obj
			return INTERRUPT
		}

		return action
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		if(action == 1) {
			addChild(GameObject())
			return INTERRUPT
		}
		return super.onSecondary(window, action, mods, cursorPos)
	}
}