package com.pineypiney.game_engine.resources.video

import com.pineypiney.game_engine.audio.AudioSourceStream
import com.pineypiney.game_engine.resources.audio.Audio
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.util.extension_functions.toByteBuffer
import org.lwjgl.openal.AL10
import java.nio.ByteBuffer

class VideoAudio(override val video: Video): VideoData<Audio>() {

    val audio = AudioSourceStream(4)

    override fun init() {
        for(i in audio.buffers.indices){
            val frame = video.audio(i)
            loadNextBuffer(frame.toByteBuffer())
        }
    }

    override fun loadNextBuffer(buffer: ByteBuffer){
        val index = nextUpdate % audio.buffers.size
        audio.updateQueue(index, AudioLoader.loadAudio(buffer, AL10.AL_FORMAT_STEREO16, video.sampleRate))
        nextUpdate++
    }

    override fun update() {
        val time = video.timeStamp * 1e6

        while(nextUpdate * video.sampleRate <= time){

            val sample = video.audio((nextUpdate + 4) % video.audioSamples.size)
            loadNextBuffer(sample.toByteBuffer())
        }
    }
    
    override fun delete() {
        audio.delete()
    }
}