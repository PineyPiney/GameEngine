package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

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

    override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {}

    override fun onScroll(window: WindowI, scrollDelta: Vec2): Int{
        return 0
    }

    override fun onType(window: WindowI, char: Char): Int{
        return 0
    }

    override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int = 0

    companion object {
        const val INTERRUPT = InteractorComponent.INTERRUPT
    }
}