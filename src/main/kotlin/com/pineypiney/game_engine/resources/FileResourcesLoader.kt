package com.pineypiney.game_engine.resources

import com.pineypiney.game_engine.util.s
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

open class FileResourcesLoader(val file: File) : ResourcesLoader(){

    constructor(location: String) : this(File(location))

    override val streamList: Set<String> = createStreamList()

    fun createStreamList(): Set<String>{
        return if(!file.isDirectory) setOf(file.canonicalPath)
        else file.walk().filter { !it.isDirectory }.map { it.canonicalPath.removePrefix(file.canonicalPath + s).replace(s, '/') }.toSet()
    }


    override fun getStream(name: String): InputStream {
        return try {
            val path = file.canonicalPath + s + name
            val file = File(path)
            file.inputStream()
        }
        catch (e: FileNotFoundException){
            println("Could not find file $name")
            InputStream.nullInputStream()
        }

    }
}