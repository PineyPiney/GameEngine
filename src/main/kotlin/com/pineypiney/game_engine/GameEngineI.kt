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
        var defaultFont = ""
        val logger = KotlinLogging.logger("Game Engine")

        fun info(msg: String, obj: Any? = null) = logger.info(msg, obj)
        fun debug(msg: String, obj: Any? = null) = logger.debug(msg, obj)
        fun warn(msg: String, obj: Any? = null) = logger.warn(msg, obj)
        fun error(msg: String, obj: Any? = null) = logger.error(msg, obj)
        fun trace(msg: String, obj: Any? = null) = logger.trace(msg, obj)
    }
}
