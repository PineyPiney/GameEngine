package com.pineypiney.game_engine.objects.components.widgets.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.window.WindowI
import org.lwjgl.glfw.GLFW

abstract class SelectableScrollListEntryComponent(parent: GameObject) : ScrollListEntryComponent(parent), InteractorComponent {

	abstract val index: Int

	override var hover: Boolean = false
	override var pressed: Boolean = false
	override var forceUpdate: Boolean = false
	override var passThrough: Boolean = true

	override val list: SelectableScrollListComponent get() = parent.parent!!.parent!!.getComponent<SelectableScrollListComponent>()!!

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)

		if (action == GLFW.GLFW_RELEASE && list.hover && this.hover) {
			if (this.index != list.selectedEntry) {
				select()
			}
		}

		return action
	}

	open fun select() {
		list.getSelectedEntry()?.forceUpdate = false
		this.forceUpdate = true
		list.selectedEntry = this.index
		list.action(list.selectedEntry, this)
	}

	open fun unselect() {
		this.forceUpdate = false
		list.selectedEntry = -1
		list.action(-1, null)
	}
}