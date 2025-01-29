package com.pineypiney.game_engine.apps.editor.util.context_menus

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.extension_functions.sumOf
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class ContextMenuComponent<C>(parent: GameObject, val context: C, val menu: ContextMenu<C>): DefaultInteractorComponent(
	parent
) {

	val sections: MutableList<MenuSection> = mutableListOf()
	var hoveredSection = 0
	var hoveredIndex = -1
	var hoveredEntry: ContextMenuEntry<C>? = null
	val menuTree = mutableListOf<Int>()

	override fun init() {
		super.init()

		forceUpdate = true

		val entries = menu.entries.map { Text(it.name, fontSize = .05f).apply { updateLines(1f) } }
		val width = entries.maxOf { it.getWidth() }
		val height = entries.sumOf { it.getHeight() }
		sections.add(MenuSection(Vec2(0f), Vec2(width, height), entries.size))

		val top = parent.position.y + sections.maxOf { it.origin.y + it.size.y }
		if(top > .98f) parent.translate(Vec3(0f, .98f - top))

		val rootChild = MenuItem("Sub Menu Root")
		parent.addChild(rootChild)
		rootChild.addChild(MenuItem("ContextMenuBackground").apply { components.add(ColourRendererComponent(this, Vec3(.6f),
			ColourRendererComponent.menuShader, Mesh.cornerSquareShape)); scale = Vec3(width, height, 1f) })

		entries.forEachIndexed { i, it -> rootChild.addChild(MenuItem("Context Entry ${it.text}").apply { components.add(TextRendererComponent(this, it, Font.fontShader)); position = Vec3(0f, height - (.05f * (i + .5f)), .01f) }) }
	}

	override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(hoveredSection == -1) {
			hoveredIndex = -1
			hoveredEntry = null
			return
		}

		val sec = sections[hoveredSection]
		hoveredIndex = ((sec.size.y + sec.origin.y + parent.position.y - cursorPos.y) * 20f).toInt()
		var menuEntry: ContextMenuEntry<C>
		val childrenToKeep = mutableListOf("Root")
		if(hoveredSection == 0) {
			menuEntry = menu.entries[hoveredIndex]
		}
		else{
			menuEntry = menu.entries[menuTree[0]]
			childrenToKeep.add(menuEntry.name)
			for (i in 1..<hoveredSection - 1){
				menuEntry = menuEntry.children[menuTree[i]]
				childrenToKeep.add(menuEntry.name)
			}
			menuEntry = menuEntry.children[hoveredIndex]
			childrenToKeep.add(menuEntry.name)
		}
		hoveredEntry = menuEntry
		while(menuTree.size > hoveredSection + 1) {
			menuTree.removeLast()
			sections.removeLast()
		}
		names@for(i in parent.children){
			for(k in childrenToKeep){
				if(i.name == "Sub Menu $k"){
					childrenToKeep.remove(k)
					continue@names
				}
			}
			parent.removeAndDeleteChild(i)
		}

		hoveredEntry?.let { newSubmenu ->
			if (newSubmenu.children.isNotEmpty() && parent.children.none { it.name == "Sub Menu ${newSubmenu.name}" }) {
				val origin = sec.origin + Vec2(sec.size.x + .01f, (sec.sections - (hoveredIndex + 1)) * 20f)
				menuTree.add(hoveredIndex)

				val entries = newSubmenu.children.map { Text(it.name, fontSize = .05f).apply { updateLines(1f) } }
				val width = entries.maxOf { it.getWidth() }
				val height = entries.sumOf { it.getHeight() }
				val newSection = MenuSection(origin, Vec2(width, height), entries.size)
				sections.add(newSection)

				val top = parent.position.y + newSection.origin.y + newSection.size.y
				if(top > .98f) newSection.origin.y -= (top - .98f)

				val subchild = MenuItem("Sub Menu ${newSubmenu.name}")
				parent.addChild(subchild)
				subchild.position = Vec3(newSection.origin, 0f)
				subchild.addChild(MenuItem("ContextMenuBackground").apply { components.add(ColourRendererComponent(this, Vec3(.6f),
					ColourRendererComponent.menuShader, Mesh.cornerSquareShape)); scale = Vec3(width, height, 1f) })
				entries.forEachIndexed { i, it -> subchild.addChild(MenuItem("Context Entry ${it.text}").apply { components.add(TextRendererComponent(this, it, Font.fontShader)); position = Vec3(0f, height - (.05f * (i + .5f)), .01f) }) }

				subchild.init()
			}
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 0) {
			if(hover) hoveredEntry?.action(context)
			parent.delete()
		}
		return action
	}

	override fun checkHover(ray: Ray, screenPos: Vec2): Float {
		val relativePos = screenPos - Vec2(parent.transformComponent.worldPosition)
		for((i, sec) in sections.withIndex()) {
			if (relativePos.isWithin(sec.origin, sec.size)) {
				hoveredSection = i
				return ray.rayOrigin.z - parent.transformComponent.worldPosition.z
			}
		}
		hoveredSection = -1
		return -1f
	}

	data class MenuSection(val origin: Vec2, val size: Vec2, val sections: Int)
}