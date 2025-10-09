package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.ArrayMesh
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.normal
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

open class Arrow2DRenderer(parent: GameObject, origin: Vec2, point: Vec2, width: Float, var colour: Vec4 = Vec4(1f)) : ShaderRenderedComponent(parent,
	ColourRendererComponent.defaultShader
) {

	constructor(parent: GameObject, colour: Vec3, origin: Vec2, point: Vec2, width: Float) : this(parent, origin, point, width, Vec4(colour, 1f))

	var origin: Vec2 = origin
		set(value) {
			field = value
			mesh = generateMesh()
		}
	var point: Vec2 = point
		set(value) {
			field = value
			mesh = generateMesh()
		}
	var width: Float = width
		set(value) {
			field = value
			mesh = generateMesh()
		}
	var mesh = generateMesh()

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		mesh.bindAndDraw()
	}

	fun setOriginAndPoint(origin: Vec2, point: Vec2){
		this.origin(origin)
		this.point(point)
		mesh = generateMesh()
	}

	fun generateMesh(): Mesh{
		val length = (point - origin).length()
		if(length == 0f) return Mesh.EmptyMesh
		val vec = (point - origin) / length

		val array: Array<Vec2> = if(length <= width * 2f){
			val perp = vec.normal() * (length * .5f)
			arrayOf(origin - perp, point, origin + perp)
		}
		else {
			val perp = vec.normal() * (width * .5f)
			val headBase = point - vec * (width * 2f)
			arrayOf(
				origin - perp, headBase - perp, headBase + perp,
				headBase + perp, origin + perp, origin - perp,
				headBase - (perp * 2), point, headBase + (perp * 2)
			)
		}
		return ArrayMesh(array.flatMap { it.array.toList() }.toFloatArray(), arrayOf(VertexAttribute.POSITION2D))
	}

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/2D"), ResourceKey("fragment/colour"))
		val shader3D = ShaderLoader.getShader(ResourceKey("vertex/3D"), ResourceKey("fragment/colour"))
		val menuShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/colour"))
	}
}