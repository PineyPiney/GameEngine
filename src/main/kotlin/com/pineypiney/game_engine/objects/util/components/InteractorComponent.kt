package com.pineypiney.game_engine.objects.util.components

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.Storable

open class InteractorComponent(parent: Storable, var hover: Boolean, var pressed: Boolean, var forceUpdate: Boolean, var importance: Int): Component("C2D", parent) {

    val interactableChildren: Collection<Interactable> get() = parent.children.filterIsInstance<Interactable>()

    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("hvr", ::hover){ hover = it },
        BooleanField("psd", ::pressed){ pressed = it },
        BooleanField("fud", ::forceUpdate){ forceUpdate = it },
        IntField("ipt", ::importance){ importance = it },
        //CollectionField("cld", ::interactableChildren, { interactableChildren.addAll(it)}, ",", StorableField::serialise, StorableField::parse, Collection<Interactable>::toSet, ::DefaultFieldEditor)
    )
}