package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.vec2.Vec2
import kotlin.math.min

open class AtlasAnimatedSprite(parent: GameObject, var texture: Texture, var ppu: Float, var numFrames: Int, var fps: Float, var spriteCenter: Vec2 = Vec2(.5f), shader: Shader = SpriteComponent.defaultShader, val frameCallback: AtlasAnimatedSprite.(Int) -> Unit = {}): ShaderRenderedComponent(parent, shader) {

	val size get() = 1f / numFrames
	var startTime = 0.0
	var currentFrame = -1
	var loop = true

	var flipX = false
	var flipY = false

	override val shape: Shape<*> get() = meshes.getOrNull(currentFrame)?.shape ?: meshes[0].shape
	protected val meshes = createMeshes()

	fun getFrame(): Int {
		val f = ((Timer.frameTime - startTime) * fps).toInt()
		return if(loop) f % numFrames
		else min(f, numFrames - 1)
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		currentFrame = getFrame()
		shader.setUp(uniforms, renderer)
		texture.bind()
		meshes.getOrNull(currentFrame)?.bindAndDraw()
	}

	fun restart(){
		startTime = Timer.frameTime
	}

	fun createMeshes(): MutableList<Mesh>{
		val list = mutableListOf<Mesh>()
		val size = size
		for(i in 0..<numFrames){
			val sprite = Sprite(texture, ppu, spriteCenter, Vec2(size * i, 0f), Vec2(size, 1f), flipX, flipY)
			list.add(sprite.mesh)
		}
		return list
	}

	fun setAnimation(data: AtlasAnimationData){
		data.start(this)

		meshes.delete()
		meshes.clear()

		meshes.addAll(createMeshes())
	}

	override fun delete() {
		super.delete()
		meshes.delete()
	}

	companion object {
		val atlasShader = ShaderLoader[
				ResourceKey("vertex/2D"),
				ResourceKey("fragment/animation_atlas")
		]
	}

	open class AtlasAnimationData(val texture: Texture, val numFrames: Int, val fps: Float, val loop: Boolean = true, val startAtBeginning: Boolean = false){
		fun start(animator: AtlasAnimatedSprite){
			animator.texture = texture
			animator.numFrames = numFrames
			animator.fps = fps
			animator.loop = loop
			if(startAtBeginning) animator.restart()
		}
	}
}