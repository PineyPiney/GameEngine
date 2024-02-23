package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

open class InteractorComponent(parent: GameObject, id: String): Component(id, parent) {

    var hover: Boolean = false
    var pressed: Boolean = false
    var forceUpdate: Boolean = false
    var importance: Int = 0

    val interactableChildren: Collection<Interactable> get() = parent.children.filterIsInstance<Interactable>()

    val renderSize get() = parent.getComponent<RenderedComponent>()?.renderSize ?: Vec2(1f, 1f)

    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("hvr", ::hover){ hover = it },
        BooleanField("psd", ::pressed){ pressed = it },
        BooleanField("fud", ::forceUpdate){ forceUpdate = it },
        IntField("ipt", ::importance){ importance = it },
        //CollectionField("cld", ::interactableChildren, { interactableChildren.addAll(it)}, ",", StorableField::serialise, StorableField::parse, Collection<Interactable>::toSet, ::DefaultFieldEditor)
    )

    open fun checkHover(ray: Ray, screenPos: Vec2): Boolean{
        return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition), Vec2(parent.transformComponent.worldScale) * renderSize)
    }

    open fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray){
        if(pressed) onDrag(window, cursorPos, cursorDelta)
    }

    open fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2) {}

    open fun onScroll(window: WindowI, scrollDelta: Vec2): Int{
        return 0
    }

    open fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
        return when {
            input.i == 0 && input.controlType == ControlType.MOUSE -> onPrimary(window, action, input.mods, cursorPos)
            input.i == 1 && input.controlType == ControlType.MOUSE -> onSecondary(window, action, input.mods, cursorPos)
            else -> 0
        }
    }

    open fun onType(window: WindowI, char: Char): Int{
        return 0
    }

    // This is the default action when an item is clicked
    open fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Continue pressing if already pressed, or start pressing on first click
        pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
        return action
    }

    open fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int = 0

    open fun shouldUpdate(): Boolean {
        return this.hover || this.pressed || this.forceUpdate || this.interactableChildren.any { child -> child.shouldUpdate() }
    }
}