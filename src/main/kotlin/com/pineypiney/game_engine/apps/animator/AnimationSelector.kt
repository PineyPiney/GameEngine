package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListEntryComponent
import com.pineypiney.game_engine.objects.components.scrollList.SelectableScrollListComponent
import com.pineypiney.game_engine.objects.components.scrollList.SelectableScrollListEntryComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.util.extension_functions.init
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class AnimationSelector(item: AnimatedComponent?, origin: Vec2, size: Vec2, pred: () -> Unit) : MenuItem() {

	override var name: String = "Animation Selector"

	init {
		os(origin, size)
		components.add(AnimationSelectorComponent(this, item, pred))
		components.add(
			ColourRendererComponent(
				this,
				Vec4(0.8f, 0.8f, 0.8f, 1f),
				ColourRendererComponent.menuShader,
				Mesh.cornerSquareShape
			)
		)
	}

	class AnimationSelectorComponent(parent: GameObject, animator: AnimatedComponent?, pred: () -> Unit) :
		SelectableScrollListComponent(parent) {
		override val entryHeight: Float = 1f
		override val scrollerWidth: Float = 0.05f

		var item: AnimatedComponent? = animator
			set(value) {
				field = value
				parent.removeAndDeleteChildren(items)
				if (field != null) {
					val newItems = field!!.animations.mapIndexed { i, a ->
						object : GameObject() {
							override var name: String = "${a.name} Animation Entry"

							override fun addComponents() {
								super.addComponents()
								components.add(AnimationSelectorEntry(this, a.name, i))
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
								addChild(ScrollListEntryComponent.makeScrollerText(a.name, Vec4(1f), fontSize = 0f))
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
			(e as? AnimationSelectorEntry)?.let { item?.setAnimation(it.a) }
			pred()
		}

		override fun createEntries(): List<GameObject> {
			return listOf()
		}
	}

	class AnimationSelectorEntry(parent: GameObject, val a: String, override val index: Int) :
		SelectableScrollListEntryComponent(parent) {

	}
}