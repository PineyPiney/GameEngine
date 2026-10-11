package com.pineypiney.game_engine.util

class TallyMap<K, V>(val one: V, val plus: (V, V) -> V) : LinkedHashMap<K, V>() {

	fun inc(key: K, value: V = one) {
		val v = get(key)
		if (v == null) set(key, value)
		else set(key, plus(v, value))
	}

	companion object {
		fun <K> int() = TallyMap<K, Int>(1, Int::plus)
		fun <K> double() = TallyMap<K, Double>(1.0, Double::plus)
	}
}