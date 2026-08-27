package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.Texture2D

class TextureUniform(name: String, default: Texture = Texture2D.missing, getter: UniformGetter<Texture> = { Texture2D.missing }) :
	Uniform<Texture>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setTexture(name, getValue(renderer))
	}
}