package com.pineypiney.game_engine.objects.components.scrollList

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

abstract class ScrollListEntryComponent(parent: GameObject) : Component(parent, "SLE") {

	open val list: ScrollListComponent get() = parent.parent?.getComponent<ScrollListComponent>()!!

	var limits = Vec2(0f); protected set

	override fun init() {
		super.init()
		parent.scale = Vec3((1f - list.scrollerWidth), list.entryHeight, 1f)
	}

	companion object {
		val entryColourShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/scroll_entry_colour")]
		val entryTextShader =
			ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/scroll_entry_text"))

		fun makeScrollerText(
			text: String,
			colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
			maxWidth: Float = 1f,
			maxHeight: Float = 1f,
			font: Font = Font.defaultFont,
			italic: Float = 0f,
			underlineThickness: Float = 0f,
			underlineOffset: Float = -0.2f,
			underlineAmount: Float = 1f,
			fontSize: Float = 1f,
			alignment: Int = Text.ALIGN_CENTER_LEFT,
			shader: Shader = entryTextShader
		): GameObject {
			return object : MenuItem("Scroller Text $text") {
				override fun addComponents() {
					super.addComponents()
					val x = this
					components.add(object : TextRendererComponent(
						x,
						Text(
							text,
							colour,
							maxWidth,
							maxHeight,
							font,
							italic,
							underlineThickness,
							underlineOffset,
							underlineAmount,
							fontSize,
							alignment
						),
						shader
					) {
						override fun setUniforms() {
							super.setUniforms()
							uniforms.setVec2Uniform(
								"limits",
								parent.parent!!.parent!!.getComponent<ScrollListComponent>()!!::limits
							)
						}
					})
				}
			}
		}
	}
}