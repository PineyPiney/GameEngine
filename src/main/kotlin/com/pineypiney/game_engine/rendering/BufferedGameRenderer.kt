package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.util.shapes.Shape
import org.lwjgl.opengl.GL13.*

abstract class BufferedGameRenderer<E: IGameLogic>: GameRenderer<E>() {

    val buffer = FrameBuffer(0, 0)

    override fun init() {
        buffer.setSize(window.size)
    }

    open fun clearFrameBuffer(buffer: FrameBuffer = this.buffer){
        buffer.bind()
        clear()
    }

    protected fun drawBufferTexture(buffer: FrameBuffer = this.buffer){
        val shape = Shape.screenQuadShape
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, buffer.TCB)
        shape.bindAndDraw()
    }

    override fun updateAspectRatio(window: Window, objects: ObjectCollection) {
        buffer.setSize(window.size)
    }

    open fun deleteFrameBuffers(){
        buffer.delete()
    }

    override fun delete() {
        deleteFrameBuffers()
    }
}