package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.ShadUn
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.ResourceKey

abstract class ShaderRenderedComponent(parent: GameObject, s: Shader) : RenderedComponent(parent) {

	val shadUn = ShadUn(s, ::setUniforms)

	var shader: Shader
		get() = shadUn.shader
		set(value) {
			shadUn.shader = value
		}

	var uniforms: Uniforms
		get() = shadUn.uniforms
		set(value) {
			shadUn.uniforms = value
		}

	override val fields: Array<Field<*>> = super.fields + arrayOf(
		Field(
			"vsh",
			::DefaultFieldEditor,
			shader::vName,
			{ shader = ShaderLoader[ResourceKey(it), ResourceKey(shader.fName)] },
			{ it },
			{ _, s -> s }),
		Field(
			"fsh",
			::DefaultFieldEditor,
			shader::fName,
			{ shader = ShaderLoader[ResourceKey(shader.vName), ResourceKey(it)] },
			{ it },
			{ _, s -> s })
	)

	override fun init() {
		super.init()
		setUniforms()
	}

	open fun setUniforms() {
		shader.setRendererDefaults(uniforms)
		uniforms.setMat4Uniform("model", parent::worldModel)
	}
}