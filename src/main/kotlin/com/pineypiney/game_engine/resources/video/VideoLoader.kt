package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import com.pineypiney.game_engine.util.ResourceKey
import java.io.BufferedInputStream
import java.io.InputStream

class VideoLoader private constructor(): AbstractResourceLoader<Video>() {

    override val missing: Video = Video.broke

    fun loadVideos(streams: Map<String, InputStream>) {
        for((fileName, stream) in streams){

            val i = fileName.lastIndexOf(".")
            if (i <= 0) continue
            val type = fileName.substring(i + 1)

            loadVideo(fileName.removeSuffix(".$type"), BufferedInputStream(stream))

            stream.close()
        }
    }

    fun loadVideo(name: String, stream: InputStream): Video{
        val v = Video(stream)
        map[ResourceKey(name)] = v
        return v
    }

    companion object{
        val INSTANCE: VideoLoader = VideoLoader()
    }
}