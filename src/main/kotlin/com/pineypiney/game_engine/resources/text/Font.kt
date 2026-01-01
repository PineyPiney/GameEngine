package com.pineypiney.game_engine.resources.text

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.meshes.TextMesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2

abstract class Font {

	abstract val name: String
	abstract val shader: RenderShader
	abstract val lineSpacing: Float

	abstract fun getCharWidth(char: Char): Float
	abstract fun getCharHeight(char: Char): Vec2

	abstract fun getWidth(text: String): Float
	abstract fun getHeight(text: String): Float
	fun getSize(text: String): Vec2 = Vec2(getWidth(text), getHeight(text))

	abstract fun getShape(text: String, bold: Boolean, bounds: Vec2, alignment: Int): TextMesh

	fun getAlignmentOffset(text: String, bounds: Vec2, alignment: Int): Pair<FloatArray, Float>{

		val alignX = alignment and 7
		val alignY = alignment and 0x70

		val lines = text.split('\n')
		val offsetX = FloatArray(lines.size){
			val width = getWidth(lines[it])
			when(alignX){
				Text.ALIGN_RIGHT -> bounds.x - width
				Text.ALIGN_CENTER_H -> (bounds.x - width) * .5f
				else -> 0f
			}
		}

		val height = getHeight(text)
		val offsetY = when(alignY){
			Text.ALIGN_TOP -> bounds.y - height
			Text.ALIGN_CENTER_V -> (bounds.y - height) * .5f
			else -> 0f
		} + (lines.size - 1) * lineSpacing

		return offsetX to offsetY
	}

	companion object {
		val fontShader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/text")]
		val defaultFont: Font; get() = FontLoader[ResourceKey(GameEngineI.defaultFont)]
	}
}