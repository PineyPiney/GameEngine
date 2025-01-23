package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.rendering.RendererI
import glm_.f

class AnimatedComponent(
	parent: GameObject,
	var animation: Animation,
	val animations: List<Animation> = listOf(animation)
) : Component(
	parent
), PreRenderComponent {

	constructor(parent: GameObject) : this(parent, Animation.default)

	override val whenVisible: Boolean = true
	var animationTime: Float = 0f
	var playing: Boolean = true

	override fun preRender(renderer: RendererI, tickDelta: Double) {
		// For animated items the texture must be set to the animations current frame
		if (playing) updateAnimationTime()
		val p = getProperties()
		for ((key, value) in p) parent.setProperty(key, value)
	}

	fun setAnimation(name: String) {
		animations.firstOrNull { it.name == name }?.let { animation = it }
	}

	fun getProperties(): Map<String, String> {
		return animation.getProperties(animationTime)
	}

	fun updateAnimationTime() {
		animationTime = (animationTime + Timer.frameDelta).f % animation.length
	}
}