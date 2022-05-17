package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.Visual
import com.pineypiney.game_engine.objects.game_objects.InteractableGameObject
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.KeyBind
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

abstract class GameLogic : IGameLogic {

    override var gameObjects: ScreenObjectCollection = ScreenObjectCollection()

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
            if(item.hover) item.onCursorMove(this, cursorPos, cursorDelta)
        }
    }

    override fun onScroll(window: Window, scrollDelta: Vec2) {
        for (item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java)){
            if(item.hover || item.forceUpdate){
                if(item.onScroll(this, scrollDelta) == 1) break
            }
        }
    }

    override fun onInput(key: KeyBind, action: Int) {

        if(key.c == 'F' && action == 1){
            setFullscreen(!gameEngine.window.fullScreen)
        }
        if(key.i == GLFW.GLFW_KEY_ESCAPE && action == 1){
            window.setShouldClose()
        }


        for(item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java).sortedByDescending { it.importance }){
            val stop = conditionalInput(item, key, action)
            if(stop) return
        }

        when {
            key.matches(this.input.primary) -> onPrimary(Window.INSTANCE, action, key.mods)
            key.matches(this.input.secondary) -> onSecondary(Window.INSTANCE, action, key.mods)
        }
    }

    private fun conditionalInput(item: Interactable, key: KeyBind, action: Int): Boolean{
        var stop = false
        if(item.shouldUpdate()){
            val mousePos = input.mouse.lastPos
            stop = item.onInput(this, key, action, mousePos) == InteractableGameObject.INTERRUPT

            for(child in item.children.sortedByDescending { it.importance }){
                child.hover = child.checkHover(mousePos, camera.screenToWorld(mousePos))
                if(child.shouldUpdate()){
                    if(child.onInput(this, key, action, mousePos) == InteractableGameObject.INTERRUPT) break
                }
            }
        }
        return stop
    }

    open fun onPrimary(window: Window, action: Int, mods: Byte){}
    open fun onSecondary(window: Window, action: Int, mods: Byte){}

    open fun setFullscreen(state: Boolean){
        gameEngine.window.fullScreen = state
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