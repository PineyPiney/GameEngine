package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.IndexedMeshBuilder
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.parameters.CullMode
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.maths.shapes.*
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class CollisionPolygonRenderer(parent: GameObject, var obj: GameObject?, var width: Float = .02f, val pointSize: Float = .05f, val colour: Vec4 = Vec4(1f), shader: RenderShader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	private var lastShape: Shape2D? = null
	private var lastWidth = width
	private var mesh: Mesh? = null
	private var pointMesh: Mesh? = null

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4Uniform("model", ::Mat4)
		uniforms.setVec4Uniform("colour", ::colour)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		val shape = obj?.getComponent<Collider2DComponent>()?.transformedShape
		if (shape != lastShape || width != lastWidth) {
			lastShape = shape
			lastWidth = width
			mesh?.delete()
			pointMesh?.delete()
			val points = getPoints(shape)
			if (points == null) {
				mesh = null
				pointMesh = null
			} else {
				val name = obj?.name ?: "Null"
				mesh = createMesh(name, points.toList(), width)
				pointMesh =
					if (shape is Circle) createPointMesh(name, listOf(shape.center), pointSize)
					else createPointMesh(name, points, pointSize)
			}
		}
		mesh?.let {
			shader.setUp(uniforms, renderer)
			shader.draw("vertexBuffer", it, renderer)
//			it.bindAndDraw(renderer.getRenderingApi(), GL11C.GL_LINE_LOOP)
		}
		pointMesh?.let { shader.draw("vertexBuffer", it, renderer) }
	}

	override fun delete() {
		super.delete()
		mesh?.delete()
		pointMesh?.delete()
	}

	override fun getMeshes(): Collection<Mesh> = mesh?.let { listOf(it) } ?: emptyList()

	companion object {
		val defaultShader = ShaderLoader[ResourceKey("vertex/pass_pos"), ResourceKey("fragment/colour"), RenderShaderParameters().cullMode(CullMode.NONE)]

		fun getPoints(shape: Shape2D?): Iterable<Vec2>? {
			return when(shape){
				is Rect2D -> shape.points
				is Parallelogram -> shape.points
				is Polygon -> shape.vertices
				is Circle -> List(32) { shape.center + Vec2.fromAngle(it * .0625f * PIF, shape.radius) }
				else -> null
			}
		}

		fun createMesh(name: String, points: List<Vec2>, width: Float): Mesh {
			val builder = IndexedMeshBuilder(VertexAttribute.POSITION2D)
			val numPoints = points.size
			for (i in points.indices) {
				val p1 = points[i]
				val p2 = points[(i + 1) % numPoints]
				val p3 = points[(i + 2) % numPoints]
				val p4 = points[(i + 3) % numPoints]

				val v1 = (p2 - p1).normalizeAssign()
				val v2 = (p3 - p2).normalizeAssign()
				val v3 = (p4 - p3).normalizeAssign()

				val o1 = (v2 - v1).normalizeAssign()
				val o2 = (v3 - v2).normalizeAssign()

				val m1 = o1.cross(v2)
				val m2 = o2.cross(v2)

				builder.startQuad()
					.vertex(p2 - o1 * width / m1)
					.vertex(p2 + o1 * width / m1)
					.vertex(p3 + o2 * width / m2)
					.vertex(p3 - o2 * width / m2)
			}
			return builder.build("$name Wireframe")
		}

		fun createPointMesh(name: String, points: Iterable<Vec2>, pointWidth: Float): Mesh {
			val builder = IndexedMeshBuilder(VertexAttribute.POSITION2D)
			for (point in points) {
				builder.startQuad()
					.vertex(point.x - pointWidth, point.y - pointWidth)
					.vertex(point.x + pointWidth, point.y - pointWidth)
					.vertex(point.x + pointWidth, point.y + pointWidth)
					.vertex(point.x - pointWidth, point.y + pointWidth)
			}
			return builder.build("$name Points")
		}

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(CollisionPolygonRenderer(par, obj, lineThickness, .05f, colour))
			return par
		}
	}
}