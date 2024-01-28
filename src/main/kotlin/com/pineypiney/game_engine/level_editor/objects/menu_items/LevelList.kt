package com.pineypiney.game_engine.level_editor.objects.menu_items

import com.pineypiney.game_engine.level_editor.resources.levels.LevelLoader
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListEntry
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListItem
import glm_.vec2.Vec2

class LevelList(override val origin: Vec2, override val size: Vec2, override val entryHeight: Float, override val scrollerWidth: Float, override val action: (Int, SelectableScrollingListEntry<*>?) -> Unit): SelectableScrollingListItem(){

    override val items: MutableList<LevelListEntry> = createKeys()

    private fun createKeys(): MutableList<LevelListEntry> {
        val levels = LevelLoader.getAllLevels().sortedBy { level -> level.edited }.reversed()
        val entries = levels.indices.map { i ->
            LevelListEntry(this, i, levels[i])
        }
        return entries.toMutableList()
    }

    fun addEntry(entry: LevelListEntry){
        items.add(entry)
        addChild(entry)
        updateEntries()
    }
    fun removeEntry(entry: LevelListEntry){
        items.remove(entry)
        removeChild(entry)
        updateEntries()
    }
    fun removeEntryIf(predicate: (LevelListEntry) -> Boolean){
        val removed = items.filter(predicate).toSet()
        items.removeAll(removed)
        removeChildren(removed)
        updateEntries()
    }
}