package com.pineypiney.game_engine.objects.text

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.util.shapes.TextMesh
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.text.Font
import com.pineypiney.game_engine.util.extension_functions.replaceWhiteSpaces
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import kotlin.math.min

open class Text(
	text: String,
	val colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
	var maxWidth: Float = 2f,
	var maxHeight: Float = 2f,
	val font: Font = Font.defaultFont,
	var italic: Float = 0f,
	var underlineThickness: Float = 0f,
	var underlineOffset: Float = -0.2f,
	var underlineAmount: Float = 1f,
	var fontSize: Float = 1f,
	var alignment: Int = ALIGN_CENTER_LEFT
) : Initialisable {

	var lines = arrayOf<String>()
	var lengths = floatArrayOf()
	var size: Float = 1f

	var mesh: TextMesh = TextMesh(emptyArray())

	// Initialise as true so the text is generated before it's first render call
	var textChanged = true
	var text: String = text
		set(value) {
			if(field != value) {
				field = value
				textChanged = true
			}
		}

	override fun init() {

	}

	fun updateLines(parentAspect: Float) {
		if (fontSize > 0f) size = fontSize
		else fitWithin(Vec2(maxWidth * parentAspect, maxHeight))

		lines = generateLines(parentAspect)
		lengths = lines.map { getWidth(it) }.toFloatArray()
		mesh.delete()
		mesh = font.getShape(lines.joinToString("\n"), false, alignment)
		textChanged = false
	}

	fun getWidth(): Float {
		return lines.maxOf { font.getWidth(it) } * size
	}

	fun getWidth(s: String): Float {
		return s.split('\n').maxOf { font.getWidth(s) } * size
	}

	fun getWidth(range: IntRange): Float {
		return getWidth(text.substring(range))
	}

	fun getHeight(): Float {
		return getHeight(lines.joinToString("\n"))
	}

	fun getHeight(s: String): Float {
		return font.getHeight(s) * size
	}

	fun fitWithin(bounds: Vec2) {
		val fSize = font.getSize(text)
		val fits = bounds / fSize
		size = min(fits.x, fits.y)
	}

	fun generateLines(parentAspect: Float): Array<String> {
		val maxWidth = this.maxWidth * parentAspect
		val lines = mutableListOf<String>()
		var lastBreak = 0
		var lastWord = 0
		var i = 0

		while(i <= text.length){

			if(getHeight(lines.joinToString("\n")) > maxHeight){
				lines.removeLast()
				val line = lines.removeLast()
				var d = line.length
				while(d > 0){
					if(getWidth(line.substring(0, d) + "...") < maxWidth) break
					d--
				}
				lines.add(line.substring(0, d) + "...")
				break
			}
			if(i == text.length) break

			// Set values
			lastWord = i
			lastBreak = i

			// Get first word in this line
			while(i < text.length){
				if(" \n".contains(text[i])) break
				i++
				if(text[i - 1] == '-') break
			}

			val firstWordWidth = getWidth(text.substring(lastBreak, i))
			// If the first word in the line if too lang then it has to be cut in half
			if(firstWordWidth > maxWidth){
				i--
				// Find the maximum amount of the word that can fit on one line,
				// add it and continue from the rest of the word for the next line
				while(i > lastBreak + 1){
					if(getWidth(text.substring(lastBreak, i)) < maxWidth) break
					i--
				}
				lines.add(text.substring(lastBreak, i))
				continue
			}

			// If at least one word can fit, then no other words will be truncated for this line
			lastWord = i
			word@while(i <= text.length && getWidth(text.substring(lastBreak, i)) < maxWidth){
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
			i = lastWord
			lines.add(text.substring(lastBreak, lastWord))
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

	data class Params(var colour: Vec4 = Vec4(0f, 0f, 0f, 1f), var maxWidth: Float = 1f, var maxHeight: Float = 1f,
					  var fontSize: Float = 1f, var alignment: Int = ALIGN_CENTER_LEFT,
					  var shader: Shader = Font.fontShader, var font: Font = Font.defaultFont,
					  var italic: Float = 0f, var underlineThickness: Float = 0f,
					  var underlineOffset: Float = -0.2f, var underlineAmount: Float = 1f){

		fun withColour(c: Vec4) = this.apply { colour = c }
		fun withMaxWidth(c: Float) = this.apply { maxWidth = c }
		fun withMaxHeight(c: Float) = this.apply { maxHeight = c }
		fun withFontSize(c: Float) = this.apply { fontSize = c }
		fun withAlignment(c: Int) = this.apply { alignment = c }
		fun withShader(c: Shader) = this.apply { shader = c }
		fun withFont(c: Font) = this.apply { font = c }
		fun withItalic(c: Float) = this.apply { italic = c }
		fun withUnderlineThickness(c: Float) = this.apply { underlineThickness = c }
		fun withUnderlineOffset(c: Float) = this.apply { underlineOffset = c }
		fun withUnderlineAmount(c: Float) = this.apply { underlineAmount = c }
	}

	companion object {
		const val ALIGN_CENTER_H = 1
		const val ALIGN_LEFT = 2
		const val ALIGN_RIGHT = 4
		const val ALIGN_CENTER_V = 16
		const val ALIGN_TOP = 32
		const val ALIGN_BOTTOM = 64
		const val ALIGN_CENTER = ALIGN_CENTER_H or ALIGN_CENTER_V
		const val ALIGN_CENTER_LEFT = ALIGN_CENTER_V or ALIGN_LEFT
		const val ALIGN_CENTER_RIGHT = ALIGN_CENTER_V or ALIGN_RIGHT
		const val ALIGN_TOP_CENTER = ALIGN_TOP or ALIGN_CENTER_H
		const val ALIGN_BOTTOM_CENTER = ALIGN_BOTTOM or ALIGN_CENTER_H
		const val ALIGN_BOTTOM_LEFT = ALIGN_BOTTOM or ALIGN_LEFT
		const val ALIGN_BOTTOM_RIGHT = ALIGN_BOTTOM or ALIGN_RIGHT
		const val ALIGN_TOP_LEFT = ALIGN_TOP or ALIGN_LEFT
		const val ALIGN_TOP_RIGHT = ALIGN_TOP or ALIGN_RIGHT

		fun makeMenuText(
			text: String,
			colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
			maxWidth: Float = 1f,
			maxHeight: Float = 1f,
			fontSize: Float = 1f,
			alignment: Int = ALIGN_CENTER_LEFT,
			shader: Shader = Font.fontShader,
			font: Font = Font.defaultFont,
			italic: Float = 0f,
			underlineThickness: Float = 0f,
			underlineOffset: Float = -0.2f,
			underlineAmount: Float = 1f,
		): MenuItem {
			return object : MenuItem("$text Text Object") {

				override fun addComponents() {
					super.addComponents()
					components.add(
						TextRendererComponent(this, Text(text, colour, maxWidth, maxHeight, font, italic,
							underlineThickness, underlineOffset, underlineAmount, fontSize, alignment), shader)
					)
				}
			}
		}

		fun makeMenuText(text: String, params: Params): MenuItem{
			return object : MenuItem("$text Text Object") {

				override fun addComponents() {
					super.addComponents()
					components.add(
						TextRendererComponent(this, Text(text, params.colour, params.maxWidth,
							params.maxHeight, params.font, params.italic, params.underlineThickness,
							params.underlineOffset, params.underlineAmount, params.fontSize, params.alignment),
							params.shader)
					)
				}
			}
		}

		fun makeGameText(
			text: String,
			colour: Vec4 = Vec4(0f, 0f, 0f, 1f),
			maxWidth: Float = 2f,
			maxHeight: Float = 2f,
			fontSize: Float = 1f,
			alignment: Int = ALIGN_CENTER_LEFT,
			shader: Shader = TextRendererComponent.gameTextShader,
			font: Font = Font.defaultFont,
			italic: Float = 0f,
			underlineThickness: Float = 0f,
			underlineOffset: Float = -0.2f,
			underlineAmount: Float = 1f,
		): GameObject {
			return object : GameObject() {

				override fun addComponents() {
					super.addComponents()
					components.add(
						TextRendererComponent(
							this,
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
						)
					)
				}
			}
		}
	}
}