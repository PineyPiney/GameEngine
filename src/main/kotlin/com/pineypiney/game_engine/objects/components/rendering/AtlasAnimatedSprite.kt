package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.fields.EditorIgnore
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Sprite
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.delete
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import kotlin.math.min

open class AtlasAnimatedSprite(parent: GameObject, texture: Texture, ppu: Float, numFrames: Int, var fps: Float, spriteCenter: Vec2 = Vec2(.5f), shader: RenderShader = SpriteComponent.defaultShader, val frameCallback: AtlasAnimatedSprite.(Int) -> Unit = {}): ShaderRenderedComponent(parent, shader) {

	constructor(parent: GameObject, data: AtlasAnimationData, ppu: Float, spriteCenter: Vec2 = Vec2(.5f), shader: RenderShader = SpriteComponent.defaultShader, frameCallback: AtlasAnimatedSprite.(Int) -> Unit = {}): this(parent, data.texture, ppu, data.numFrames, data.fps, spriteCenter, shader, frameCallback){
		loop = data.loop
		if(data.startAtBeginning) restart()
	}

	var startTime = 0.0
	var currentFrame = -1
	var loop = true

	@EditorIgnore
	var shouldChange = true

	var texture: Texture = texture
		set(value) { if(field != value) { field = value; shouldChange = true } }
	var ppu: Float = ppu
		set(value) { if(field != value) { field = value; shouldChange = true } }
	var numFrames: Int = numFrames
		set(value) { if(field != value) { field = value; shouldChange = true } }
	var spriteCenter: Vec2 = spriteCenter
		set(value) { if(field != value) { field = value; shouldChange = true } }

	var origin = Vec2i(0)
		set(value) { if(field != value) { field = value; shouldChange = true } }
	var size = texture.size
		set(value) { if(field != value) { field = value; shouldChange = true } }
	var flipX = false
		set(value) { if(field != value) { field = value; shouldChange = true } }
	var flipY = false
		set(value) { if(field != value) { field = value; shouldChange = true } }

	protected val meshes = mutableListOf<Mesh>()

	override fun init() {
		super.init()
		refreshMeshes()
	}

	fun getFrame(): Int {
		val f = ((Timer.frameTime - startTime) * fps).toInt()
		return if(loop) f % numFrames
		else min(f, numFrames - 1)
	}

	override fun getMeshes(): Collection<Mesh> = listOf(meshes.getOrNull(currentFrame) ?: meshes[0])

	override fun render(renderer: RendererI, tickDelta: Double) {
		if(meshes.isEmpty()) return
		else if(shouldChange) refreshMeshes()

		val frame = getFrame()
		if(currentFrame != frame) {
			currentFrame = frame
			frameCallback(frame)
		}

		shader.setUp(uniforms, renderer)
		texture.bind()
		meshes.getOrNull(currentFrame)?.bindAndDraw()
	}

	fun restart(){
		startTime = Timer.frameTime
	}

	fun createMeshes(list: MutableList<Mesh>): MutableList<Mesh>{
		val origin = Vec2(origin) / texture.size
		val size = Vec2(size.x.toFloat() / (texture.width * numFrames), size.y.toFloat() / texture.height)
		for(i in 0..<numFrames){
			val sprite = Sprite(texture, ppu, spriteCenter, origin + Vec2(size.x * i, 0f), size, flipX, flipY)
			list.add(sprite.fetchMesh())
		}
		return list
	}

	fun setAnimation(data: AtlasAnimationData){
		data.start(this)
		refreshMeshes()
	}

	fun refreshMeshes(){
		meshes.delete()
		meshes.clear()
		createMeshes(meshes)
		shouldChange = false
	}

	override fun delete() {
		super.delete()
		meshes.delete()
	}

	companion object {
		val atlasShader = ShaderLoader[ResourceKey("vertex/2D"), ResourceKey("fragment/animation_atlas")]
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