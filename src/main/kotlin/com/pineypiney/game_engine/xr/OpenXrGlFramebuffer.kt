package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.objects.Deletable
import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D
import org.lwjgl.opengl.GL30C

class OpenXrGlFramebuffer(val colourTextures: List<OpenGlTexture2D>, val depthTextures: List<OpenGlTexture2D>) : Deletable{

	val FBO: Int = GL30C.glGenFramebuffers()

	fun setTextures(colour: Int, depth: Int){
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, FBO)
		GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL30C.GL_TEXTURE_2D, colourTextures[colour].texturePointer, 0)
		GL30C.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, depthTextures[depth].texturePointer, 0)
	}

	override fun delete() {
		GL30C.glDeleteFramebuffers(FBO)
	}
}