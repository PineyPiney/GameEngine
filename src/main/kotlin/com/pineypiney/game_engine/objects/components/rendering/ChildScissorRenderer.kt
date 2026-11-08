package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.PixelTransformComponent
import com.pineypiney.game_engine.objects.components.TransformComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.window.Viewport
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class ChildScissorRenderer(
	parent: GameObject,
	val sort: GameObject.() -> Float = { transformComponent.worldPosition.z }
) : RenderedComponent(parent) {

	override fun init() {
		super.init()

		// This uses a scissor so will render its children on its own
		parent.throwKidsAtRenderer = false
	}

	override fun getMeshes(): Collection<Mesh> = emptyList()

	override fun render(renderer: RendererI, tickDelta: Double) {

		val api = renderer.getRenderingApi()
		val transform = parent.transformComponent

		val viewport = renderer.getViewport()
		if(transform is PixelTransformComponent && transform.screenRelative) {
			val scissorOrigin = Vec2i((transform.origin * .5f + Vec2(.5f)) * renderer.getViewport().size) + transform.pixelPos
			val view = Viewport(scissorOrigin, scissorOrigin + transform.pixelScale)
			api.setScissors(view)
		}
		else {
			val scissorOrigin = Vec2i((Vec2(transform.worldPosition) + Vec2(viewport.aspectRatio, 1f)) * .5f * viewport.size.y)
			val size = Vec2i(Vec2(transform.worldScale) * .5f * viewport.size.y)
			val view = Viewport(scissorOrigin, scissorOrigin + size)
			api.setScissors(view)
		}

		val descendants = mutableSetOf<GameObject>()
		parent.children.forEach { it.catchRenderingComponents(descendants) }
		val sortedDescendants = descendants.sortedBy(sort)
		for(o in sortedDescendants){
			val renderers = o.components.filterIsInstance<RenderedComponentI>()
			val preRenderers = o.components.filterIsInstance<PreRenderComponent>().sortedByDescending { it is TransformComponent }
			if(renderers.isEmpty()){
				preRenderers.forEach { it.preRender(renderer, tickDelta) }
			}
			else{
				val vis = renderers.any { it.visible }
				preRenderers.forEach{ if(vis || !it.whenVisible) it.preRender(renderer, tickDelta)}
				renderers.forEach{ if (it.visible) it.render(renderer, tickDelta)}
			}
		}

		api.setScissors(renderer.getViewport())
	}
}