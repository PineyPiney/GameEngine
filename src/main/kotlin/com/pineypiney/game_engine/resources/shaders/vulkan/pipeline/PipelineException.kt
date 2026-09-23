package com.pineypiney.game_engine.resources.shaders.vulkan.pipeline

class PipelineException : Exception {
	constructor(message: String) : super(message)
	constructor(cause: Throwable) : super(cause)
	constructor(message: String, cause: Throwable) : super(message, cause)
}