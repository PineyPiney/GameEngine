package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.IndexedMeshBuilder
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.parameters.CullMode
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape3D
import com.pineypiney.game_engine.util.maths.shapes.Triangle3D
import com.pineypiney.game_engine.util.maths.shapes.TriangulatedSolid
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Collision3DPolygonRenderer(parent: GameObject, val obj: GameObject, val width: Float = .05f, val colour: Vec4 = Vec4(1f), shader: RenderShader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	val mesh by lazy { createMesh(obj.name, obj.getComponent<Collider3DComponent>()!!.shape, width) }

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setFloatUniform("width", ::width)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		shader.draw("vertexBuffer", mesh, renderer)
//		mesh.bindAndDraw(renderer.getRenderingApi(), GL11C.GL_LINE_LOOP)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	companion object {
		val defaultShader = ColourRendererComponent.shader3D.withParameters(ResourceKey("3d_polygon"), RenderShaderParameters(cullMode = CullMode.NONE))

		fun createMesh(name: String, shape: Shape3D, width: Float): Mesh {
			val builder = IndexedMeshBuilder(VertexAttribute.POSITION)
			when(shape){
				is Triangle3D -> addPolygon(builder, shape.points, width)
				is TriangulatedSolid -> for (triangle in shape.triangles) addPolygon(builder, triangle.points, width)
			}
			return builder.build("$name Wireframe")
		}

		fun addPolygon(builder: IndexedMeshBuilder, points: Array<Vec3>, width: Float) {
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

				val m1 = v2.cross(o1)
				val m2 = v2.cross(o2)

				builder.startQuad()
					.vertex(p2 + o1 * width / m1.length())
					.vertex(p2)
					.vertex(p3)
					.vertex(p3 + o2 * width / m2.length())
			}
		}

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(Collision3DPolygonRenderer(par, obj, lineThickness, colour))
			return par
		}
	}
}