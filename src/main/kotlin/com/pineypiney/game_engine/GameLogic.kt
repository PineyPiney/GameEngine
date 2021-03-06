package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.forEachInstance
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import glm_.vec2.Vec2

abstract class GameLogic : IGameLogic {

    override var gameObjects: ObjectCollection = ObjectCollection()

    override val input get() = gameEngine.window.input

    override fun init() {
        camera.init()
        renderer.init()
        addObjects()
        gameObjects.getAllObjects().init()
    }

    // This addObjects function allows adding items and calling init() on them all here in gameLogic, see init()
    abstract fun addObjects()

    override fun open() {
        // Force update everything
        gameObjects.update(0f)

        // Reset textures so that the last bound texture isn't carried over
        Texture.brokeTexture.bind()
    }

    override fun onCursorMove(window: Window, cursorPos: Vec2, cursorDelta: Vec2) {
        val worldPos = camera.screenToWorld(cursorPos)
        for (item in gameObjects.getAllInteractables()){
            item.hover = item.checkHover(cursorPos, worldPos)
            if(item.shouldUpdate()) item.onCursorMove(this, cursorPos, cursorDelta)
        }
    }

    override fun onScroll(window: Window, scrollDelta: Vec2): Int {
        for (item in gameObjects.getAllInteractables()){
            if(item.shouldUpdate()){
                if(item.onScroll(this, scrollDelta) == Interactable.INTERRUPT) return Interactable.INTERRUPT
            }
        }
        return 0
    }

    override fun onInput(state: InputState, action: Int): Int {

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

    override fun onType(char: Char): Int {
        for (item in gameObjects.getAllInteractables()){
            if(item.shouldUpdate()){
                if(item.onType(this, char) == Interactable.INTERRUPT) return Interactable.INTERRUPT
            }
        }
        return 0
    }

    open fun onPrimary(window: Window, action: Int, mods: Byte){}
    open fun onSecondary(window: Window, action: Int, mods: Byte){}

    open fun setFullscreen(state: Boolean){
        gameEngine.window.fullScreen = state
    }

    open fun toggleFullscreen(){
        setFullscreen(!gameEngine.window.fullScreen)
    }

    override fun add(o: Storable?){
        this.gameObjects.addObject(o)
    }

    override fun remove(o: Storable?){
        this.gameObjects.removeObject(o)
    }

    override fun update(interval: Float, input: Inputs) {
        gameObjects.update(interval)
    }

    override fun updateAspectRatio(window: Window) {
        renderer.updateAspectRatio(window, gameObjects)
        gameObjects.getAllObjects().forEachInstance<Drawable> {
            it.updateAspectRatio(window)
        }
    }

    override fun cleanUp() {
        gameObjects.delete()
        renderer.delete()
    }
}