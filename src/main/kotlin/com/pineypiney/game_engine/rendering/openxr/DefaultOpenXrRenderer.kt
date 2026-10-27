package com.pineypiney.game_engine.rendering.openxr

import com.pineypiney.game_engine.rendering.PresentingApi
import com.pineypiney.game_engine.util.extension_functions.rigidInverse
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.Viewport
import com.pineypiney.game_engine.window.Window
import com.pineypiney.game_engine.xr.OpenXrGameLogicI
import com.pineypiney.game_engine.xr.XrUtil
import com.pineypiney.game_engine.xr.glm
import com.pineypiney.game_engine.xr.mat4
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.openxr.XrView

open class DefaultOpenXrRenderer<in E: OpenXrGameLogicI>(window: Window, val renderManager: OpenXrRenderManager) : OpenXrRendererI<E> {

	override val viewPos: Vec3 = Vec3(0f)
	override val view: Mat4 = I
	override val projection: Mat4 = I
	override val guiProjection: Mat4 = I
	override val viewportSize: Vec2i = window.framebufferSize
	override val aspectRatio: Float get() = viewportSize.x.toFloat() / viewportSize.y.toFloat()

	val api: PresentingApi = renderManager.session.manager.manager.createPresentingApi(window, "OpenXR Renderer")

	override fun init() {

	}

	override fun render(game: E, tickDelta: Double) {
		if (!api.beginPresentation()) return

		api.bindFramebuffer(renderManager.swapchain.framebuffer, Vec4(.8f, .2f, .8f, 0f), renderManager.session.manager.manager.minDepth())

		renderLayer(0, game, tickDelta)
		renderLayer(1, game, tickDelta) { transformComponent.worldPosition.z }

		api.endFramebuffer(renderManager.swapchain.framebuffer)

		api.setViewport(Viewport(Vec2i(0), renderManager.session.manager.window.framebufferSize))
		api.copyFramebuffer(renderManager.swapchain.framebuffer, renderManager.session.manager.window, this)

		api.present()
	}

	override fun updateView(view: XrView) {
		viewPos(view.pose().`position$`().glm())
		XrUtil.createProjection(view.fov(), 100f, .05f, renderManager.session.manager.manager.invertY(), projection)
		guiProjection(projection)
		view.pose().mat4().rigidInverse(this.view)
	}

	override fun getRenderingApi(): PresentingApi = api

	override fun delete() {
		api.delete()
	}
}