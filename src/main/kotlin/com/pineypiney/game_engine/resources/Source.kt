package com.pineypiney.game_engine.resources

import java.io.InputStream

interface Source {

	val streamList: List<String>

	fun getStream(name: String): InputStream?

	fun lowercaseExtension(file: String): String {
		val i = file.lastIndexOf('.')
		return if (i == -1) file
		else file.substring(0..i) + file.substring(i + 1).lowercase()
	}
}