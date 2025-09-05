package com.pineypiney.game_engine.apps.editor.component_browser

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Components
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.applied
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListEntryComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI

class NewComponentList(parent: GameObject, val browser: ComponentBrowser): ScrollListComponent(parent) {

	override val entryHeight: Float = .125f
	override val scrollerWidth: Float = .05f

	var search = ""
		set(value) {
			field = value
			updateSearch()
		}

	override fun createEntries(): List<GameObject> {
		return Components.getAllComponentNames().filter { it.contains(search) }.map { n ->
			NewComponentEntry(MenuItem("Component Entry $n"), n).applied().parent
		}
	}

	fun updateSearch(){
		val valid = Components.getAllComponentNames().filter { it.lowercase().contains(search) }.toMutableSet()
		val entries = items.toSet()

		for(e in entries){
			val entry = e.getComponent<NewComponentEntry>() ?: continue
			if(valid.contains(entry.compName)) valid.remove(entry.compName)
			else parent.removeAndDeleteChild(e)
		}
		for(c in valid) {
			val newEntry = NewComponentEntry(MenuItem("Component Entry $c"), c).applied().parent
			parent.addChild(newEntry)
			newEntry.init()
		}

		updateEntries()
	}

	class NewComponentEntry(parent: GameObject, val compName: String): ScrollListEntryComponent(parent),
		InteractorComponent {
		override var hover: Boolean = false
		override var pressed: Boolean = false
		override var forceUpdate: Boolean = false
		override var passThrough: Boolean = true

		override fun init() {
			super.init()

			parent.addChild(makeScrollerText(compName, fontSize = (list as? NewComponentList)?.browser?.screen?.settings?.textScale ?: 16))
		}

		override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
			super.onPrimary(window, action, mods, cursorPos)

			if(action == 1) {
				val browser = (list as? NewComponentList)?.browser
				browser?.addComponent(compName)
				return INTERRUPT
			}
			return action
		}

		override fun checkHover(ray: Ray, cursor: CursorPosition): Float {
			return if(list.hover) super.checkHover(ray, cursor) else -1f
		}
	}
}