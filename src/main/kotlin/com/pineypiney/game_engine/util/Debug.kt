package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import glm_.d

class Debug {

	val times = mutableListOf<Pair<String, Double>>()

	fun start(): Debug {
		times.clear()
		times.add("Start" to millis())
		return this
	}

	fun add(label: String = times.size.toString()): Debug {
		times.add(label to millis())
		return this
	}

	fun time() = times.last().second - times.first().second

	fun differences() = (1..<times.size).map { times[it].first to times[it].second - times[it - 1].second }

	fun printDiffs() {
		if (times.size <= 1) return
		GameEngineI.logger.debug("Times are " + differences().joinToString())
	}

	companion object {
		fun millis() = System.nanoTime().d / 1000000.0
		fun micros() = System.nanoTime().d / 1000.0
	}
}