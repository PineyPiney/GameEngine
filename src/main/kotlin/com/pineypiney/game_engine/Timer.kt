package com.pineypiney.game_engine

class Timer {

	fun init() {
		time = getCurrentTime()
		frameTime = getCurrentTime()
	}

	fun tick(): Double {
		val newTime = getCurrentTime()
		delta = newTime - time
		time = newTime
		return delta
	}

	fun tickFrame(): Double {
		val newTime = getCurrentTime()
		frameDelta = newTime - frameTime
		frameTime = newTime
		return frameDelta
	}

	companion object {
		/**
		 * The time the program started at in nanoseconds, relative to an arbitrary, program specific time
		 */
		var startTime = System.nanoTime()

		/**
		 * Time of last game update in seconds
		 */
		var time: Double = 0.0; private set

		/**
		 * Time between last game update and the one before in seconds
		 */
		var delta: Double = 0.0; private set
		/**
		 * Time of last game render in seconds
		 */
		var frameTime: Double = 0.0; private set
		/**
		 * Time between last game render and the one before in seconds
		 */
		var frameDelta: Double = 0.0; private set

		// Gets the current system time in seconds
		fun getCurrentTime(): Double {
			return (System.nanoTime() - startTime).toDouble() * 1e-9
		}

		fun getCurrentMillis(): Double = (System.nanoTime() - startTime).toDouble() * 1e-6
	}
}