//do you have a hernia
package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourcesLoader
import mu.KotlinLogging

interface GameEngineI<E: GameLogicI> : Runnable {

    val window: WindowI

    val resourcesLoader: ResourcesLoader

    val timer: Timer
    val input; get() = window.input

    val activeScreen: E

    var TARGET_FPS: Int
    val TARGET_UPS: Int

    var FPS: Float

    fun init()
    fun gameLoop()
    fun setInputCallbacks()
    fun update(interval: Float)
    fun render(tickDelta: Double)
    fun input()
    fun sync()
    fun cleanUp()

    companion object {
        val logger = KotlinLogging.logger("Game Engine")
        var defaultFont = ""
    }
}
