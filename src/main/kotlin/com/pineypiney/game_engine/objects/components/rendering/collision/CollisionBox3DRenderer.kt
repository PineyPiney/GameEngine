package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec4.Vec4

class CollisionBox3DRenderer(
	parent: GameObject,
	val obj: GameObject,
	val lineThickness: Float = .05f,
	val colour: Vec4 = Vec4(1f),
	shader: Shader = defaultShader
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
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setFloatUniform("thickness", ::lineThickness)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		mesh.bindAndDraw()
	}

	override fun getScreenShape(): Shape<*> {
		return mesh.shape
	}

	companion object {
		val defaultShader =
			ShaderLoader.getShader(ResourceKey("vertex/pass_pos_3D"), ResourceKey("fragment/collider3D"))

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(CollisionBox3DRenderer(par, obj, lineThickness, colour))
			return par
		}
	}
}