package com.pineypiney.game_engine.objects.util.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.rotate
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class CollisionBox2DRenderer(parent: GameObject, val obj: GameObject, val width: Float = .05f, val colour: Vec4 = Vec4(1f), shader: Shader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	val mesh = Mesh.cornerSquareShape

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4Uniform("model") {
			(this@CollisionBox2DRenderer.obj.getShape() as Rect2D).run {
				I.translate(
					Vec3(origin, 0f)
				).rotate(angle).scale(Vec3(lengths, 1f))
			}
		}
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setFloatUniform("width", ::width)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		mesh.bindAndDraw()
	}

	override fun getScreenShape(): Shape<*> {
		return mesh.shape
	}

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(CollisionBox2DRenderer(par, obj, lineThickness, colour))
			return par
		}
	}
}