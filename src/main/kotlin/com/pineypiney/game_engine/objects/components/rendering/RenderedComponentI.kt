package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.components.ComponentI
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.util.maths.shapes.*
import glm_.vec2.Vec2

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
}