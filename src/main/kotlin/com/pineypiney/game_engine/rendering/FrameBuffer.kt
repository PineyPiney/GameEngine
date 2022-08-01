package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.objects.Deleteable
import glm_.i
import glm_.vec2.Vec2t
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class FrameBuffer(var width: Int, var height: Int): Deleteable {

    constructor(size: Vec2t<*>): this(size.x.i, size.y.i)

    val FBO = glGenFramebuffers()
    val TCB = glGenTextures()
    val RBO = glGenRenderbuffers()

    fun setSize(width: Int, height: Int){
        this.width = width
        this.height = height
        generate()
    }

    fun setSize(size: Vec2t<*>){
        setSize(size.x.i, size.y.i)
    }

    fun generate(){
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

    fun bind(){
        glBindFramebuffer(GL_FRAMEBUFFER, FBO)
    }

    override fun delete() {
        glDeleteFramebuffers(FBO)
        glDeleteTextures(TCB)
        glDeleteRenderbuffers(RBO)
    }

    companion object{
        /**
         * Unbind framebuffers, so that things are now drawn onto the screen
         */
        fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }
}