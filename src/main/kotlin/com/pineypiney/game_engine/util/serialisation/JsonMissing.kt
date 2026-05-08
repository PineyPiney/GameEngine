package com.pineypiney.game_engine.util.serialisation

import com.google.gson.JsonElement

@Suppress("DEPRECATION")
object JsonMissing : JsonElement() {

	override fun deepCopy(): JsonElement = this
}