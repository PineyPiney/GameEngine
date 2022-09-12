//do you have a hernia
package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourcesLoader
import glm_.c
import glm_.f
import mu.KotlinLogging
import org.lwjgl.glfw.GLFW.glfwTerminate

abstract class GameEngine<E: IGameLogic>(val resourcesLoader: ResourcesLoader) : Runnable {

    abstract val window: Window

    abstract var TARGET_FPS: Int
    abstract val TARGET_UPS: Int

    abstract val activeScreen: E

    private val timer = Timer()
    val input; get() = window.input

    private var nextUpdateTime: Double = Timer.getCurrentTime()
    private var FPSCounter: Int = 0
    var FPS: Float = 0f; private set

    init {
        // Load the resources for the game
        resourcesLoader.loadResources()
    }

    override fun run() {
        init()
        gameLoop()
        cleanUp()
    }

    protected open fun init(){

        timer.init()
        activeScreen.init()

        window.setResizeCallback { window -> activeScreen.updateAspectRatio(window) }
        setInputCallbacks()
    }

    protected open fun gameLoop(){

        var frameTime: Double
        var accumulator = 0.0
        val interval: Float = 1f / TARGET_UPS

        while (!window.shouldClose) {
            // elapsed time is the time since this function was last called
            frameTime = timer.tickFrame()

            // accumulator adds up elapsed time
            accumulator += frameTime

            // Once the accumulator exceeds the interval, the game is updated
            // and the accumulator reduces by interval amount.
            // Advantage of doing it this way is that if there is lag, then the game will catch up with itself
            while (accumulator >= interval) {
                update(interval)
                accumulator -= interval
            }

            input()

            // Render screen regardless of the accumulator
            render(accumulator / interval)

            FPSCounter++
            val t = Timer.frameTime
            if(t > nextUpdateTime){
                FPS = FPSCounter.f / 5f
                FPSCounter = 0
                nextUpdateTime = t + 5
            }

            // sync means that the game only runs game loops at the intended FPS
            if (!window.vSync) {
                sync()
            }
        }
    }

    open fun setInputCallbacks(){
        input.mouseMoveCallback = { screenPos, cursorOffset ->
            activeScreen.onCursorMove(screenPos, cursorOffset)
        }
        input.mouseScrollCallback = { scrollOffset ->
            activeScreen.onScroll(scrollOffset)
        }
        input.keyPressCallback = { bind, action ->
            activeScreen.onInput(bind, action)
        }
        input.keyboardCharCallback = { codepoint ->
            activeScreen.onType(codepoint.c)
        }
    }

    protected open fun update(interval: Float) {
        timer.tick()
        activeScreen.update(interval, input)
    }

    protected open fun render(tickDelta: Double) {
        activeScreen.render(window, tickDelta)
        window.update()
    }

    protected open fun input(){
        input.input()
    }

    private fun sync() {
        val loopSlot = 1f / TARGET_FPS
        val endTime: Double = Timer.time + loopSlot
        while (Timer.getCurrentTime() < endTime) {
            try {
                Thread.sleep(1)
            }
            catch (_: InterruptedException) {
            }
        }
    }

    protected open fun cleanUp() {
        resourcesLoader.cleanUp()
        activeScreen.cleanUp()
        glfwTerminate()
    }

    companion object {
        val logger = KotlinLogging.logger("Game Engine")
        var defaultFont = ""
    }
}
