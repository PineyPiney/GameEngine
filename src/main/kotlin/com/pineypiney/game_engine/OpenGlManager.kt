package com.pineypiney.game_engine

import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.rendering.opengl.OpenGlPresentRendering
import com.pineypiney.game_engine.rendering.opengl.OpenGlRendering
import com.pineypiney.game_engine.resources.OpenGlResourceFactory
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.util.ApiEnum
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import org.lwjgl.opengl.GL11C

object OpenGlManager : ApiManager {

	override fun init() {

	}

	override val getEnum: (ApiEnum) -> Int = ApiEnum::opengl

	override fun invertY(): Boolean  = false

	override fun createResourceFactory(): ResourceFactory {
		return OpenGlResourceFactory()
	}

	override fun createRenderingApi(name: String): RenderingApi = OpenGlRendering

	override fun createPresentingApi(window: WindowI, name: String): PresentingApi {
		return OpenGlPresentRendering(window)
	}

	override fun waitIdle() {
		GL11C.glFinish()
	}

	override fun getMaxTextureSide1D(): Int = GLFunc.maxTextureSize
	override fun getMaxTextureSide2D(): Int = GLFunc.maxTextureSize
	override fun getMaxTextureSide3D(): Int = GLFunc.maxTextureSize3D
	override fun getMaxVertexAttribs(): Int = GLFunc.maxVertexAttribs
	override fun getMaxViewportDimensions(): Vec2i = GLFunc.maxViewPort

	override fun getMaxTesselationPatchVertices(): Int = GLFunc.maxPatchVertices
	override fun getMaxTesselationLevel(): Int = GLFunc.maxTessLevel
	override fun getMaxComputeWorkGroupSize(): Vec3i = GLFunc.maxComputeWorkGroupSize
	override fun getMaxComputeWorkGroupCount(): Vec3i = GLFunc.maxComputeWorkGroupCount

	override fun delete() {

	}
}