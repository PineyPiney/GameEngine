package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.ShadUn
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms

abstract class ShaderRenderedComponent(parent: GameObject, s: Shader) : RenderedComponent(parent) {

	protected val shadUn = ShadUn(s, ::setUniforms)

	var shader: Shader
		get() = shadUn.shader
		set(value) {
			shadUn.shader = value
		}

	var uniforms: Uniforms
		get() = shadUn.uniforms
		protected set(value) {
			shadUn.uniforms = value
		}

	override fun init() {
		setUniforms()
	}

	open fun setUniforms() {
		shader.setRendererDefaults(uniforms)
		uniforms.setMat4Uniform("model", parent::worldModel)
	}
}