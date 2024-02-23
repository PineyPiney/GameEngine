package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject

abstract class SelectableScrollListComponent(parent: GameObject): ScrollListComponent(parent){

    abstract val action: (Int, SelectableScrollListEntryComponent?) -> Unit
    override val items: List<GameObject> get() = parent.children.filter { it.hasComponent<SelectableScrollListEntryComponent>() }

    open var selectedEntry: Int = -1

    override val fields: Array<Field<*>> = super.fields +
            IntField("sle", ::selectedEntry){ selectedEntry = it }

    open fun getSelectedEntry(): SelectableScrollListEntryComponent? = items.getOrNull(selectedEntry)?.getComponent()
}