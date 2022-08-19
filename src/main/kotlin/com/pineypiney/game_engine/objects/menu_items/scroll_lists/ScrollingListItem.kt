package com.pineypiney.game_engine.objects.menu_items.scroll_lists

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.menu_items.StaticInteractableMenuItem
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2

abstract class ScrollingListItem(final override var origin: Vec2, final override val size: Vec2, val entryHeight: Float, val scrollerWidth: Float): StaticInteractableMenuItem() {  //, Iterable<ScrollingListEntry<*>>

    abstract val items: List<ScrollingListEntry<*>>

    // Total height is the total height of the list of entries, given that the screen height is 2
    private var totalHeight = 0f

    // Ratio is the proportion of the list that can be shown on the screen at a time
    private var ratio = size.y / totalHeight
        set(value) {
            // Ratio should not exceed 1, if it is 1 then the scroller is unnecessary
            field = value.coerceIn(0f, 1f)
        }

    // The scroll value is where in the list is being shown, where the bottom of the list is (1 - ratio)
    var scroll: Float = 0f
        set(value) {
            // Ensure that the scroll value does not exceed the maximum,
            // and that the maximum is between 0 and 1
            field = value.coerceIn(0f, (1f - ratio).coerceIn(0f, 1f))
            scrollBar.origin = Vec2(origin.x + (size.x * (1f - scrollerWidth)), origin.y + (size.y * (1 - ratio - scroll)))

            for(i in items.indices){
                items[i].origin = Vec2(origin.x, origin.y + size.y + (scroll * totalHeight) - ((i + 1) * entryHeight))
            }
        }

    protected open val scrollBar = ScrollBarItem(this, Vec2(origin.x + (size.x * (1f - scrollerWidth)), origin.y + (size.y * (1 - ratio - scroll))), Vec2(size.x * scrollerWidth, size.y * ratio))

    override fun init() {
        super.init()
        items.init()
        updateEntries()
    }

    override fun setChildren() {
        addChild(scrollBar, )
        addChildren(items.filterIsInstance<Interactable>())
    }

    override fun draw() {
        if(ratio < 1) scrollBar.draw()
        for(item in items){
            if(item.origin.y > (origin.y + size.y) || (item.origin.y + entryHeight) < origin.y) continue
            item.draw()
        }
    }

    open fun updateEntries(){

        totalHeight =
            if(items.isNotEmpty()) entryHeight * items.size
            else size.y

        ratio = size.y / totalHeight

        scrollBar.size = Vec2(size.x * scrollerWidth, size.y * ratio)

        // Put the scroll bar back to the top in case limits have changed
        scroll = 0f

        for(i in items.indices){
            items[i].origin = Vec2(origin.x, origin.y + size.y - ((i + 1) * entryHeight))
        }
    }

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean {
        if(super.checkHover(ray, screenPos)){
            scrollBar.hover = scrollBar.checkHover(ray, screenPos)
            items.forEachInstance<Interactable> {
                it.hover = it.checkHover(ray, screenPos)
            }
            return true
        }
        return false
    }

    override fun onScroll(game: IGameLogic, scrollDelta: Vec2): Int {
        scroll += (scrollDelta.y * -0.05f)
        return super.onScroll(game, scrollDelta)
    }

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        items.forEachInstance<Interactable> { item ->
            if (item.shouldUpdate()) item.onPrimary(game, action, mods, cursorPos)
        }

        return super.onPrimary(game, action, mods, cursorPos)
    }

    override fun update(interval: Float, time: Double) {
        super.update(interval, time)

        scrollBar.update(interval, time)
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        for(item in items) item.updateAspectRatio(window)
    }

    override fun delete() {
        super.delete()

        scrollBar.delete()
        items.forEach(Deleteable::delete)
    }
}