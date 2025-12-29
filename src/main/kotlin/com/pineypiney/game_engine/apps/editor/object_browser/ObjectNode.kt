package com.pineypiney.game_engine.apps.editor.object_browser

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.Draggable
import com.pineypiney.game_engine.apps.editor.util.DraggableAcceptor
import com.pineypiney.game_engine.apps.editor.util.MenuNode
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenu
import com.pineypiney.game_engine.apps.editor.util.context_menus.ContextMenuEntry
import com.pineypiney.game_engine.apps.editor.util.edits.AddObjectEdit
import com.pineypiney.game_engine.apps.editor.util.edits.ObjectReparentEdit
import com.pineypiney.game_engine.apps.editor.util.edits.RemoveObjectEdit
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xy
import glm_.vec4.Vec4

class ObjectNode(parent: GameObject, obj: GameObject): MenuNode<GameObject>(parent, obj), Draggable, DraggableAcceptor {

	val head: Boolean get() = parent.parent?.name == "Object List"
	val browser: ObjectBrowser? get() = parent.getAncestor(-1).getComponent<ObjectBrowser>()

	var fileSelect = 0.0

	override var canDrop: Boolean = false

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super<DraggableAcceptor>.onPrimary(window, action, mods, cursorPos)

		when(action){
			1 -> {
				// Set this node to be dragged
				fileSelect = Timer.time
				browser?.screen?.setDragging(this, cursorPos)
				return INTERRUPT
			}
			0 -> {
				browser?.screen?.clearDragging(cursorPos.position)
				// If the mouse button was released within half a second of clicking it
				// then select this node's object
				if(Timer.time - fileSelect < .5) {
					browser?.let { browser ->
						browser.selected = this
						browser.screen.editingObject = obj
					}
				}
				return INTERRUPT
			}
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
		return super<MenuNode>.onSecondary(window, action, mods, cursorPos)
	}

	override fun checkHover(ray: Ray, cursor: CursorPosition): Float {
		return if(browser?.checkHover(ray, cursor) != -1f) super<MenuNode>.checkHover(ray, cursor) else -1f
	}

	override fun getElement(): Any = this

	override fun addRenderer(parent: GameObject, cursor: CursorPosition) {
		val menuRenderer = GameObject("Menu Renderer", 1)
		menuRenderer.scale = this.parent.transformComponent.worldScale
		menuRenderer.position = Vec3(this.parent.transformComponent.worldPosition.xy - cursor.position, 0f)
		menuRenderer.components.add(ColourRendererComponent(menuRenderer, Vec3(.6f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape))

		val textChild = Text.makeMenuText(obj.name, Vec4(0f, 0f, 0f, 1f), 16)
		textChild.position = Vec3(.02f, 0f, .01f)
		textChild.scale = Vec3(.96f, 1f, 1f)
		menuRenderer.addChild(textChild)
		parent.addChild(menuRenderer)
	}

	override fun onHoverElement(element: Any, cursorPos: Vec2): Boolean {
		// Ensure no ancestry loops
		return element is ObjectNode && parent != element.parent && !parent.getAncestry(-1).contains(element.parent)
	}

	override fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen) {
		if(element is ObjectNode && parent != element.parent && !parent.getAncestry(-1).contains(element.parent)){
			screen.editManager.addEdit(ObjectReparentEdit(screen, element.obj, element.obj.parent, this.obj))
			screen.objectBrowser.reparentNode(element, this)
		}
	}

	companion object {
		data class ObjectNodeContext(val browser: ObjectBrowser, val node: ObjectNode): ContextMenu.Context(browser.screen.settings, browser.screen.renderer.viewportSize)

		val objectNodeContextMenu = ContextMenu<ObjectNodeContext>(arrayOf(
			ContextMenuEntry("Add Child") {
				val obj = GameObject()
				browser.addChildObject(node, obj)
				browser.screen.editManager.addEdit(AddObjectEdit(obj, node.obj, browser.screen))
			},
			ContextMenuEntry("Delete Object"){
				browser.screen.editManager.addEdit(RemoveObjectEdit(node.obj, node.obj.parent, browser.screen))
				browser.removeObject(node)
			},
			ContextMenuEntry("Colour", arrayOf(
				ContextMenuEntry("Blue"){ println("Selected Blue") },
				ContextMenuEntry("Red"){ println("Selected Red") }
			))
		))
	}
}