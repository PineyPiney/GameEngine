package com.pineypiney.game_engine.apps.editor.util.transformers

import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.ColouredSpriteComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.PIF
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Transformers(val creator: (base: GameObject, EditorScreen) -> Unit) {

	companion object {
		val TRANSLATE2D = Transformers{ o, s ->
			val head = Translator2D(o, s)
			o.components.add(head)

			val ip = 1f / 320f

			val xArrow = MenuItem("X Arrow")
			xArrow.translate(Vec3(-2 * ip, 0f, .02f))
			xArrow.components.add(ColouredSpriteComponent(xArrow, Sprite(TextureLoader[ResourceKey("editor/arrow")], 64f, Vec2(0f, .5f)), head.red))

			val yArrow = MenuItem("Y Arrow")
			yArrow.translate(Vec3(0f, -2 * ip, .01f))
			yArrow.rotate(Vec3(0f, 0f, PIF * .5f))
			yArrow.components.add(ColouredSpriteComponent(yArrow, Sprite(TextureLoader[ResourceKey("editor/arrow")], 64f, Vec2(0f, .5f)), head.green))

			val box = MenuItem("Box")
			box.components.add(ColouredSpriteComponent(box, Sprite(TextureLoader[ResourceKey("editor/box")], 32f, Vec2(0f)), head.blue))

			o.addChild(xArrow, yArrow, box)
		}

		val ROTATE2D = Transformers{ o, s ->
			val head = Rotate2D(o, s)
			o.components.add(head)

			val rings = MenuItem("Rotator Rings")
			rings.scale = Vec3(1.2f, 1.2f, 1f)
			rings.components.add(object : ShaderRenderedComponent(rings, ShaderLoader[ResourceKey("vertex/2D_pass_pos"), ResourceKey("fragment/editor/rotate2D")]){

				override fun setUniforms() {
					super.setUniforms()
					uniforms.setVec4Uniform("xColour", head::red)
					uniforms.setVec4Uniform("yColour", head::green)
					uniforms.setVec4Uniform("zColour", head::blue)
				}

				override fun render(renderer: RendererI, tickDelta: Double) {
					shader.setUp(uniforms, renderer)
					Mesh.centerSquareShape.bindAndDraw()
				}

				override fun getMeshes(): Collection<Mesh> = listOf(Mesh.centerSquareShape)
			})

			o.addChild(rings)
		}
		val SCALE2D = Transformers{ o, s ->
			val head = Scale2D(o, s)
			o.components.add(head)

			val ip = 1f / 320f

			val xArrow = MenuItem("X Arrow")
			xArrow.translate(Vec3(-2 * ip, 0f, .02f))
			xArrow.components.add(ColouredSpriteComponent(xArrow, Sprite(TextureLoader[ResourceKey("editor/arrow")], 64f, Vec2(0f, .5f)), head.red))

			val yArrow = MenuItem("Y Arrow")
			yArrow.translate(Vec3(0f, -2 * ip, .01f))
			yArrow.rotate(Vec3(0f, 0f, PIF * .5f))
			yArrow.components.add(ColouredSpriteComponent(yArrow, Sprite(TextureLoader[ResourceKey("editor/arrow")], 64f, Vec2(0f, .5f)), head.green))

			val box = MenuItem("Box")
			box.position = Vec3(.5f, .5f, 0f)
			box.components.add(ColouredSpriteComponent(box, Sprite(TextureLoader[ResourceKey("editor/box")], 32f, Vec2(0f)), head.blue))

			o.addChild(xArrow, yArrow, box)
		}
	}
}