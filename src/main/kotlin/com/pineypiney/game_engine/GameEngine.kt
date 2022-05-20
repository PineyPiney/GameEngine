//do you have a hernia
package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourceLoader
import com.pineypiney.game_engine.util.text.FontLoader
import glm_.c
import glm_.f
import org.lwjgl.glfw.GLFW.glfwTerminate

abstract class GameEngine(val window: Window) : Runnable {

    abstract var TARGET_FPS: Int
    abstract val TARGET_UPS: Int

    abstract var activeScreen: IGameLogic

    private val timer = Timer()
    val input; get() = window.input

    private var nextUpdateTime: Double = Timer.getCurrentTime()
    private var FPSCounter: Int = 0
    var FPS: Float = 0f; private set

    init{
        // Load the resources for the game
        ResourceLoader.INSTANCE.loadResources()

        // Create all the fonts
        FontLoader.INSTANCE.loadFontWithTexture("Large Font.bmp", 128, 256, 8)
        FontLoader.INSTANCE.loadFontWithTexture("ExportedFont.png", 32, 64, 2)
        FontLoader.INSTANCE.loadFontWithTexture("PixelFont.png", 32, 64, 2)
    }

    override fun run() {
        init()
        gameLoop()
        cleanUp()
    }

    protected open fun init(){
        println("Initialising GameEngine")


        timer.init()
        this.activeScreen.init()

        window.setResizeCallback { window -> this.activeScreen.updateAspectRatio(window) }
        setInputCallbacks()
    }

    protected open fun gameLoop(){

        var frameTime: Double
        var accumulator = 0.0
        val interval: Float = 1f / TARGET_UPS

        while (!window.windowShouldClose()) {
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

    open fun setInputCallbacks(){
        input.mouseMoveCallback = { win, screenPos, cursorOffset ->
            activeScreen.onCursorMove(win, screenPos, cursorOffset)
        }
        input.mouseScrollCallback = { win, scrollOffset ->
            activeScreen.onScroll(win, scrollOffset)
        }
        input.keyPressCallback = { bind, action ->
            activeScreen.onInput(bind, action)
        }
        input.keyboardCharCallback = { codepoint ->
            activeScreen.onType(codepoint.c)
        }
    }

    private fun update(interval: Float) {
        timer.tick()
        activeScreen.update(interval, input)
    }

    private fun render(tickDelta: Double) {
        activeScreen.render(window, tickDelta)
        window.update()
    }

    protected open fun cleanUp() {
        ResourceLoader.INSTANCE.cleanUp()
        activeScreen.cleanUp()
        glfwTerminate()
    }
}
