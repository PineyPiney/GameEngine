package com.pineypiney.game_engine_test.testVR

import com.pineypiney.game_engine.rendering.BufferedGameRenderer
import com.pineypiney.game_engine.rendering.Framebuffer
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.vr.HMD
import com.pineypiney.game_engine.vr.VRRenderer
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11

class TestVRRenderer(w: Int, h: Int, override val hmd: HMD): VRRenderer<TestVRGame>(w, h) {

	override var view: Mat4 = I

	val viewOffset = Vec3()

	override fun init() {
		super.init()
		GLFunc.clearColour = Vec4(0, 1, 0, 1)
	}

	override fun render(game: TestVRGame, tickDelta: Double) {
		GLFunc.viewportO = Vec2i(leftFramebuffer.width, leftFramebuffer.height)
		GLFunc.multiSample = true
		drawScene(game, 0, leftFramebuffer, tickDelta)
		drawScene(game, 1, rightFramebuffer, tickDelta)

		GLFunc.multiSample = false
		blitBuffer(leftFramebuffer, leftDisplay)
		blitBuffer(rightFramebuffer, rightDisplay)

		submitFrames(leftDisplay.TCB, rightDisplay.TCB)
		GL11.glGetError()

		// Draws output to the test window
		Framebuffer.unbind()
		clear()

		GLFunc.viewportO = game.gameEngine.window.framebufferSize

		val shader = BufferedGameRenderer.screenShader
		shader.use()
		shader.setUniforms(BufferedGameRenderer.screenUniforms, this)
		leftDisplay.draw(Mesh.textureQuad(Vec2(-0.5, 0), Vec2(1, 2)))
		rightDisplay.draw(Mesh.textureQuad(Vec2(0.5, 0), Vec2(1, 2)))
		game.gameEngine.window.update()
	}

	override fun getView(eye: Int): Mat4 {
		return super.getView(eye) * (I.translate(viewOffset).inverse())
	}
}