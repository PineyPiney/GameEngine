package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import glm_.d

class Debug {

	val times = mutableListOf<Double>()

	fun start(): Debug {
		times.clear()
		times.add(millis())
		return this
	}

	fun add() {
		times.add(millis())
	}

	fun differences() = (1..<times.size).map { times[it] - times[it - 1] }

	fun printDiffs() {
		if (times.size <= 1) return
		GameEngineI.logger.debug("Times are " + differences().joinToString())
	}

	companion object {
		fun millis() = System.nanoTime().d / 1000000.0
		fun micros() = System.nanoTime().d / 1000.0
	}
}