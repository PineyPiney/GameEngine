package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.resources.textures.Texture

class TextureMapsComponent(parent: GameObject, val textures: Map<String, Texture> = emptyMap()) :
	Component(parent, "TMC"),
	PreRenderComponent {

	constructor(parent: GameObject) : this(parent, emptyMap())

	val renderer by lazy { parent.getComponent<ShaderRenderedComponent>() }
	override val whenVisible: Boolean = true

	override fun init() {
		super.init()
		renderer?.let {
			for ((name, texture) in textures) it.uniforms.setIntUniform(name, texture::binding)
		}
	}

	override fun preRender(tickDelta: Double) {
		renderer?.let {
			for ((_, texture) in textures) {
				texture.bind()
			}
		}
	}
}