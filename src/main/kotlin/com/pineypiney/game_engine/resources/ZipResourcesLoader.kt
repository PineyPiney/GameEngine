package com.pineypiney.game_engine.resources

import java.io.InputStream
import java.util.zip.ZipFile

open class ZipResourcesLoader(val zipFile: ZipFile): ResourcesLoader(){

    constructor(location: String) : this(ZipFile(location))

    override val streamList: Set<String> = zipFile.entries().toList().map { it.name.lowercase() }.toSet()

    override fun getStream(name: String): InputStream = zipFile.getInputStream(zipFile.getEntry(name))
}