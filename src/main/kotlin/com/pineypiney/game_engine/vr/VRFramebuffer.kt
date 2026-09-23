package com.pineypiney.game_engine.vr

import com.pineypiney.game_engine.rendering.opengl.OpenGlFramebuffer
import com.pineypiney.game_engine.resources.textures.TextureFormat
import com.pineypiney.game_engine.resources.textures.parameters.TextureParameters
import org.lwjgl.opengl.GL30

class VRFramebuffer(width: Int, height: Int) : OpenGlFramebuffer(width, height, TextureFormat.RGBA8) {

	override fun generate() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO)

		colour.setSize(size, TextureParameters(mipMapRange = 0..0))
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, colour.texturePointer, 0)

		// check FBO status
		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw Error("framebuffer incomplete!")

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
	}
}