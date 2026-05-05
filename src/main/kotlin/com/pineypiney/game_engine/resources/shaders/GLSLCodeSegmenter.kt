package com.pineypiney.game_engine.resources.shaders

class GLSLCodeSegmenter(val code: String) {

	var macro = false
	var singleLineComment = false
	var multilineComment = false
	val inComment get() = singleLineComment || multilineComment
	var currentComment = ""

	var i = 0
	var lastIndex = 0

	var segmentTree = mutableListOf(mutableListOf<GLSLCodeSegment>())
	var currentSegment = segmentTree.last()

	fun addSegment(isComment: Boolean = inComment) {

		val contents = code.substring(lastIndex, i).trim()
		if (isComment) {
			currentComment += contents
		} else if (contents.isNotEmpty()) {
			currentSegment.add(GLSLCodeSegment(contents, comment = currentComment))
			currentComment = ""
		}
	}

	fun segmentCode() {

		while (i < code.length) {
			val c = code[i]
			when (c) {
				'\n' -> {
					if (singleLineComment) {
						addSegment(true)
						singleLineComment = false
						lastIndex = ++i
					} else if (macro) {
						addSegment(false)
						macro = false
						lastIndex = ++i
					} else ++i
				}

				';' -> {
					if (!inComment) {
						addSegment(false)
					}
					lastIndex = ++i
				}

				'/' -> {
					// Only start a new single line comment if not already in a comment
					if (code[i + 1] == '/' && !inComment) {
						addSegment(false)
						singleLineComment = true
						i += 2
						lastIndex = i
					}

					// Start a new multiline comment
					else if (code[i + 1] == '*' && !multilineComment) {
						// End previous single line comment or code segment
						addSegment(singleLineComment)
						singleLineComment = false
						multilineComment = true

						i += 2
						lastIndex = i
					} else i++
				}

				'*' -> {
					if (code[i + 1] == '/' && multilineComment) {
						addSegment(true)
						multilineComment = false

						i += 2
						lastIndex = i

					} else i++
				}

				'{' -> {
					if (!inComment) {
						addSegment(false)
						val parent = currentSegment.last()
						segmentTree.add(parent.bracketContents)
						currentSegment = parent.bracketContents
						lastIndex = ++i
					} else i++
				}

				'}' -> {
					if (!inComment) {
						addSegment(false)
						segmentTree.removeLast()
						currentSegment = segmentTree.last()
						lastIndex = ++i
					} else i++
				}

				'#' -> {
					if (!inComment) {
						macro = true
						lastIndex = i++
					} else i++
				}

				else -> i++
			}
		}
	}
}