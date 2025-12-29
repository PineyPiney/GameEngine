package com.pineypiney.game_engine.apps.editor.util

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

interface DraggableAcceptor : InteractorComponent {

	var canDrop: Boolean
	fun getDragging() = parent.getObjectCollection()?.findTop("Dragged Element", 1)?.getComponent<DraggedElement>()

	fun onHoverElement(element: Any, cursorPos: Vec2): Boolean = false
	fun onDropElement(element: Any, cursorPos: Vec2, screen: EditorScreen){}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		val dragging = getDragging() ?: return
		canDrop = onHoverElement(dragging.element.getElement(), cursorPos.position)
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
//		val dragging = getDragging() ?: return action
//		if(action == 0 && canDrop) {
////			onDropElement(dragging.element.getElement(), cursorPos.position, dragging.screen)
//		}
		return action
	}
}