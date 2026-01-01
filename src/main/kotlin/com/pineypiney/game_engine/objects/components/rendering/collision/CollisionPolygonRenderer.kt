package com.pineypiney.game_engine.objects.components.rendering.collision

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.ArrayMesh
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.rendering.meshes.VertexAttribute
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Parallelogram
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C

class CollisionPolygonRenderer(parent: GameObject, var obj: GameObject?, val width: Float = .05f, val colour: Vec4 = Vec4(1f), shader: RenderShader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	private var lastShape = obj?.getComponent<Collider2DComponent>()?.shape
	private var mesh  = createMesh(lastShape)

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setVec4Uniform("colour", ::colour)
		uniforms.setFloatUniform("width", ::width)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		val shape = obj?.getComponent<Collider2DComponent>()?.shape
		if(shape != lastShape){
			lastShape = shape
			mesh = createMesh(shape)
		}
		mesh?.let {
			shader.setUp(uniforms, renderer)
			it.bindAndDraw(GL11C.GL_LINE_LOOP)
		}
	}

	override fun getMeshes(): Collection<Mesh> = mesh?.let { listOf(it) } ?: emptyList()

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/collider"))

		fun createMesh(shape: Shape2D?): Mesh? {
			return when(shape){
				is Rect2D -> ArrayMesh(shape.points.flatMap { listOf(it.x, it.y) }.toFloatArray(), arrayOf(VertexAttribute.POSITION2D))
				is Parallelogram -> ArrayMesh(shape.points.flatMap { listOf(it.x, it.y) }.toFloatArray(), arrayOf(VertexAttribute.POSITION2D))
				else -> null
			}
		}

		fun create(obj: GameObject, lineThickness: Float = .05f, colour: Vec4 = Vec4(1f)): GameObject{
			val par = GameObject(obj.name + " Collider Renderer")
			par.components.add(CollisionPolygonRenderer(par, obj, lineThickness, colour))
			return par
		}
	}
}