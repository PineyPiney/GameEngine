package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.s
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

open class DirectorySource(val file: File = File("src/main/resources")) : Source {

	constructor(location: String) : this(File(location))

	val location = file.path.replace(s, '/')

	override val streamList: List<String> = createStreamList()

	fun createStreamList(): List<String> {
		return if (!file.isDirectory) listOf(file.canonicalPath)
		else file.walk().filter { !it.isDirectory }.toList()
			.map { lowercaseExtension(it.canonicalPath.removePrefix(file.canonicalPath + s).replace(s, '/')) }
	}

	override fun getStream(name: String): InputStream? {
		val path = file.canonicalPath + s + name
		return try {
			val file = File(path)
			file.inputStream()
		} catch (e: FileNotFoundException) {
			GameEngineI.warn("Could not find resources file at $path")
			null
		}
	}
}