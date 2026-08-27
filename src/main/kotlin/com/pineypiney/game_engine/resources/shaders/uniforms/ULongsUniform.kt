package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import org.lwjgl.BufferUtils
import java.nio.LongBuffer

class ULongsUniform(name: String, default: LongBuffer = BufferUtils.createLongBuffer(0), getter: UniformGetter<LongBuffer> = { BufferUtils.createLongBuffer(0) }) :
	Uniform<LongBuffer>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setULongs(name, getValue(renderer))
	}
}