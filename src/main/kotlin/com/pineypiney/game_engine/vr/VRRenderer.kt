package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponent
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.GameRendererI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.getTranslation
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.vr.util.logCompositorError
import glm_.L
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL30
import org.lwjgl.openvr.VR
import org.lwjgl.openvr.VRCompositor
import org.lwjgl.openvr.Texture as VRTexture

abstract class VRRenderer<E : GameLogicI>(w: Int, h: Int) : GameRendererI<E> {

	val leftFramebuffer = Framebuffer(w, h)
	val rightFramebuffer = Framebuffer(w, h)

	val leftDisplay = VRFramebuffer(w, h)
	val rightDisplay = VRFramebuffer(w, h)

	abstract val hmd: HMD

	override var viewPos: Vec3 = Vec3(0f)
	override var view: Mat4 = I
	override var projection: Mat4 = I
	override val guiProjection: Mat4 = I
	override var viewportSize: Vec2i = Vec2i(1)
	override val aspectRatio: Float = w.toFloat() / h

	override fun init() {
		leftFramebuffer.generate()
		rightFramebuffer.generate()
		leftDisplay.generate()
		rightDisplay.generate()
	}

	fun drawScene(game: GameLogicI, eye: Int, buffer: Framebuffer, tickDelta: Double) {

		clearFrameBuffer(buffer)

		viewPos = hmd.hmdPose.getTranslation()
		view = getView(eye)
		projection = getProjection(eye)

		GLFunc.depthTest = true
		for (o in game.gameObjects.gameItems.flatMap { it.allActiveDescendants() }) {
			val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
			if (renderedComponents.isNotEmpty()) {
				for (c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
				for (c in renderedComponents) c.render(this, tickDelta)
			}
		}

		GLFunc.depthTest = false
		for (o in game.gameObjects.guiItems.flatMap { it.allActiveDescendants() }) {
			val renderedComponents = o.components.filterIsInstance<RenderedComponent>().filter { it.visible }
			if (renderedComponents.isNotEmpty()) {
				for (c in o.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
				for (c in renderedComponents) c.render(this, tickDelta)
			}
		}
	}

	fun blitBuffer(read: Framebuffer, draw: Framebuffer) {
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read.FBO)
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw.FBO)

		GL30.glBlitFramebuffer(
			0,
			0,
			read.width,
			read.height,
			0,
			0,
			draw.width,
			draw.height,
			GL30.GL_COLOR_BUFFER_BIT,
			GL30.GL_LINEAR
		)

		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0)
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0)
	}

	fun clearFrameBuffer(buffer: Framebuffer) {
		buffer.bind()
		viewportSize = Vec2i(buffer.width, buffer.height)
		clear()
	}

	fun submitFrames(leftTex: Int, rightTex: Int) {

		val lt = VRTexture.create().set(leftTex.L, VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma)
		val rt = VRTexture.create().set(rightTex.L, VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma)

		VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, lt, null, 0).logCompositorError()
		VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, rt, null, 0).logCompositorError()
	}

	open fun getView(eye: Int) = hmd.eyes[eye] * hmd.hmdPose
	open fun getProjection(eye: Int) = hmd.projections[eye]

	fun deleteFrameBuffers() {
		leftFramebuffer.delete()
		rightFramebuffer.delete()
		leftDisplay.delete()
		rightDisplay.delete()
	}

	override fun delete() {
		deleteFrameBuffers()
	}
}