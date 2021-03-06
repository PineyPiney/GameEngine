package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.ObjectCollection
import com.pineypiney.game_engine.objects.util.shapes.Shape
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL46C.*
import java.nio.ByteBuffer

abstract class BufferedGameRenderer<E: IGameLogic>: GameRenderer<E>() {

    val FBO = glGenFramebuffers()
    val TCB = glGenTextures()
    val RBO = glGenRenderbuffers()

    override fun init() {
        createFrameBuffer(FBO, TCB, RBO, window.size)
    }

    fun createFrameBuffer(FBO: Int, TCB: Int, RBO: Int, size: Vec2i) =
        createFrameBuffer(FBO, TCB, RBO, size.x, size.y)

    fun createFrameBuffer(FBO: Int, TCB: Int, RBO: Int, width: Int, height: Int){

        glBindFramebuffer(GL_FRAMEBUFFER, FBO)
        glBindTexture(GL_TEXTURE_2D, TCB)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, TCB, 0)


        glBindRenderbuffer(GL_RENDERBUFFER, RBO)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height) // use a single renderbuffer object for both a depth AND stencil buffer.
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, RBO) // now actually attach it

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) GameEngine.logger.error("Framebuffer could not be completed in ${this::class}")
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    open fun clearFrameBuffer(buffer: Int = FBO){
        glBindFramebuffer(GL_FRAMEBUFFER, buffer)
        clear()
    }

    protected fun drawBufferTexture(texture: Int = TCB){
        val shape = Shape.screenQuadShape
        shape.bind()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture)
        shape.draw()
    }

    override fun updateAspectRatio(window: Window, objects: ObjectCollection) {
        createFrameBuffer(FBO, TCB, RBO, window.size)
    }

    open fun deleteFrameBuffers(){
        glDeleteFramebuffers(FBO)
        glDeleteTextures(TCB)
        glDeleteRenderbuffers(RBO)
    }

    override fun delete() {
        deleteFrameBuffers()
    }
}