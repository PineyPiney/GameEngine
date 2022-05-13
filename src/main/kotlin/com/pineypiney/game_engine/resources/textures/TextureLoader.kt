package com.pineypiney.game_engine.resources.textures

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import java.io.InputStream

class TextureLoader private constructor() : AbstractResourceLoader<Texture>() {

    private val textures = mutableMapOf<ResourceKey, Texture>()

    fun loadTextures(streams: Map<String, InputStream>) {
        streams.forEach { (fileName, stream) ->

            val i = fileName.lastIndexOf(".")
            if (i <= 0) return@forEach
            val type = fileName.substring(i + 1)

            loadTexture(fileName.removePrefix("textures/").removeSuffix(".$type"), stream)

            stream.close()
        }
    }

    private fun loadTexture(name: String, stream: InputStream){
        textures[ResourceKey(name)] = Texture(stream, name)
    }

    fun getTexture(key: ResourceKey): Texture {
        val t = textures[key]
        return t ?: Texture.brokeTexture
    }

    fun findTexture(name: String): Texture{
        val t = textures[ResourceKey(name)] ?: textures.entries.firstOrNull { (key, _) ->
            key.key.contains(name)
        }?.value
        return t ?: Texture.brokeTexture
    }

    override fun delete() {
        textures.delete()
        textures.clear()
    }

    companion object{
        val INSTANCE = TextureLoader()

        fun getTexture(key: ResourceKey): Texture = INSTANCE.getTexture(key)
        fun findTexture(name: String): Texture = INSTANCE.findTexture(name)

        fun blank(): Texture = INSTANCE.getTexture(ResourceKey("broke"))
    }
}