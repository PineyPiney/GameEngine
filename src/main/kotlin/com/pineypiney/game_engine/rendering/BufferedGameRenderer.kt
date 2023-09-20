package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.window.WindowI

abstract class BufferedGameRenderer<E: GameLogicI>: WindowRendererI<E> {

    val buffer = FrameBuffer(0, 0)

    override fun init() {
        camera.init()
        buffer.setSize(window.framebufferSize)
    }

    open fun clearFrameBuffer(buffer: FrameBuffer = this.buffer){
        buffer.bind()
        clear()
    }

    override fun updateAspectRatio(window: WindowI, objects: ObjectCollection) {
        camera.updateAspectRatio()
        buffer.setSize(window.framebufferSize)
    }

    open fun deleteFrameBuffers(){
        buffer.delete()
    }

    override fun delete() {
        deleteFrameBuffers()
    }

    companion object{
        val screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
        val screenUniforms = screenShader.compileUniforms()
    }
}