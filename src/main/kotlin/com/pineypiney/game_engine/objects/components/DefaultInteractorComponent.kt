package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject

open class DefaultInteractorComponent(parent: GameObject, id: String): Component(parent, id), InteractorComponent {

    override var hover: Boolean = false
    override var pressed: Boolean = false
    override var forceUpdate: Boolean = false
    override var importance: Int = 0


    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("hvr", ::hover){ hover = it },
        BooleanField("psd", ::pressed){ pressed = it },
        BooleanField("fud", ::forceUpdate){ forceUpdate = it },
        IntField("ipt", ::importance){ importance = it },
        //CollectionField("cld", ::interactableChildren, { interactableChildren.addAll(it)}, ",", StorableField::serialise, StorableField::parse, Collection<Interactable>::toSet, ::DefaultFieldEditor)
    )

    companion object {
        const val INTERRUPT = InteractorComponent.INTERRUPT
    }
}