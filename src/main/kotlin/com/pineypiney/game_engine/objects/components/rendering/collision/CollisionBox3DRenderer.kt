package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import glm_.vec4.Vec4

class CollisionBox3DRenderer(
	parent: GameObject,
	val obj: GameObject,
	val width: Float = .05f,
	val colour: Vec4 = Vec4(1f),
	shader: RenderShader = defaultShader
) : ShaderRenderedComponent(parent, shader) {

	val mesh = Mesh.centerCubeShape

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4Uniform("model") {
			(this@CollisionBox3DRenderer.obj.getShape() as Cuboid).run {
				(I.translate(
					center
				) * rotation.toMat4()).scale(sides)
			}
		}
		uniforms.setVec4Uniform("outlineColour", ::colour)
		uniforms.setFloatUniform("width", ::width)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		shader.draw("vertexBuffer", mesh, renderer)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	companion object {
		val defaultShader = ShaderLoader.loadRenderShader(ResourceKey("3d/outline_cube"))

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(CollisionBox3DRenderer(par, obj, lineThickness, colour))
			return par
		}
	}
}