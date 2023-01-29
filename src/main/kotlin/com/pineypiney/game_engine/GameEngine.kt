package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourcesLoader
import glm_.c
import glm_.f
import org.lwjgl.glfw.GLFW

abstract class GameEngine<E: GameLogicI>(final override val resourcesLoader: ResourcesLoader): GameEngineI<E> {

    override val timer: Timer = Timer()

    private var nextUpdateTime: Double = Timer.getCurrentTime()
    private var FPSCounter: Int = 0
    private val FPSInterval: Float = 1f
    override var FPS: Float = 0f

    init {
        // Load the resources for the game
        resourcesLoader.loadResources()
    }

    override fun run() {
        init()
        gameLoop()
        cleanUp()
    }

    override fun init(){

        timer.init()
        activeScreen.init()

        window.setFrameBufferResizeCallback { activeScreen.updateAspectRatio(window) }
        setInputCallbacks()
    }

    override fun gameLoop(){

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
            if(Timer.frameTime > nextUpdateTime){
                updateFPS()
            }

            if (!window.vSync) {
                // sync means that the game only runs game loops at the intended FPS
                sync()
            }
        }
    }

    override fun setInputCallbacks(){
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

    override fun update(interval: Float) {
        timer.tick()
        activeScreen.update(interval, input)
    }

    override fun render(tickDelta: Double) {
        activeScreen.render(window, tickDelta)
        window.update()
    }

    override fun input(){
        input.input()
    }

    override fun sync() {
        val loopSlot = 1f / TARGET_FPS
        val endTime: Double = Timer.frameTime + loopSlot
        while (Timer.getCurrentTime() < endTime) {
            try {
                Thread.sleep(0, 100000)
            }
            catch (_: InterruptedException) {
            }
        }
    }

    fun updateFPS(){
        FPS = FPSCounter.f / FPSInterval
        FPSCounter = 0
        nextUpdateTime = Timer.frameTime + FPSInterval
    }

    override fun cleanUp() {
        resourcesLoader.cleanUp()
        activeScreen.cleanUp()
        GLFW.glfwTerminate()
    }
}