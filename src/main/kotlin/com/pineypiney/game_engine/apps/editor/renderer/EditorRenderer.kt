package com.pineypiney.game_engine.apps.editor.renderer

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.apps.editor.util.EditorSettings
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.ResourceFactory
import com.pineypiney.game_engine.util.Colour
import glm_.vec2.Vec2

interface EditorRenderer : WindowRendererI<EditorScreen> {

	val settings: EditorSettings
	val sort: GameObject.() -> Float
	val depth: Boolean

	var backgroundColour: Colour

	fun createSceneBufferMesh(): Mesh {
		return Mesh.textureQuad(
			ResourceFactory.INSTANCE, "Scene Buffer",
			Vec2((settings.objectBrowserWidth * 2f / viewportSize.x) - 1f, (settings.fileBrowserHeight * 2f / viewportSize.y) - 1f),
			Vec2(1f - (settings.componentBrowserWidth * 2f / viewportSize.x), 1f)
		)
	}
}