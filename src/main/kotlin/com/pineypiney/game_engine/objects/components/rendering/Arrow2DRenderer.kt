package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.resources.shaders.parameters.VertexBufferLayout
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.extension_functions.put
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.BufferUtils

open class Arrow2DRenderer(parent: GameObject, origin: Vec2, point: Vec2, width: Float, var colour: Vec4 = Vec4(1f)) : ShaderRenderedComponent(parent, shader) {

	constructor(parent: GameObject, colour: Vec3, origin: Vec2, point: Vec2, width: Float) : this(parent, origin, point, width, Vec4(colour, 1f))

	var origin: Vec2 = origin
		set(value) {
			field = value
			regenerateMesh()
		}
	var point: Vec2 = point
		set(value) {
			field = value
			regenerateMesh()
		}
	var width: Float = width
		set(value) {
			field = value
			regenerateMesh()
		}
	var mesh = generateMesh()

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		shader.draw(mesh, renderer)
	}

	fun setOriginAndPoint(origin: Vec2, point: Vec2){
		this.origin(origin)
		this.point(point)
		regenerateMesh()
	}

	fun regenerateMesh(){
		mesh.delete()
		mesh = generateMesh()
	}

	fun generateMesh(): Mesh {
		val length = (point - origin).length()
		if(length == 0f) return Mesh.EmptyMesh
		val vec = (point - origin) / length

		val positions: Array<Vec2> = if (length <= width * 2f) {
			val perp = vec.normal() * (length * .5f)
			arrayOf(origin + perp, point, origin - perp)
		}
		else {
			val perp = vec.normal() * (width * .5f)
			val headBase = point - vec * (width * 2f)
			arrayOf(
				origin - perp, headBase + perp, headBase - perp,
				headBase + perp, origin - perp, origin + perp,
				headBase + (perp * 2), point, headBase - (perp * 2)
			)
		}
		val buffer = BufferUtils.createByteBuffer(positions.size * 8)
		for (pos in positions) buffer.put(pos)
		return ResourceFactory.INSTANCE.createArrayMesh("Arrow Mesh", listOf(buffer.flip() to mapOf(VertexAttribute.POSITION2D to 0L)))
	}

	override fun delete() {
		mesh.delete()
	}

	companion object {
		val shader = ColourRendererComponent.shader2D.withParameters(
			ResourceKey("2D Arrow Shader"), ColourRendererComponent.shader2D.parameters.copy(
				vertexLayout = VertexBufferLayout(
					listOf(
						VertexBufferLayout.Binding(
							0, listOf(
								VertexBufferLayout.Attribute(0, TextureFormat.RG32F, 0),
								VertexBufferLayout.Attribute(1, TextureFormat.RG32F, 0)
							), 8
						)
					)
				)
			)
		)
	}
}