package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.vec2.Vec2

abstract class WindowGameLogic : GameLogic() {


    abstract override val gameEngine: WindowedGameEngineI<*>
    val window get() = gameEngine.window
    val input get() = gameEngine.window.input
    abstract override val renderer: WindowRendererI<*>

    override fun open() {
        // Force update everything
        gameObjects.update(0f)

        // Reset textures so that the last bound texture isn't carried over
        Texture.broke.bind()
    }

    open fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
        val ray = renderer.camera.getRay()
        for (item in gameObjects.getAllInteractables()){
            item.hover = item.checkHover(ray, cursorPos)
            if(item.shouldUpdate()) item.onCursorMove(this, cursorPos, cursorDelta, ray)
        }
    }

    open fun onScroll(scrollDelta: Vec2): Int {
        for (item in gameObjects.getAllInteractables()){
            if(item.shouldUpdate()){
                if(item.onScroll(this, scrollDelta) == Interactable.INTERRUPT) return Interactable.INTERRUPT
            }
        }
        return 0
    }

    open fun onInput(state: InputState, action: Int): Int {

        val mousePos = input.mouse.lastPos
        for(item in gameObjects.getAllInteractables()){
            if(item.shouldUpdate()){
                if(item.onInput(this, state, action, mousePos) == Interactable.INTERRUPT) return Interactable.INTERRUPT
            }
        }

        when {
            state.i == 0 && state.controlType == ControlType.MOUSE -> onPrimary(gameEngine.window, action, state.mods)
            state.i == 1 && state.controlType == ControlType.MOUSE -> onSecondary(gameEngine.window, action, state.mods)
        }
        return action
    }

    open fun onType(char: Char): Int {
        for (item in gameObjects.getAllInteractables()){
            if(item.shouldUpdate()){
                if(item.onType(this, char) == Interactable.INTERRUPT) return Interactable.INTERRUPT
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
        gameObjects.getAllObjects().forEachInstance<Drawable> {
            it.updateAspectRatio(window)
        }
    }
}