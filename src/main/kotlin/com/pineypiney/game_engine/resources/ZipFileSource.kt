package com.pineypiney.game_engine.resources

import java.io.InputStream
import java.util.zip.ZipFile

open class ZipFileSource(val zipFile: ZipFile) : Source {

	constructor(location: String) : this(ZipFile(location))

	override val streamList: List<String> = zipFile.entries().toList().map { lowercaseExtension(it.name) }

	override fun getStream(name: String): InputStream = zipFile.getInputStream(zipFile.getEntry(name))
}