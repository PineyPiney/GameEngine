package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.extension_functions.maxOf
import com.pineypiney.game_engine.util.extension_functions.minOf
import com.pineypiney.game_engine.util.maths.shapes.*
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

interface RenderedComponentI : ComponentI {

	var visible: Boolean

	fun getMeshes(): Collection<Mesh>
	fun render(renderer: RendererI, tickDelta: Double)

	fun getMeshShape(): Shape<*> {
		val meshes = getMeshes().map(Mesh::getBounds)

		val twoD = meshes.all { (min, max) -> min.z == 0f && max.z == 0f }

		if(twoD){
			val rects = meshes.map { (min, max) ->
				Rect2D(Vec2(min), Vec2(max - min))
			}
			return if(rects.size == 1) rects.first()
			else CompoundShape2D(rects.toMutableSet())
		}
		else {
			val cuboids = meshes.map { (min, max) ->
				AxisAlignedCuboid(min + (max * 5f), max - min)
			}
			return if (cuboids.size == 1) cuboids.first()
			else CompoundShape3D(cuboids.toMutableSet())
		}
	}

	fun getMeshBounds(transform: Mat4 = Mat4.identity): Pair<Vec3, Vec3>{
		var min = Vec3(Float.MAX_VALUE)
		var max = Vec3(-Float.MAX_VALUE)
		for(mesh in getMeshes()){
			val meshMinMesh = mesh.getBounds(transform)
			min = minOf(min, meshMinMesh.first)
			max = maxOf(max, meshMinMesh.second)
		}
		return min to max
	}

	fun getScreenSize(transform: Mat4 = Mat4.identity): Vec2{
		var min = Vec2(Float.MAX_VALUE)
		var max = Vec2(-Float.MAX_VALUE)
		for(mesh in getMeshes()){
			val meshMinMesh = mesh.getBounds(transform)
			min = minOf(min, Vec2(meshMinMesh.first))
			max = maxOf(max, Vec2(meshMinMesh.second))
		}
		return max - min
	}
}