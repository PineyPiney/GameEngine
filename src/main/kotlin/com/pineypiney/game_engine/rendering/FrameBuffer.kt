package com.pineypiney.game_engine.rendering

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.resources.textures.TextureLoader
import glm_.i
import glm_.vec2.Vec2t
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

open class FrameBuffer(var width: Int, var height: Int, var internalFormat: Int = GL_RGB, var format: Int = GL_RGB): Deleteable {

    constructor(size: Vec2t<*>): this(size.x.i, size.y.i)

    val FBO = glGenFramebuffers()
    val TCB = glGenTextures()
    val RBO = glGenRenderbuffers()

    open fun setSize(width: Int, height: Int){
        if(width > 0 && height > 0 && (width != this.width || height != this.height)){
            this.width = width
            this.height = height
            generate()
        }
    }

    fun setSize(size: Vec2t<*>){
        setSize(size.x.i, size.y.i)
    }

    open fun generate(){
        glBindFramebuffer(GL_FRAMEBUFFER, FBO)
        glBindTexture(GL_TEXTURE_2D, TCB)

        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        TextureLoader.loadIndividualSettings(TCB)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, TCB, 0)


        glBindRenderbuffer(GL_RENDERBUFFER, RBO)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height) // use a single renderbuffer object for both a depth AND stencil buffer.
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, RBO) // now actually attach it
        glBindRenderbuffer(GL_RENDERBUFFER, 0)

        val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
        if (status != GL_FRAMEBUFFER_COMPLETE) GameEngineI.error("Framebuffer could not be completed, status was $status")
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    open fun bind(){
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