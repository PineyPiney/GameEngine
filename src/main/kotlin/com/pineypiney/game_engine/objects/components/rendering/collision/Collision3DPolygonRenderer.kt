package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider3DComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.ArrayMesh
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape3D
import com.pineypiney.game_engine.util.maths.shapes.Triangle3D
import com.pineypiney.game_engine.util.maths.shapes.TriangulatedSolid
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class Collision3DPolygonRenderer(parent: GameObject, val obj: GameObject, val width: Float = .05f, val colour: Vec4 = Vec4(1f), shader: Shader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	val mesh by lazy { createMesh(obj.getComponent<Collider3DComponent>()!!.shape) }

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setFloatUniform("width", ::width)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		shader.setUp(uniforms, renderer)
		GLFunc.lineWidth = width
		mesh.bindAndDraw(GL11C.GL_LINE_LOOP)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(mesh)

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))

		fun createMesh(shape: Shape3D): Mesh{
			when(shape){
				is Triangle3D -> return ArrayMesh(shape.points.flatMap { listOf(it.x, it.y, it.z) }.toFloatArray(), arrayOf(VertexAttribute.POSITION))
				is TriangulatedSolid -> return ArrayMesh(shape.triangles.flatMap { tri -> tri.points.flatMap { listOf(it.x, it.y, it.z) }}.toFloatArray(), arrayOf(VertexAttribute.POSITION))
			}
			return Mesh.centerSquareShape
		}

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(Collision3DPolygonRenderer(par, obj, lineThickness, colour))
			return par
		}
	}
}