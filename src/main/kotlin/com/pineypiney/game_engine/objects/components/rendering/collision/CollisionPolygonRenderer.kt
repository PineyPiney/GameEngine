package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.ArrayMesh
import com.pineypiney.game_engine.rendering.meshes.IndicesMeshBuilder
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.PIF
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.maths.shapes.*
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class CollisionPolygonRenderer(parent: GameObject, var obj: GameObject?, val width: Float = .05f, val pointSize: Float = .05f, val colour: Vec4 = Vec4(1f), shader: RenderShader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	private var lastShape: Shape2D? = null
	private var mesh: Mesh? = null
	private var pointMesh: Mesh? = null

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4Uniform("model", ::Mat4)
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setFloatUniform("width", ::width)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		val shape = obj?.getComponent<Collider2DComponent>()?.transformedShape
		if(shape != lastShape){
			lastShape = shape
			mesh?.delete()
			pointMesh?.delete()
			val points = getPoints(shape)
			if (points == null) {
				mesh = null
				pointMesh = null
			} else {
				mesh = createMesh(points)
				pointMesh =
					if (shape is Circle) createPointMesh(listOf(shape.center), pointSize)
					else createPointMesh(points, pointSize)
			}
		}
		mesh?.let {
			shader.setUp(uniforms, renderer)
			it.bindAndDraw(GL11C.GL_LINE_LOOP)
		}
		pointMesh?.bindAndDraw()
	}

	override fun delete() {
		super.delete()
		mesh?.delete()
		pointMesh?.delete()
	}

	override fun getMeshes(): Collection<Mesh> = mesh?.let { listOf(it) } ?: emptyList()

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/colour"))

		fun getPoints(shape: Shape2D?): Iterable<Vec2>? {
			return when(shape){
				is Rect2D -> shape.points
				is Parallelogram -> shape.points
				is Polygon -> shape.vertices
				is Circle -> List(32) { shape.center + Vec2.fromAngle(it * .0625f * PIF, shape.radius) }
				else -> null
			}
		}

		fun createMesh(points: Iterable<Vec2>): Mesh {
			return ArrayMesh(points.flatMap { listOf(it.x, it.y) }.toFloatArray(), arrayOf(VertexAttribute.POSITION2D))
		}

		fun createPointMesh(points: Iterable<Vec2>, pointWidth: Float): Mesh {
			val builder = IndicesMeshBuilder(VertexAttribute.POSITION2D)
			for (point in points) {
				builder.startQuad()
					.vertex(point.x - pointWidth, point.y - pointWidth)
					.vertex(point.x + pointWidth, point.y - pointWidth)
					.vertex(point.x + pointWidth, point.y + pointWidth)
					.vertex(point.x - pointWidth, point.y + pointWidth)
			}
			return builder.build()
		}

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(CollisionPolygonRenderer(par, obj, lineThickness, .05f, colour))
			return par
		}
	}
}