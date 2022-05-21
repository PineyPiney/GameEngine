package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.Visual
import com.pineypiney.game_engine.resources.textures.Texture
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
        for (item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java)){
            item.hover = item.checkHover(cursorPos, worldPos)
            if(item.shouldUpdate()) item.onCursorMove(this, cursorPos, cursorDelta)
        }
    }

    override fun onScroll(window: Window, scrollDelta: Vec2): Int {
        for (item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java).sortedByDescending { it.importance }){
            if(item.shouldUpdate()){
                if(item.onScroll(this, scrollDelta) == Interactable.INTERRUPT) return Interactable.INTERRUPT
            }
        }
        return 0
    }

    override fun onInput(key: InputState, action: Int): Int {

        val mousePos = input.mouse.lastPos
        for(item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java).sortedByDescending { it.importance }){
            if(item.shouldUpdate()){
                if(item.onInput(this, key, action, mousePos) == Interactable.INTERRUPT) return Interactable.INTERRUPT
            }
        }

        when {
            key.i == 0 && key.controlType == ControlType.MOUSE -> onPrimary(Window.INSTANCE, action, key.mods)
            key.i == 1 && key.controlType == ControlType.MOUSE -> onSecondary(Window.INSTANCE, action, key.mods)
        }
        return action
    }

    override fun onType(char: Char): Int {
        for (item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java).sortedByDescending { it.importance }){
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
        gameObjects.getAllObjects().filterIsInstance<Visual>().forEach {
            it.updateAspectRatio(window)
        }
    }

    override fun render(window: Window, tickDelta: Double) {
        renderer.render(window, camera, this, tickDelta)
    }

    override fun cleanUp() {
        gameObjects.delete()
        renderer.delete()
    }
}