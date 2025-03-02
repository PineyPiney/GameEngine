package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import kotlin.math.min

open class AtlasAnimatedSprite(parent: GameObject, sprite: Sprite, var numFrames: Int, var fps: Float, shader: Shader = atlasShader, val frameCallback: AtlasAnimatedSprite.(Int) -> Unit = {}): SpriteComponent(parent, sprite, shader) {

	val size get() = 1f / numFrames
	var startTime = 0.0

	var currentFrame = -1
	var loop = true

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setFloatUniform("origin"){
			val f = getFrame()
			if(f != currentFrame) {
				currentFrame = f
				frameCallback(f)
			}
			size * f
		}
	}

	fun getFrame(): Int {
		val f = ((Timer.frameTime - startTime) * fps).toInt()
		return if(loop) f % numFrames
		else min(f, numFrames - 1)
	}

	fun restart(){
		startTime = Timer.frameTime
	}

	fun setAnimation(data: AtlasAnimationData){
		data.start(this)
	}

	companion object {
		val atlasShader = ShaderLoader[
				ResourceKey("vertex/2D"),
				ResourceKey("fragment/animation_atlas")
		]
	}

	open class AtlasAnimationData(val texture: Texture, val numFrames: Int, val fps: Float, val loop: Boolean = true, val startAtBeginning: Boolean = false){
		fun start(animator: AtlasAnimatedSprite){
			animator.sprite.texture = texture
			animator.numFrames = numFrames
			animator.fps = fps
			animator.loop = loop
			if(startAtBeginning) animator.restart()
		}
	}
}