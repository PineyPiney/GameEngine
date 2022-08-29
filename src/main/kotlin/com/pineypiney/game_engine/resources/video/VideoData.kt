package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.Initialisable
import glm_.i
import java.nio.ByteBuffer
import kotlin.math.floor

abstract class VideoData<E: Deleteable>: Initialisable {

    abstract val video: Video

    var nextUpdate = 0
    val currentIndex: Int get() = floor(video.timeStamp * video.frameRate).i

    abstract fun loadNextBuffer(buffer: ByteBuffer)
    abstract fun update()

    fun reset(){
        nextUpdate = 0
    }
}