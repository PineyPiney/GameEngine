package com.pineypiney.game_engine.objects.components.widgets.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.addAll
import glm_.vec3.Vec3
import glm_.vec4.Vec4

abstract class SelectableScrollListComponent(parent: GameObject) : ScrollListComponent(parent) {

	abstract val action: (Int, SelectableScrollListEntryComponent?) -> Unit

	open var selectedEntry: Int = -1

	open fun getSelectedEntry(): SelectableScrollListEntryComponent? = items.toList().getOrNull(selectedEntry)?.getComponent()

	companion object {

		@Suppress("UNCHECKED_CAST", "KotlinConstantConditions")
		fun createSelectableScrollList(name: String, action: (String) -> Unit, entryHeight: Float = 1f, scrollerWidth: Float = 0.05f, vararg options: String) =
			createSelectableScrollList(name, options as Array<String>, entryHeight, scrollerWidth, action)

		fun createSelectableScrollList(name: String, options: Array<String>, entryHeight: Float = 1f, scrollerWidth: Float = 0.05f, action: (String) -> Unit): GameObject {
			val item = GameObject(name, 1)
			item.components.add(ChildContainingRenderer(item, Mesh.cornerSquareShape))
			item.components.add(object : SelectableScrollListComponent(item){
				override val action: (Int, SelectableScrollListEntryComponent?) -> Unit = { i, _ -> action(options[i]) }
				override val entryHeight: Float = entryHeight
				override val scrollerWidth: Float = scrollerWidth

				override fun createEntries(): List<GameObject> {
					return options.mapIndexed { i, value ->
						val entry = GameObject("$name entry $value", 1)
						entry.components.addAll(
							object : SelectableScrollListEntryComponent(entry){
								override val index: Int = i
							},
							ColourRendererComponent(
								entry,
								Vec4(Vec3(if (i % 2 == 0) 0.4f else 0.6f), 1f),
								ColourRendererComponent.menuShader,
								Mesh.cornerSquareShape
							)

						)
						entry.addChild(Text.makeMenuText(value, Vec4(1f), fontSize = 0).apply { position = Vec3(.05f, .2f, 0f); scale = Vec3(.9f, .8f, 1f) })
						entry
					}
				}

			})
			return item
		}
	}
}