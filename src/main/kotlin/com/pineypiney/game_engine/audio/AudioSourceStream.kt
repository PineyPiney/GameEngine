package com.pineypiney.game_engine.audio

import com.pineypiney.game_engine.resources.audio.Audio
import com.pineypiney.game_engine.util.extension_functions.delete
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11

class AudioSourceStream(val buffers: MutableList<Audio>): AbstractAudioSource() {

    constructor(size: Int, init: (Int) -> Audio = { Audio(0) }): this(MutableList(size, init))

    init {
        AL10.alSourceQueueBuffers(ptr, buffers.map { it.buf }.toIntArray())
    }

    fun updateQueue(index: Int, audio: Audio){
        // Remove old Buffer
        AL11.alSourceUnqueueBuffers(ptr, intArrayOf(buffers[index].buf))
        buffers[index].delete()

        // Add new buffer
        buffers[index] = audio
        AL10.alSourceQueueBuffers(ptr, audio.buf)
    }

    override fun delete() {
        super.delete()
        buffers.delete()
    }
}