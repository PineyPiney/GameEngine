package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ChildContainingRenderer
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3

class StencilTest(gameEngine: WindowedGameEngineI<*>, renderer: WindowRendererI<TestScene>) : TestScene(gameEngine, renderer) {

	val stencilWriter = GameObject("Stencil Writer", 1)

	override fun addObjects() {
		stencilWriter.components.add(ChildContainingRenderer(stencilWriter, Mesh.cornerSquareShape, Vec3(0f, 1f, 0f), ColourRendererComponent.shader2D))
		add(stencilWriter.relative(Vec3(-5f, -5f, .01f), Vec2(5f)))
	}

	override fun onPress(key: Char) {
		when (key) {
			'S' -> {
				window.pos = Vec2i(1440, 540)
				window.size = Vec2i(2)
			}
		}
	}
}