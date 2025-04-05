package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.RenderedComponent
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import glm_.glm
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL30C

open class DefaultWindowRenderer<G: WindowGameLogic, R: CameraI>(override val window: WindowI, override val camera: R): BufferedGameRenderer<G>() {

	constructor(window: WindowI, camera: (WindowI) -> R): this(window, camera(window))

	override val view = I
	override val projection = I
	override val guiProjection = I

	override fun init() {
		super.init()

		GLFunc.blend = true
		GLFunc.blendFunc = Vec2i(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		screenUniforms.setIntUniform("effects") { 0 }
	}

	override fun render(game: G, tickDelta: Double) {

		camera.getView(view)
		camera.getProjection(projection)

		clearFrameBuffer()

		GLFunc.depthTest = true
		renderLayer(0, game, tickDelta, buffer)

		GLFunc.depthTest = false
		renderLayer(1, game, tickDelta, buffer){ transformComponent.worldPosition.z }

		// This draws the buffer onto the screen
		FrameBuffer.unbind()
		clear()
		screenShader.setUp(screenUniforms, this)
		buffer.draw()
		glClear(GL_DEPTH_BUFFER_BIT)
	}

	fun renderLayer(layer: Int, game: G, tickDelta: Double, framebuffer: FrameBuffer? = null, sort: GameObject.() -> Float = { -(transformComponent.worldPosition - camera.cameraPos).length2() }) = renderLayer(game.gameObjects[layer], tickDelta, framebuffer?.FBO ?: 0, sort)

	open fun renderLayer(layer: Collection<GameObject>, tickDelta: Double, framebuffer: Int = 0, sort: GameObject.() -> Float = { -(transformComponent.worldPosition - camera.cameraPos).length2() }){
		for(o in layer.flatMap { it.catchRenderingComponents() }.sortedBy(sort)) {
			renderObject(o, tickDelta, framebuffer)
		}
	}

	open fun renderObject(obj: GameObject, tickDelta: Double, framebuffer: Int = 0){
		val renderedComponents = obj.components.filterIsInstance<RenderedComponent>().filter { it.visible }
		if(renderedComponents.isNotEmpty()){
			for(c in obj.components.filterIsInstance<PreRenderComponent>()) c.preRender(this, tickDelta)
			GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, framebuffer)
			for(c in renderedComponents) c.render(this, tickDelta)
		}
		else for(c in obj.components.filterIsInstance<PreRenderComponent>()){
			if(!c.whenVisible) c.preRender(this, tickDelta)
		}
	}

	override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
		super.updateAspectRatio(window, objects)
		val w = window.aspectRatio
		glm.ortho(-w, w, -1f, 1f, guiProjection)
	}
}