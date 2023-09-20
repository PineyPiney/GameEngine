package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.objects.Renderable
import com.pineypiney.game_engine.rendering.FrameBuffer
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.vr.util.logCompositorError
import glm_.L
import org.lwjgl.opengl.GL30
import org.lwjgl.openvr.VR
import org.lwjgl.openvr.VRCompositor
import org.lwjgl.openvr.Texture as VRTexture

abstract class VRRenderer<E: GameLogicI>(w: Int, h: Int): RendererI<E> {

    val leftBuffer = FrameBuffer(w, h)
    val rightBuffer = FrameBuffer(w, h)

    val leftDisplay = VRFrameBuffer(w, h)
    val rightDisplay = VRFrameBuffer(w, h)

    abstract val hmd: HMD

    override fun init() {
        leftBuffer.generate()
        rightBuffer.generate()
        leftDisplay.generate()
        rightDisplay.generate()
    }

    fun drawScene(game: GameLogicI, eye: Int, buffer: FrameBuffer, tickDelta: Double){

        clearFrameBuffer(buffer)

        val view = getView(eye)
        val proj = getProjection(eye)

        GLFunc.depthTest = true
        game.gameObjects.gameItems.filterIsInstance<Renderable>().forEach{ if(it.visible) it.render(view, proj, tickDelta)}

        GLFunc.depthTest = false
        game.gameObjects.guiItems.forEach{ if(it.visible) it.draw() }
    }

    fun blitBuffer(read: FrameBuffer, draw: FrameBuffer){
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read.FBO)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw.FBO)

        GL30.glBlitFramebuffer(0, 0, read.width, read.height, 0, 0, draw.width, draw.height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_LINEAR)

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0)
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0)
    }

    fun clearFrameBuffer(buffer: FrameBuffer){
        buffer.bind()
        clear()
    }

    fun submitFrames(leftTex: Int, rightTex: Int){

        val lt = VRTexture.create().set(leftTex.L, VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma)
        val rt = VRTexture.create().set(rightTex.L, VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma)

        VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, lt, null, 0).logCompositorError()
        VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, rt, null, 0).logCompositorError()
    }

    open fun getView(eye: Int) = hmd.eyes[eye] * hmd.hmdPose
    open fun getProjection(eye: Int) = hmd.projections[eye]

    fun deleteFrameBuffers(){
        leftBuffer.delete()
        rightBuffer.delete()
        leftDisplay.delete()
        rightDisplay.delete()
    }

    override fun delete() {
        deleteFrameBuffers()
    }
}