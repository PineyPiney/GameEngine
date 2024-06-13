package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.vec2.Vec2

abstract class WindowGameLogic : GameLogic() {


    abstract override val gameEngine: WindowedGameEngineI<*>
    open val window get() = gameEngine.window
    open val input get() = gameEngine.window.input
    abstract override val renderer: WindowRendererI<*>

    override fun open() {
        // Force update everything
        gameObjects.update(0f)

        // Reset textures so that the last bound texture isn't carried over
        Texture.broke.bind()

        updateAspectRatio(window)
        onCursorMove(gameEngine.input.mouse.lastPos, Vec2(0f))
    }

    open fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
        val ray = renderer.camera.getRay()
        for (component in gameObjects.getAllInteractables()){
            val oldHover = component.hover
            component.hover = component.checkHover(ray, cursorPos)
            if(!oldHover && component.hover) component.onCursorEnter(window, cursorPos, cursorDelta, ray)
            else if(oldHover && !component.hover) component.onCursorExit(window, cursorPos, cursorDelta, ray)
            if(component.shouldUpdate()) component.onCursorMove(window, cursorPos, cursorDelta, ray)
        }
    }

    open fun onScroll(scrollDelta: Vec2): Int {
        for (component in gameObjects.getAllInteractables()){
            if(component.shouldUpdate()){
                if(component.onScroll(window, scrollDelta) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT
            }
        }
        return 0
    }

    open fun onInput(state: InputState, action: Int): Int {

        val mousePos = input.mouse.lastPos
        for(component in gameObjects.getAllInteractables()){
            if(component.shouldUpdate()){
                if(component.onInput(window, state, action, mousePos) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT
            }
        }

        when {
            state.i == 0 && state.controlType == ControlType.MOUSE -> onPrimary(gameEngine.window, action, state.mods)
            state.i == 1 && state.controlType == ControlType.MOUSE -> onSecondary(gameEngine.window, action, state.mods)
        }
        return action
    }

    open fun onType(char: Char): Int {
        for (component in gameObjects.getAllInteractables()){
            if(component.shouldUpdate()){
                if(component.onType(window, char) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT
            }
        }
        return 0
    }

    open fun onPrimary(window: WindowI, action: Int, mods: Byte){}
    open fun onSecondary(window: WindowI, action: Int, mods: Byte){}

    open fun setFullscreen(monitor: Monitor?){
        window.monitor = monitor
    }

    open fun toggleFullscreen(){
        setFullscreen(if(window.fullScreen) null else Monitor.primary)
    }

    open fun updateAspectRatio(window: WindowI) {
        renderer.updateAspectRatio(window, gameObjects)
        for(r in gameObjects.getAllComponents().filterIsInstance<UpdatingAspectRatioComponent>()) r.updateAspectRatio(renderer)

    }
}