package com.pineypiney.game_engine.apps.editor.object_browser

import com.pineypiney.game_engine.apps.editor.util.MenuNode
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenuEntry
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import org.lwjgl.glfw.GLFW

class ObjectNode(parent: GameObject, obj: GameObject): MenuNode<GameObject>(parent, obj) {

	val head: Boolean get() = parent.parent?.name == "Object List"
	val browser: ObjectBrowser? get() = parent.getAncestor(-1).getComponent<ObjectBrowser>()

	fun addChild(obj: GameObject){
		this.obj.addChild(obj)
		browser?.addChildObject(this, obj)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)

		if(action == GLFW.GLFW_PRESS){
			val browser = browser ?: return action
			browser.selected = this
			browser.screen.editingObject = obj
			return INTERRUPT
		}

		return action
	}

	override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		if(action == 1) {
			browser?.let { browser ->
				browser.screen.setContextMenu(ObjectNodeContext(browser, this), objectNodeContextMenu, cursorPos.position)
				return INTERRUPT
			}
		}
		return super.onSecondary(window, action, mods, cursorPos)
	}

	override fun checkHover(ray: Ray, screenPos: CursorPosition): Float {
		return if(browser?.checkHover(ray, screenPos) != -1f) super.checkHover(ray, screenPos) else -1f
	}

	companion object {
		data class ObjectNodeContext(val browser: ObjectBrowser, val node: ObjectNode): ContextMenu.Context(browser.screen.settings, browser.screen.renderer.viewportSize)

		val objectNodeContextMenu = ContextMenu<ObjectNodeContext>(arrayOf(
			ContextMenuEntry("Add Child") {
				node.addChild(GameObject())
				browser.positionNodes()
			},
			ContextMenuEntry("Delete Object"){
				if(browser.screen.editingObject == node.obj || browser.screen.editingObject?.getAncestry()?.contains(node.obj) == true) browser.screen.editingObject = null
				node.obj.delete()
				node.parent.parent?.removeAndDeleteChild(node.parent)
				browser.positionNodes()
			},
			ContextMenuEntry("Colour", arrayOf(
				ContextMenuEntry("Blue"){ println("Selected Blue") },
				ContextMenuEntry("Red"){ println("Selected Red") }
			))
		))
	}
}