package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.meshes.TextMesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.vec2.Vec2
import glm_.vec4.Vec4

open class Text(
	var text: String,
	val colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
	val font: Font = Font.defaultFont,
	var italic: Float = 0f,
	var underlineThickness: Float = 0f,
	var underlineOffset: Float = -0.2f,
	var underlineAmount: Float = 1f,
	var alignment: Int = ALIGN_CENTER_LEFT
) : Initialisable {

	var lines = arrayOf<String>()
	var lengths = floatArrayOf()

	var mesh: TextMesh = TextMesh(emptyArray()); private set

	override fun init() {

	}

	fun updateLines(bounds: Vec2) {
		if(bounds.y == 0f) return

		lines = generateLines(bounds)
		lengths = lines.map { getWidth(it) }.toFloatArray()
		mesh.delete()
		mesh = font.getShape(lines.joinToString("\n"), false, bounds, alignment)
	}

	fun getWidth(): Float {
		return lines.maxOf { font.getWidth(it) }
	}

	fun getWidth(s: String): Float {
		// TODO("Might not need to split by \n because font does it for you")
		return s.split('\n').maxOf { font.getWidth(s) }
	}

	fun getWidth(range: IntRange): Float {
		return getWidth(text.substring(range))
	}

	fun getHeight(): Float {
		return getHeight(lines.joinToString("\n"))
	}

	fun getHeight(s: String): Float {
		return font.getHeight(s)
	}

	fun generateLines(bounds: Vec2): Array<String> {
		if(text.trim() == "") return emptyArray()
		val lines = mutableListOf<String>()
		var lastBreak = 0
		var lastWord = 0
		var i = 0

		while(i <= text.length){

			if(getHeight(lines.joinToString("\n")) > bounds.y){
				val line = try {
					lines.removeLast()
					lines.removeLast()
				}
				catch (e: Exception){
					""
				}
				var d = line.length
				while(d > 0){
					if(getWidth(line.substring(0, d) + "...") < bounds.x) break
					d--
				}
				lines.add(line.substring(0, d) + "...")
				break
			}
			if(i == text.length) break

			// Set values
			lastWord = i
			lastBreak = i
			i++

			// Get first word in this line
			while(i < text.length){
				if(" \n".contains(text[i])) break
				i++
				if(text[i - 1] == '-') break
			}

			val firstWordWidth = getWidth(text.substring(lastBreak, i))
			// If the first word in the line if too long then it has to be split up
			if(firstWordWidth > bounds.x){
				i--
				// Find the maximum amount of the word that can fit on one line,
				// add it and continue from the rest of the word for the next line
				while(i > lastBreak + 1){
					if(getWidth(text.substring(lastBreak, i)) < bounds.x) break
					i--
				}
				if(i == lastBreak) break // This isn't going anywhere, the text bounds are awful and not worth dealing with
				lines.add(text.substring(lastBreak, i))
				continue
			}

			// If at least one word can fit, then no other words will be truncated for this line
			lastWord = i
			word@while(i <= text.length && getWidth(text.substring(lastBreak, i)) <= bounds.x){
				lastWord = i
				// Reached the end of the string or forced new line
				if(i == text.length || text[i] == '\n') break

				i++
				// Search for the next character that can cause a line break
				while(i < text.length && !" \n".contains(text[i])){
					i++
					if(text[i - 1] == '-') break
				}
			}
			// Add this line to the collection, and set i to start at the end of this line
			lines.add(text.substring(lastBreak, lastWord))
			if(i < text.length - 1 && text[i] == '\n') lastWord++
			i = lastWord
		}
		lines.removeAll { it.replaceWhiteSpaces() == "" }
		return lines.toTypedArray()
	}

	fun getUnderlineOf(line: Int): Float {
		val underlineStart = lengths.copyOf(line).sum() / lengths.sum()
		val underlineEnd = underlineStart + (lengths[line] / lengths.sum())
		if (underlineAmount <= underlineStart) return 0f
		if (underlineAmount >= underlineEnd) return 1f
		return (underlineAmount - underlineStart) / (underlineEnd - underlineStart)
	}

	fun getAlignment(line: String, totalWidth: Float): Float {
		return when (alignment and 0xf) {
			ALIGN_CENTER_H -> (totalWidth - getWidth(line.trim())) * 0.5f
			ALIGN_RIGHT -> totalWidth - getWidth(line.trim())
			else -> 0f
		}
	}

	override fun delete() {
		mesh.delete()
	}

	override fun toString(): String {
		return "Text[\"$text\"]"
	}

	data class Params(var colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
					  var fontSize: Int = 12, var alignment: Int = ALIGN_CENTER_LEFT,
					  var shader: Shader = Font.fontShader, var font: Font = Font.defaultFont,
					  var italic: Float = 0f, var underlineThickness: Float = 0f,
					  var underlineOffset: Float = -0.2f, var underlineAmount: Float = 1f){

		fun withColour(c: Vec4) = this.apply { colour = c }
		fun withFontSize(c: Int) = this.apply { fontSize = c }
		fun withAlignment(c: Int) = this.apply { alignment = c }
		fun withShader(c: Shader) = this.apply { shader = c }
		fun withFont(c: Font) = this.apply { font = c }
		fun withItalic(c: Float) = this.apply { italic = c }
		fun withUnderlineThickness(c: Float) = this.apply { underlineThickness = c }
		fun withUnderlineOffset(c: Float) = this.apply { underlineOffset = c }
		fun withUnderlineAmount(c: Float) = this.apply { underlineAmount = c }
	}

	companion object {
		const val ALIGN_LEFT = 1
		const val ALIGN_CENTER_H = 2
		const val ALIGN_RIGHT = 4
		const val ALIGN_BOTTOM = 16
		const val ALIGN_CENTER_V = 32
		const val ALIGN_TOP = 64
		const val ALIGN_BOTTOM_LEFT = ALIGN_BOTTOM or ALIGN_LEFT
		const val ALIGN_BOTTOM_CENTER = ALIGN_BOTTOM or ALIGN_CENTER_H
		const val ALIGN_BOTTOM_RIGHT = ALIGN_BOTTOM or ALIGN_RIGHT
		const val ALIGN_CENTER_LEFT = ALIGN_CENTER_V or ALIGN_LEFT
		const val ALIGN_CENTER = ALIGN_CENTER_V or ALIGN_CENTER_H
		const val ALIGN_CENTER_RIGHT = ALIGN_CENTER_V or ALIGN_RIGHT
		const val ALIGN_TOP_LEFT = ALIGN_TOP or ALIGN_LEFT
		const val ALIGN_TOP_CENTER = ALIGN_TOP or ALIGN_CENTER_H
		const val ALIGN_TOP_RIGHT = ALIGN_TOP or ALIGN_RIGHT

		fun makeMenuText(
			text: String,
			colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
			fontSize: Int = 12,
			alignment: Int = ALIGN_CENTER_LEFT,
			shader: Shader = Font.fontShader,
			font: Font = Font.defaultFont,
			italic: Float = 0f,
			underlineThickness: Float = 0f,
			underlineOffset: Float = -0.2f,
			underlineAmount: Float = 1f,
		): MenuItem {
			return object : MenuItem("$text Text Object") {

				init {
					components.add(
						TextRendererComponent(this, Text(text, colour, font, italic,
							underlineThickness, underlineOffset, underlineAmount, alignment), fontSize, shader)
					)
				}
			}
		}

		fun makeMenuText(text: String, params: Params): MenuItem{
			return object : MenuItem("$text Text Object") {

				init{
					components.add(
						TextRendererComponent(this, Text(text, params.colour, params.font, params.italic, params.underlineThickness,
							params.underlineOffset, params.underlineAmount, params.alignment),
							params.fontSize, params.shader)
					)
				}
			}
		}

		fun makeGameText(
			text: String,
			colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
			fontSize: Int = 12,
			alignment: Int = ALIGN_CENTER_LEFT,
			shader: Shader = TextRendererComponent.gameTextShader,
			font: Font = Font.defaultFont,
			italic: Float = 0f,
			underlineThickness: Float = 0f,
			underlineOffset: Float = -0.2f,
			underlineAmount: Float = 1f,
		): GameObject {
			return object : GameObject() {

				init {
					components.add(
						TextRendererComponent(
							this,
							Text(
								text,
								colour,
								font,
								italic,
								underlineThickness,
								underlineOffset,
								underlineAmount,
								alignment
							),
							fontSize, shader
						)
					)
				}
			}
		}
	}
}