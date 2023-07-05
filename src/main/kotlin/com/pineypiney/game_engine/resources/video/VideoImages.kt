package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.extension_functions.delete
import java.nio.ByteBuffer

class VideoImages(override val video: Video): VideoData<Texture>() {

    val format = VideoLoader.ffmpegToGL(video.pixelFormat).y

    val textures = Array(4){ Texture(video.name, TextureLoader.createTexture(null, video.width, video.height)) }

    val current: Texture get() = textures[currentIndex % textures.size]

    override fun init() {
        nextUpdate = 0
        for(i in textures.indices){
            val buffer = video.image(i)
            loadNextBuffer(buffer)
        }
    }

    override fun loadNextBuffer(buffer: ByteBuffer){
        val index = nextUpdate % textures.size
        val updateTexture = textures[index]
        updateTexture.setData(buffer, format = format)
        nextUpdate++
    }

    override fun update() {
        val time = video.timeStamp

        while(nextUpdate * video.frameTime <= time){
            updateNextFrame()
        }
    }

    fun updateNextFrame(){
        val t1 = Timer.getCurrentTime()
        val frame = video.image((nextUpdate + 4) % video.images.size)
        val t2 = Timer.getCurrentTime()

        loadNextBuffer(frame)
        val d1 = t2 - t1
        val d2 = Timer.getCurrentTime() - t2
        GameEngineI.debug("Stage times are $d1 and $d2")
        GameEngineI.debug("Frame Time is ${(nextUpdate + 4) * video.frameRate}")
    }

    override fun delete() {
        textures.delete()
    }
}