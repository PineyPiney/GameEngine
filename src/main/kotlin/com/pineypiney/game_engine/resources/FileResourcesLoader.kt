package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.util.s
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

open class FileResourcesLoader(val file: File = File("src/main/resources")) : ResourcesLoader(file.path.replace(s, '/')) {

	constructor(location: String) : this(File(location))

	override val streamList: Set<String> = createStreamList()

	fun createStreamList(): Set<String> {
		return if (!file.isDirectory) setOf(file.canonicalPath)
		else file.walk().filter { !it.isDirectory }
			.map { lowercaseExtension(it.canonicalPath.removePrefix(file.canonicalPath + s).replace(s, '/')) }.toSet()
	}

	override fun getStream(name: String): InputStream? {
		val path = file.canonicalPath + s + name
		return try {
			val file = File(path)
			file.inputStream()
		} catch (e: FileNotFoundException) {
			GameEngineI.warn("Could not find resources file at $path")
			e.printStackTrace()
			null
		}
	}
}