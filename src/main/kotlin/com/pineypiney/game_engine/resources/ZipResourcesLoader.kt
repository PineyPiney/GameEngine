package com.pineypiney.game_engine.resources

import java.io.InputStream
import java.util.zip.ZipFile

open class ZipResourcesLoader(override val factory: ResourceFactory, val zipFile: ZipFile) : ResourcesLoader("") {

	constructor(factory: ResourceFactory, location: String) : this(factory, ZipFile(location))

	override val streamList: Set<String> = zipFile.entries().toList().map { lowercaseExtension(it.name) }.toSet()

	override fun getStream(name: String): InputStream = zipFile.getInputStream(zipFile.getEntry(name))
}