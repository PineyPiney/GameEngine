package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.textures.Texture2D

class TextureMapsComponent(parent: GameObject, val textures: Map<String, Texture2D> = emptyMap()) :
	Component(parent),
	PreRenderComponent {

	constructor(parent: GameObject) : this(parent, emptyMap())

	val renderer by lazy { parent.getComponent<ShaderRenderedComponent>() }
	override val whenVisible: Boolean = true

	override fun init() {
		super.init()
		renderer?.let { for ((name, texture) in textures) it.uniforms.setTextureUniform(name) { texture } }
	}

	override fun preRender(renderer: RendererI, tickDelta: Double) {

	}
}