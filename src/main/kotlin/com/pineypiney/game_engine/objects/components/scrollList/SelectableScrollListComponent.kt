package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject

abstract class SelectableScrollListComponent(parent: GameObject) : ScrollListComponent(parent) {

	abstract val action: (Int, SelectableScrollListEntryComponent?) -> Unit

	open var selectedEntry: Int = -1

	open fun getSelectedEntry(): SelectableScrollListEntryComponent? = items.toList().getOrNull(selectedEntry)?.getComponent()
}