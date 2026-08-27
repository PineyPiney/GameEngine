package com.pineypiney.game_engine.resources.shaders

import com.pineypiney.game_engine.objects.Deletable

interface ShaderModule : Deletable {
	fun getName(): String
	fun getStage(): ShaderStage
}