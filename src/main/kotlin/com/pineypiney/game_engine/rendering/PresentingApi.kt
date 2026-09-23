package com.pineypiney.game_engine.rendering

interface PresentingApi : RenderingApi {
	fun beginPresentation()
	fun copyFramebuffer(framebuffer: Framebuffer, renderer: WindowRendererI<*>)
	fun present()
}