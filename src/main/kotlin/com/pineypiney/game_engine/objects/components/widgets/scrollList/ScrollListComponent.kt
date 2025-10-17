package com.pineypiney.game_engine.objects.components.widgets.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.PostChildrenInit
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.ScrollBarItem
import com.pineypiney.game_engine.util.extension_functions.getScale
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class ScrollListComponent(parent: GameObject) : DefaultInteractorComponent(parent), PostChildrenInit {

	abstract val entryHeight: Float
	abstract val scrollerWidth: Float

	// Total height is the total height of the list of entries, given that the screen height is 2
	private var totalHeight = 0f

	// Ratio is the proportion of the list that can be shown on the screen at a time
	private var ratio = 1f
		set(value) {
			// Ratio should not exceed 1, if it is 1 then the scroller is unnecessary
			field = value.coerceIn(0f, 1f)
			scrollBar.getComponent<RenderedComponent>()?.visible = field < 1f
		}

	val scrollBar: GameObject = ScrollBarItem(parent.name)
	val entryContainer = MenuItem("${parent.name} Entries Container")

	open val items: Set<GameObject> get() = entryContainer.children

	// The scroll value is where in the list is being shown, where the bottom of the list is (1 - ratio) == proportion of the list that can't be shown
	var scroll: Float = 0f
		set(value) {
			// Ensure that the scroll value does not exceed the maximum,
			// and that the maximum is between 0 and 1
			field = value.coerceIn(0f, (1f - ratio).coerceIn(0f, 1f))
			scrollBar.position = Vec3(1f - scrollerWidth, (1f - ratio) - scroll, 0f)

			var i = 0
			for (item in items) {
				item.position = Vec3(0f, 1f + (scroll * totalHeight) - ((i + 1) * entryHeight), 0f)
				val renderer = item.getComponent<RenderedComponent>()
				renderer?.visible == item.position.y > 1f || (item.position.y + entryHeight) < 0f
				i++
			}
		}

	override fun init() {
		super.init()
		entryContainer.scale = Vec3(1f - scrollerWidth, 1f, 1f)

		parent.addChild(scrollBar, entryContainer)
		entryContainer.addChildren(createEntries())
	}

	override fun postChildrenInit() {
		updateEntries()
	}

	abstract fun createEntries(): List<GameObject>

	open fun updateEntries() {

		totalHeight =
			if (items.isNotEmpty()) entryHeight * items.size
			else 1f

		ratio = 1f / totalHeight

		scrollBar.scale = Vec3(scrollerWidth, ratio, 1f)

		// Put the scroll bar back to the top in case limits have changed
		scroll = 0f

		var i = 0
		for (entry in items) {
			entry.position = Vec3(0f, 1f - ((i + 1) * entryHeight), .01f)
			i++
		}
	}

	fun clearEntries(){
		entryContainer.deleteAllChildren()
	}

	override fun onScroll(window: WindowI, scrollDelta: Vec2): Int {
		if (hover) scroll += (scrollDelta.y * -0.05f)
		for (i in items) {
			val entry = i.getComponent<InteractorComponent>() ?: continue
			entry.hover = entry.checkHover(Ray(Vec3(0f, 0f, 1f), Vec3()), window.input.mouse.lastPos) >= 0f
		}
		return if(hover) -1 else 0
	}

	fun onDragBar(window: WindowI, cursorDelta: Float, ray: Ray) {
		// If the scroller item is taller, then the same scroll value should move the bar by a smaller amount
		// (Remember that scroll is proportional, a value between 0 and (1-ratio))
		scroll -= (cursorDelta / (parent.transformComponent.worldScale.y))
		for (i in items) {
			val entry = i.getComponent<InteractorComponent>() ?: continue
			entry.hover = entry.checkHover(ray, window.input.mouse.lastPos) >= 0f
		}
	}

	fun getLimits(): Vec2{
		val model = parent.worldModel
		val posY = model.getTranslation(1)
		return Vec2(posY, posY + model.getScale(1))
	}
}