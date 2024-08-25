package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class CollisionBox3DRenderer(
	val obj: GameObject,
	val lineThickness: Float = .05f,
	val colour: Vec4 = Vec4(1f),
	val shader: Shader = defaultShader
) : GameObject() {

	override fun addComponents() {
		super.addComponents()
		components.add(object : ShaderRenderedComponent(this@CollisionBox3DRenderer, shader) {

			val vShape = Mesh.centerCubeShape
			override val shape: Shape<*> = vShape.shape
			override val renderSize: Vec2 = Vec2(1f)

			override fun setUniforms() {
				super.setUniforms()
				uniforms.setMat4Uniform("model") {
					(this@CollisionBox3DRenderer.obj.getShape() as Cuboid).run {
						(I.translate(
							center
						) * rotation.toMat4()).scale(size)
					}
				}
				uniforms.setVec4Uniform("colour", ::colour)
				uniforms.setFloatUniform("thickness", ::lineThickness)
			}

			override fun render(renderer: RendererI, tickDelta: Double) {
				shader.setUp(uniforms, renderer)
				vShape.bindAndDraw()
			}
		})
	}

	companion object {
		val defaultShader =
			ShaderLoader.getShader(ResourceKey("vertex/pass_pos_3D"), ResourceKey("fragment/collider3D"))
	}
}