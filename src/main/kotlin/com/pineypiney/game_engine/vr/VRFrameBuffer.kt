package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.rendering.FrameBuffer
import org.lwjgl.opengl.GL11C.GL_RGBA
import org.lwjgl.opengl.GL11C.GL_RGBA8
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30
import java.nio.ByteBuffer

class VRFrameBuffer(width: Int, height: Int): FrameBuffer(width, height, GL_RGBA8, GL_RGBA) {

    override fun generate() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO)

        GL30.glBindTexture(GL30.GL_TEXTURE_2D, TCB)
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR)
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, null as ByteBuffer?)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, TCB, 0)

        // check FBO status
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            throw Error("framebuffer incomplete!")

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }
}