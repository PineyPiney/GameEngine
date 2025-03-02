package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourcesLoader

abstract class GameEngine<E : GameLogicI>(final override val resourcesLoader: ResourcesLoader) : GameEngineI<E> {

	override val TARGET_FPS: Int = 2000
	override val TARGET_UPS: Int = 20

	override val timer: Timer = Timer()

	private var frameTime: Double = 0.0
	private var accumulator = 0.0
	protected var interval: Float = 1f

	init {
		// Load the resources for the game
		resourcesLoader.loadResources()
	}

	override fun run() {
		init()
		while (shouldRun()) gameLoop()
		cleanUp()
	}

	override fun init() {
		timer.init()
		activeScreen.init()
		interval = 1f / TARGET_UPS
	}

	override fun gameLoop() {
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

		try {
			// Render screen regardless of the accumulator
			render(accumulator / interval)
		}
		catch (e: Exception){
			GameEngineI.error("Error rendering frame")
			e.printStackTrace()
		}
	}

	override fun update(interval: Float) {
		timer.tick()
	}

	override fun sync() {
		val loopSlot = 1f / TARGET_FPS
		val endTime: Double = Timer.frameTime + loopSlot
		while (Timer.getCurrentTime() < endTime) {
			try {
				Thread.sleep(0, 100000)
			} catch (_: InterruptedException) {
			}
		}
	}

	override fun cleanUp() {
		resourcesLoader.cleanUp()
		activeScreen.cleanUp()
	}
}