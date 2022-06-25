package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.resources.AbstractResourceLoader
import java.io.InputStream

class VideoLoader private constructor(): AbstractResourceLoader<Video>() {



    fun loadVideos(streams: Map<String, InputStream>) {
        for((fileName, stream) in streams){

            val i = fileName.lastIndexOf(".")
            if (i <= 0) continue
            val type = fileName.substring(i + 1)

            //loadVideo(fileName.removeSuffix(".$type"), BufferedInputStream(stream))

            stream.close()
        }
    }

    fun loadVideo(name: String, stream: InputStream): Video?{
        return null //Video(stream)
    }

    override fun delete() {

    }

    companion object{
        val INSTANCE: VideoLoader = VideoLoader()
    }
}