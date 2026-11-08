package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.Window
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class OpenGlPresentRendering(val window: Window) : RenderingApi by OpenGlRendering, PresentingApi {

	override fun beginPresentation(): Boolean = !window.minimised

	override fun copyFramebuffer(framebuffer: Framebuffer, window: Window, renderer: RendererI) {

		GLFunc.clearColour = Vec4(0f)
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT)

		GLFunc.viewportO = window.framebufferSize
		PresentingApi.screenShader.setUp(PresentingApi.screenUniforms, renderer)
		(framebuffer as OpenGlFramebuffer).draw(this, Mesh.screenQuadShape)
	}

	override fun drawFramebuffer(framebuffer: Framebuffer, mesh: Mesh, window: Window, renderer: RendererI) {
		GLFunc.viewportO = window.framebufferSize
		PresentingApi.screenShader.setUp(PresentingApi.screenUniforms, renderer)
		(framebuffer as OpenGlFramebuffer).colour.bind()
		PresentingApi.screenShader.draw(mesh, renderer)
	}

	override fun present() {

	}
}