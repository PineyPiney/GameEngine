package com.pineypiney.game_engine.resources.audio

import com.pineypiney.game_engine.resources.Resource
import com.pineypiney.game_engine.util.ResourceKey
import org.lwjgl.openal.AL10

class Audio(val ptr: Int): Resource() {

    override fun delete() {
        AL10.alDeleteSources(ptr)
    }

    override fun equals(other: Any?): Boolean {
        return other is Audio && ptr == other.ptr
    }

    override fun hashCode(): Int {
        return ptr.hashCode()
    }

    companion object{
        val brokeAudio: Audio; get() = AudioLoader[(ResourceKey("broke"))]
    }
}