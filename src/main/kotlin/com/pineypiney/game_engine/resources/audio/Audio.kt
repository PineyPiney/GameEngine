package com.pineypiney.game_engine.resources.audio

import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.ResourceKey
import org.lwjgl.openal.AL10

class Audio(val buf: Int): Resource() {

    override fun delete() {
        AL10.alDeleteBuffers(buf)
    }

    override fun equals(other: Any?): Boolean {
        return other is Audio && buf == other.buf
    }

    override fun hashCode(): Int {
        return buf.hashCode()
    }

    companion object{
        val brokeAudio: Audio; get() = AudioLoader[(ResourceKey("broke"))]
    }
}