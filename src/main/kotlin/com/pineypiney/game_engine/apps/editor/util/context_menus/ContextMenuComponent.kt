package com.pineypiney.game_engine.apps.editor.util.context_menus

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.extension_functions.sumOf
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import kotlin.math.roundToInt

class ContextMenuComponent<C: ContextMenu.Context>(parent: GameObject, val context: C, val menu: ContextMenu<C>): DefaultInteractorComponent(
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

		createNewSection(menu.entries.map { it.name }, "Sub Menu Root", Vec2(0f))
	}

	override fun onCursorMove(window: WindowI, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray) {
		super.onCursorMove(window, cursorPos, cursorDelta, ray)
		if(hoveredSection == -1) {
			hoveredIndex = -1
			hoveredEntry = null
			return
		}

		val sec = sections[hoveredSection]
		hoveredIndex = ((sec.size.y + sec.origin.y + parent.position.y - cursorPos.position.y) * (context.viewport.y * .4f / context.settings.textScale)).toInt()
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

				val subChild = createNewSection(newSubmenu.children.map { it.name }, "Sub Menu ${newSubmenu.name}", origin)

				subChild.init()
			}
		}
	}

	fun createNewSection(texts: List<String>, name: String, position: Vec2): GameObject{
		val lineHeight = (context.settings.textScale * 1.25f).roundToInt()
		val entries = texts.map { Text(it, alignment = Text.ALIGN_BOTTOM_LEFT).apply { updateLines(Vec2(8f, 2f)) } }
		val pixelWidth = entries.maxOf { it.getWidth() } * context.settings.textScale * 1.02f
		val pixelHeight = entries.sumOf { it.getHeight() } * lineHeight

		val pixelSize = 2f / context.viewport.y
		val newSection = MenuSection(position, Vec2(pixelWidth * pixelSize, pixelHeight * pixelSize), entries.size)
		sections.add(newSection)

		// Make sure the menu doesn't get too close to the top of the screen
		val top = parent.position.y + newSection.origin.y + newSection.size.y
		if(top > .98f) newSection.origin.y -= (top - .98f)

		val rootChild = MenuItem(name)
		parent.addChild(rootChild)
		rootChild.position = Vec3(position, 0f)
		rootChild.addChild(MenuItem("ContextMenuBackground").apply {
			pixel(Vec2i(0, 0), Vec2i(pixelWidth, pixelHeight), Vec2(0f))
			components.add(ColourRendererComponent(this, Vec3(.6f), ColourRendererComponent.menuShader, Mesh.cornerSquareShape)) })

		var lineY = pixelHeight + (lineHeight * .1f)
		entries.forEach { entry ->
			val entryHeight = entry.getHeight() * lineHeight
			lineY -= entryHeight
			rootChild.addChild(MenuItem("Context Entry(\"${entry.text}\")").apply {
				pixel(Vec2i(0, lineY), Vec2i(pixelWidth, entryHeight), Vec2(0f))
				components.add(TextRendererComponent(this, entry, context.settings.textScale, Font.fontShader))
			})
		}

		return rootChild
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: CursorPosition): Int {
		super.onPrimary(window, action, mods, cursorPos)
		if(action == 0) {
			if(hover) hoveredEntry?.action(context)
			parent.delete()
		}
		return action
	}

	override fun checkHover(ray: Ray, cursor: CursorPosition): Float {
		val relativePos = cursor.position - Vec2(parent.transformComponent.worldPosition)
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