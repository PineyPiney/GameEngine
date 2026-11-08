package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.Window

interface PresentingApi : RenderingApi {
	fun beginPresentation(): Boolean
	fun copyFramebuffer(framebuffer: Framebuffer, window: Window, renderer: RendererI)
	fun copyFramebuffer(framebuffer: Framebuffer, renderer: WindowRendererI<*>) = copyFramebuffer(framebuffer, renderer.window, renderer)
	fun drawFramebuffer(framebuffer: Framebuffer, mesh: Mesh, window: Window, renderer: RendererI)
	fun present()

	companion object {
		val screenShader = ShaderLoader.loadRenderShader(ResourceKey("menu/screen"))
		val screenUniforms = screenShader.compileUniforms()
	}
}