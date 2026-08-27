package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import org.lwjgl.BufferUtils
import java.nio.IntBuffer

class UIntsUniform(name: String, default: IntBuffer = BufferUtils.createIntBuffer(0), getter: UniformGetter<IntBuffer> = { BufferUtils.createIntBuffer(0) }) :
	Uniform<IntBuffer>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setUInts(name, getValue(renderer))
	}
}