package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.KeyBind
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Storable
import glm_.vec2.Vec2

abstract class GameLogic(final override val gameEngine: GameEngine) : IGameLogic {

    override var gameObjects: ScreenObjectCollection = ScreenObjectCollection()

    override val input get() = gameEngine.input

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
        for (item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java)){
            item.onCursorMove(this, cursorPos, cursorDelta)
        }
    }

    override fun onScroll(window: Window, scrollDelta: Vec2) {
        for (item in gameObjects.getAllObjects().filterIsInstance(Interactable::class.java)){
            if(item.hover || item.forceUpdate){
                if(item.onScroll(this, scrollDelta) == 1) break
            }
        }
    }

    open fun setFullscreen(state: Boolean){
        Window.INSTANCE.fullScreen = state
    }

    override fun onInput(key: KeyBind, action: Int) {

        if(key.matches(ResourceKey("key/fullscreen"), this.input) && action == 1){
            setFullscreen(!Window.INSTANCE.fullScreen)
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
            stop = item.onInput(this, key, action, this.gameEngine.input.mouse.lastPos) == 1

            for(child in item.children.sortedByDescending { it.importance }){
                if(child.shouldUpdate()){
                    child.hover = child.checkHover()
                    if(child.onInput(this, key, action, this.gameEngine.input.mouse.lastPos) == 1) break
                }
            }
        }
        return stop
    }

    open fun onPrimary(window: Window, action: Int, mods: Byte){}
    open fun onSecondary(window: Window, action: Int, mods: Byte){}

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
    }

    override fun render(window: Window, tickDelta: Double) {
        renderer.render(window, camera, this, tickDelta)
    }

    override fun cleanUp() {
        gameObjects.delete()
        renderer.delete()
    }
}