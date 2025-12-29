package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.TransformComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class ChildContainingRenderer(parent: GameObject, val mesh: Mesh, val colour: Vec4 = Vec4(0f), val sort: GameObject.() -> Float = { transformComponent.worldPosition.z }) : ShaderRenderedComponent(parent, ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/colour")]){

	constructor(parent: GameObject, mesh: Mesh, colour: Vec3): this(parent, mesh, Vec4(colour, 1f))

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
		GL11C.glEnable(GL11C.GL_STENCIL_TEST)

		GL11C.glDisable(GL11C.GL_SCISSOR_TEST)
		GLFunc.stencilWriteMask = 255

		// Clear the whole stencil to value 0
		GL11C.glClearStencil(0)
		GL11C.glClear(GL11C.GL_STENCIL_BUFFER_BIT)

		// If the stencil fails keep the old value, otherwise write 1 to the stencil
		GLFunc.stencilOp = Vec3i(GL11C.GL_KEEP, GL11C.GL_REPLACE, GL11C.GL_REPLACE)
		GLFunc.stencilFRM = Vec3i(GL11C.GL_ALWAYS, 1, 255)

		// Write to the stencil
		shader.setUp(uniforms, renderer)
		mesh.bindAndDraw()

		// Stencil only passes if the stencil value is 1, and don't write to the stencil
		GLFunc.stencilFRM = Vec3i(GL11C.GL_EQUAL, 1, 255)
		GLFunc.stencilWriteMask = 0

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

		GL11C.glDisable(GL11C.GL_STENCIL_TEST)
	}
}