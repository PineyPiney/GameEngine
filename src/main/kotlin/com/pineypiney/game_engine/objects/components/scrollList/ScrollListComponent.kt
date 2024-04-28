package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.DefaultInteractorComponent
import com.pineypiney.game_engine.objects.components.PostChildrenInit
import com.pineypiney.game_engine.objects.components.RenderedComponent
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.ScrollBarItem
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class ScrollListComponent(parent: GameObject): DefaultInteractorComponent(parent, "SCL"), PostChildrenInit{

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

    val scrollBar: GameObject = ScrollBarItem()
    open val items: List<GameObject> get() = parent.children.filter { it.hasComponent<ScrollListEntryComponent>() }

    // The scroll value is where in the list is being shown, where the bottom of the list is (1 - ratio) == proportion of the list that can't be shown
    var scroll: Float = 0f
        set(value) {
            // Ensure that the scroll value does not exceed the maximum,
            // and that the maximum is between 0 and 1
            field = value.coerceIn(0f, (1f - ratio).coerceIn(0f, 1f))
            scrollBar.position = Vec3(1f - scrollerWidth, (1f - ratio) - scroll, 0f)

            for(i in items.indices){
                items[i].position = Vec3(0f, 1f + (scroll * totalHeight) - ((i + 1) * entryHeight), 0f)
                val renderer = items[i].getComponent<RenderedComponent>()
                renderer?.visible == items[i].position.y > 1f || (items[i].position.y + entryHeight) < 0f
            }
        }

    var limits = Vec2(0f); protected set

    override val fields: Array<Field<*>> = super.fields + arrayOf(
        FloatField("eth", ::entryHeight){  },
        FloatField("scw", ::scrollerWidth){  },
        FloatField("tth", ::totalHeight){ totalHeight = it },
        FloatField("rto", ::ratio){ ratio = it },
        FloatField("scr", ::scroll){ scroll = it },
        GameObjectField("scb", ::scrollBar){  }
    )

    override fun init() {
        super.init()

        parent.addChild(scrollBar)
        parent.addChildren(createEntries())

        limits = Vec2(parent.transformComponent.worldPosition.y, parent.transformComponent.worldPosition.y + parent.transformComponent.worldScale.y)
    }

    override fun postChildrenInit() {
        updateEntries()
    }

    abstract fun createEntries(): List<GameObject>

    open fun updateEntries(){

        totalHeight =
            if(items.isNotEmpty()) entryHeight * items.size
            else 1f

        ratio = 1f / totalHeight

        scrollBar.scale = Vec3(scrollerWidth, ratio, 1f)

        // Put the scroll bar back to the top in case limits have changed
        scroll = 0f

        for(i in items.indices){
            items[i].position = Vec3(0f, 1f - ((i + 1) * entryHeight), 0f)
        }
    }

    override fun onScroll(window: WindowI, scrollDelta: Vec2): Int {
        if(hover) scroll += (scrollDelta.y * -0.05f)
        for(i in items) {
            val entry = i.getComponent<ScrollListEntryComponent>() ?: continue
            entry.hover = entry.checkHover(Ray(Vec3(), Vec3()), window.input.mouse.lastPos)
        }
        return super.onScroll(window, scrollDelta)
    }

    fun onDragBar(window: WindowI, cursorDelta: Float, ray: Ray){
        // If the scroller item is taller, then the same scroll value should move the bar by a smaller amount
        // (Remember that scroll is proportional, a value between 0 and (1-ratio))
        scroll -= (cursorDelta / (parent.scale.y))
        for(i in items) {
            val entry = i.getComponent<ScrollListEntryComponent>() ?: continue
            entry.hover = entry.checkHover(ray, window.input.mouse.lastPos)
        }
    }
}