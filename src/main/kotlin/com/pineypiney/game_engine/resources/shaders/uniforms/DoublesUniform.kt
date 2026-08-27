package com.pineypiney.game_engine.resources.shaders.uniforms

import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import org.lwjgl.BufferUtils
import java.nio.DoubleBuffer

class DoublesUniform(
	name: String,
	default: DoubleBuffer = BufferUtils.createDoubleBuffer(0),
	getter: UniformGetter<DoubleBuffer> = { BufferUtils.createDoubleBuffer(0) }
) : Uniform<DoubleBuffer>(name, default, getter) {

	override fun apply(shader: Shader, renderer: RendererI) {
		shader.setDoubles(name, getValue(renderer))
	}
}