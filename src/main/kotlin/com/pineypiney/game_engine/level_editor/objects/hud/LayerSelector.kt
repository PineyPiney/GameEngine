package com.pineypiney.game_engine.level_editor.objects.hud

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.level_editor.LevelMakerScreen
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.ScrollingListItem
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import glm_.vec2.Vec2

class LayerSelector(val game: LevelMakerScreen, override val origin: Vec2, override val size: Vec2, override val entryHeight: Float, override val scrollerWidth: Float): ScrollingListItem(){

    override var items: MutableList<LayerSelectorEntry> = mutableListOf()

    fun addLayer(layer: Int){
        val selector = LayerSelectorEntry(this, layer)
        selector.init()
        items.add(selector)
        addChild(selector)
        items.sortBy { it.index }
        updateEntries()
    }

    fun removeLayer(layer: Int){
        val item = items.firstOrNull { it.index == layer } ?: return

        item.delete()
        children.remove(item)
        items.remove(item)
        updateEntries()
    }

    override fun onPrimary(game: GameLogicI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        super.onPrimary(game, action, mods, cursorPos)
        return if(hover) 1 else 0
    }

    fun toggleLayer(layer: Int, value: Boolean){
        game.gameObjects.gameItems.forEachInstance<RenderedGameObject2D> { if(it.depth == layer) it.visible = value }
    }
}
