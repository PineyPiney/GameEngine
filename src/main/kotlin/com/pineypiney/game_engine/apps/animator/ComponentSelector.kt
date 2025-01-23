package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListEntryComponent
import com.pineypiney.game_engine.objects.components.scrollList.SelectableScrollListComponent
import com.pineypiney.game_engine.objects.components.scrollList.SelectableScrollListEntryComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ComponentSelector(item: GameObject?, origin: Vec2, size: Vec2, pred: ComponentI.() -> Unit) : MenuItem() {

	override var name: String = "Component Selector"

	init {
		os(origin, size)
		components.add(ComponentSelectorComponent(this, item, pred))
		components.add(
			ColourRendererComponent(
				this,
				Vec4(0.8f, 0.8f, 0.8f, 1f),
				ColourRendererComponent.menuShader,
				Mesh.cornerSquareShape
			)
		)
	}

	class ComponentSelectorComponent(parent: GameObject, item: GameObject?, pred: ComponentI.() -> Unit) :
		SelectableScrollListComponent(parent) {
		override val entryHeight: Float = 1f
		override val scrollerWidth: Float = 0.05f

		var item: GameObject? = item
			set(value) {
				field = value
				items.delete()
				parent.removeChildren(items)
				if (field != null) {

					val newItems = getComponents(field!!, "").mapIndexed { i, c ->
						object : GameObject() {
							override var name: String = "$c Component Entry"

							override fun addComponents() {
								super.addComponents()
								components.add(ComponentSelectorEntry(this, c, i))
								components.add(
									ColourRendererComponent(
										this,
										Vec4(Vec3(if (i % 2 == 0) 0.4f else 0.6f), 1f),
										ScrollListEntryComponent.entryColourShader,
										Mesh.cornerSquareShape
									)
								)
							}

							override fun addChildren() {
								super.addChildren()
								addChild(ScrollListEntryComponent.makeScrollerText(c, Vec4(1f), fontSize = 0f))
							}

							override fun init() {
								super.init()
								getComponent<ShaderRenderedComponent>()?.uniforms?.setVec2Uniform("limits", ::limits)
							}
						}
					}
					parent.addChildren(newItems)
					newItems.init()
					updateEntries()
				}
			}

		override val action: (Int, SelectableScrollListEntryComponent?) -> Unit = { i, e ->
			if (i != -1) {
				val c = (e as? ComponentSelectorEntry)?.c ?: ""
				this.item?.getComponent(c)?.pred()
			}
		}

		override fun createEntries(): List<GameObject> {
			return listOf()
		}
	}

	companion object {

		fun getComponents(o: GameObject?, prefix: String): Set<String> {
			if (o == null) return emptySet()
			val list = o.components.map { prefix + it.id }.toMutableSet()
			for (c in o.children) list.addAll(getComponents(c, "$prefix${c.name}."))
			return list
		}
	}

	class ComponentSelectorEntry(parent: GameObject, val c: String, override val index: Int) :
		SelectableScrollListEntryComponent(parent) {

	}
}