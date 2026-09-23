package com.pineypiney.game_engine.rendering.opengl

import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.GameRenderer.Companion.screenShader
import com.pineypiney.game_engine.rendering.GameRenderer.Companion.screenUniforms
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.GLFunc
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class OpenGlPresentRendering(val renderer: WindowRendererI<*>) : RenderingApi by OpenGlRendering, PresentingApi {

	override fun beginPresentation() {

	}

	override fun copyFramebuffer(framebuffer: Framebuffer, renderer: WindowRendererI<*>) {

		GLFunc.clearColour = Vec4(0f)
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT)

		GLFunc.viewportO = renderer.window.framebufferSize
		screenShader.setUp(screenUniforms, renderer)
		(framebuffer as OpenGlFramebuffer).draw(this, Mesh.screenQuadShape)
	}

	override fun present() {

	}
}