package com.pineypiney.game_engine.rendering

interface PresentingApi : RenderingApi {
	fun beginPresentation(): Boolean
	fun copyFramebuffer(framebuffer: Framebuffer, renderer: WindowRendererI<*>)
	fun present()
}