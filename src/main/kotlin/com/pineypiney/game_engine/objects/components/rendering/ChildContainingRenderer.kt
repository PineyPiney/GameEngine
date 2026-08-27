package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.TransformComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.StencilOp
import com.pineypiney.game_engine.resources.shaders.parameters.CompareOp
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ChildContainingRenderer(
	parent: GameObject,
	val mesh: Mesh,
	val colour: Vec4 = Vec4(0f),
	shader: RenderShader = ColourRendererComponent.menuShader,
	val sort: GameObject.() -> Float = { transformComponent.worldPosition.z }
) : ShaderRenderedComponent(parent, shader) {

	constructor(parent: GameObject, mesh: Mesh, colour: Vec3, shader: RenderShader = ColourRendererComponent.menuShader) : this(parent, mesh, Vec4(colour, 1f), shader)

	override fun init() {
		super.init()

		// This uses stencils so will render its children on its own
		parent.throwKidsAtRenderer = false
	}

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	override fun render(renderer: RendererI, tickDelta: Double) {

		val api = renderer.getRenderingApi()
		api.setStencilWriteMask(255)
		api.clearStencil(128)

		// If the stencil fails keep the old value, otherwise write 1 to the stencil
		api.setStencil(true, 8, 255, StencilOp.KEEP, StencilOp.REPLACE, StencilOp.REPLACE, CompareOp.ALWAYS)


		// Write to the stencil
		shader.setUp(uniforms, renderer)
		shader.draw("vertexBuffer", mesh, renderer)

		// Stencil only passes if the stencil value is 1, and don't write to the stencil
		api.setStencilComparison(8, 255, CompareOp.EQUAL)
		api.setStencilWriteMask(0)

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

		api.disableStencil()
	}
}