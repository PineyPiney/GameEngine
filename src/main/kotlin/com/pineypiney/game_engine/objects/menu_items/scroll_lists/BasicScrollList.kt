package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListEntryComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class BasicScrollList(origin: Vec2, size: Vec2, entryHeight: Float, scrollerWidth: Float, entries: Array<String>) :
	MenuItem() {

	init {
		os(origin, size)
		components.add(BasicScrollListComponent(this, entryHeight, scrollerWidth, entries))
	}

	class BasicScrollListComponent(
		parent: GameObject,
		override val entryHeight: Float,
		override val scrollerWidth: Float,
		val entries: Array<String>
	) : ScrollListComponent(parent) {

		override fun createEntries(): List<GameObject> {
			return entries.map { e ->
				object : GameObject() {
					override fun addComponents() {
						super.addComponents()
						components.add(BasicListEntry(this))
						components.add(
							ColourRendererComponent(
								this,
								Vec3(0.5f),
								ScrollListEntryComponent.entryColourShader,
								Mesh.cornerSquareShape
							)
						)
					}

					override fun addChildren() {
						super.addChildren()
						addChild(ScrollListEntryComponent.makeScrollerText(e, Vec4(1f), fontSize = 0).apply { position = Vec3(0f, .2f, .01f) })
					}
				}
			}
		}
	}
}