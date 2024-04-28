package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.util.extension_functions.isWithin
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

open class DefaultInteractorComponent(parent: GameObject, id: String): Component(parent, id), InteractorComponent {

    override var hover: Boolean = false
    override var pressed: Boolean = false
    override var forceUpdate: Boolean = false
    override var importance: Int = 0

    override val interactableChildren: Collection<InteractorComponent> get() = parent.children.flatMap { it.children.filterIsInstance<InteractorComponent>() }

    override val renderSize get() = parent.getComponent<RenderedComponent>()?.renderSize ?: Vec2(1f, 1f)

    override val fields: Array<Field<*>> = arrayOf(
        BooleanField("hvr", ::hover){ hover = it },
        BooleanField("psd", ::pressed){ pressed = it },
        BooleanField("fud", ::forceUpdate){ forceUpdate = it },
        IntField("ipt", ::importance){ importance = it },
        //CollectionField("cld", ::interactableChildren, { interactableChildren.addAll(it)}, ",", StorableField::serialise, StorableField::parse, Collection<Interactable>::toSet, ::DefaultFieldEditor)
    )

    override fun checkHover(ray: Ray, screenPos: Vec2): Boolean{
        val renderer = parent.getComponent<RenderedComponent>()
        if(renderer != null){
            val shape = renderer.shape as? Rect2D
            if(shape != null){
                val unitSize = Vec2(parent.transformComponent.worldScale) * renderer.renderSize
                return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition) + (shape.origin * unitSize), unitSize * shape.size)
            }
        }
        return screenPos.isWithin(Vec2(parent.transformComponent.worldPosition), Vec2(parent.transformComponent.worldScale) * renderSize)
    }

    override fun onCursorMove(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray){
        if(pressed) onDrag(window, cursorPos, cursorDelta, ray)
    }

    override fun onDrag(window: WindowI, cursorPos: Vec2, cursorDelta: Vec2, ray: Ray) {}

    override fun onScroll(window: WindowI, scrollDelta: Vec2): Int{
        return 0
    }

    override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
        return when {
            input.i == 0 && input.controlType == ControlType.MOUSE -> onPrimary(window, action, input.mods, cursorPos)
            input.i == 1 && input.controlType == ControlType.MOUSE -> onSecondary(window, action, input.mods, cursorPos)
            else -> 0
        }
    }

    override fun onType(window: WindowI, char: Char): Int{
        return 0
    }

    // This is the default action when an item is clicked
    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        // Continue pressing if already pressed, or start pressing on first click
        pressed = (pressed && action == GLFW.GLFW_REPEAT) || action == GLFW.GLFW_PRESS
        return action
    }

    override fun onSecondary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int = 0

    override fun shouldUpdate(): Boolean {
        return this.hover || this.pressed || this.forceUpdate || this.interactableChildren.any { child -> child.shouldUpdate() }
    }

    companion object {
        const val INTERRUPT = InteractorComponent.INTERRUPT
    }
}