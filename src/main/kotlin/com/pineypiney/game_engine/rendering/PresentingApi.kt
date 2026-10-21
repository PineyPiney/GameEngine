package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.window.WindowI

interface PresentingApi : RenderingApi {
	fun beginPresentation(): Boolean
	fun copyFramebuffer(framebuffer: Framebuffer, window: WindowI, renderer: RendererI)
	fun copyFramebuffer(framebuffer: Framebuffer, renderer: WindowRendererI<*>) = copyFramebuffer(framebuffer, renderer.window, renderer)
	fun present()
}