package com.pineypiney.game_engine

import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.opengl.OpenGlPresentRendering
import com.pineypiney.game_engine.resources.OpenGlResourceFactory
import com.pineypiney.game_engine.resources.ResourceFactory
import org.lwjgl.opengl.GL11C

object OpenGlManager : ApiManager {

	override fun createResourceFactory(): ResourceFactory {
		return OpenGlResourceFactory()
	}

	override fun createPresentingApi(renderer: WindowRendererI<*>, name: String): PresentingApi {
		return OpenGlPresentRendering(renderer)
	}

	override fun waitIdle() {
		GL11C.glFinish()
	}

	override fun delete() {

	}
}