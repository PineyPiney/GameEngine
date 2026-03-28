package com.pineypiney.game_engine.util

object RandomHelper {

	fun <T> createMask(function: (T) -> Boolean, vararg values: T): Int {
		return values.withIndex().sumOf { (i, s) -> if (function(s)) 1 shl i else 0 }
	}
}