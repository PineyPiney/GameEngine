package com.pineypiney.game_engine.resources.shaders

data class GLSLCodeSegment(val code: String, val bracketContents: MutableList<GLSLCodeSegment> = mutableListOf(), val comment: String = "") {

	override fun toString(): String {
		val builder = StringBuilder()
		if (comment.isNotEmpty()) {
			if (comment.contains("\n")) builder.append("/*$comment*/\n")
			else builder.append("//$comment\n")
		}
		builder.append(code)
		if (bracketContents.isNotEmpty()) "{\n\t${bracketContents.joinToString("\n\t")}\n}"
		return builder.toString()
	}
}