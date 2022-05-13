//do you have a hernia
package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourceLoader
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.text.FontLoader
import glm_.f

abstract class GameEngine(val window: Window) : Runnable {

    abstract var TARGET_FPS: Int
    abstract val TARGET_UPS: Int

    abstract var activeScreen: IGameLogic

    val timer = Timer()
    val input = Inputs(window)

    var nextUpdateTime: Double = Timer.getCurrentTime()
    var FPSCounter: Int = 0
    var FPS: Float = 0f

    override fun run() {
        init()
        gameLoop()
        cleanUp()
    }

    private fun init(){
        println("Initialising GameEngine")

        // Load the resources for the game
        ResourceLoader.INSTANCE.loadResources()

        // Create all the fonts
        FontLoader.INSTANCE.loadFontWithTexture("Large Font.bmp", 128, 256, 8, null)
        FontLoader.INSTANCE.loadFontWithTexture("ExportedFont.png", 32, 64, 2)
        FontLoader.INSTANCE.loadFontWithTexture("PixelFont.png", 32, 64, 2)

        timer.init()
    }

    private fun gameLoop(){

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
                timer.tick()
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

    private fun update(interval: Float) {
        activeScreen.update(interval, input)
    }

    private fun render(tickDelta: Double) {
        activeScreen.render(window, tickDelta)

        window.update()
    }

    private fun cleanUp() {
        ResourceLoader.INSTANCE.cleanUp()
        activeScreen.cleanUp()
    }
}
