package com.pineypiney.game_engine

import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.rendering.RenderingApi
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.util.ApiEnum
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

interface ApiManager : Initialisable {

	val getEnum: (ApiEnum) -> Int
	fun invertY(): Boolean

	fun createResourceFactory(): ResourceFactory
	fun createRenderingApi(name: String): RenderingApi
	fun createPresentingApi(window: WindowI, name: String): PresentingApi
	fun waitIdle()

	fun getMaxTextureSide1D(): Int
	fun getMaxTextureSide2D(): Int
	fun getMaxTextureSide3D(): Int
	fun getMaxVertexAttribs(): Int
	fun getMaxViewportDimensions(): Vec2i

	fun getMaxTesselationPatchVertices(): Int
	fun getMaxTesselationLevel(): Int
	fun getMaxComputeWorkGroupSize(): Vec3i
	fun getMaxComputeWorkGroupCount(): Vec3i

	fun printInfo(): String {
		return StringBuilder()
			.append("Max 1D Texture Size: ${getMaxTextureSide1D()}\n")
			.append("Max 2D Texture Size: ${getMaxTextureSide2D()}\n")
			.append("Max 3D Texture Size: ${getMaxTextureSide3D()}\n")
			.append("Max Vertex Attribs: ${getMaxVertexAttribs()}\n")
			.append("Max Viewport Dimensions: ${getMaxViewportDimensions()}\n")
			.append("Max Tesselation Path Vertices: ${getMaxTesselationPatchVertices()}\n")
			.append("Max Tesselation Level: ${getMaxTesselationLevel()}\n")
			.append("Max Compute Work Group Size: ${getMaxComputeWorkGroupSize()}\n")
			.append("Max Compute Work Group Count: ${getMaxComputeWorkGroupCount()}\n")
			.toString()
	}
}