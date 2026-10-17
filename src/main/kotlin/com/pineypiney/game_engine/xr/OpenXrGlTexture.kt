package com.pineypiney.game_engine.xr

import com.pineypiney.game_engine.resources.textures.opengl.OpenGlTexture2D

class OpenXrGlTexture(id: String, handle: Int, override val internalFormat: Int) : OpenGlTexture2D(id, handle) {
}