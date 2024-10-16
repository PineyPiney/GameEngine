package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.LightComponent
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.resources.models.Bone
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.filterValueIsInstance
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.min

open class ModelRendererComponent(parent: GameObject, var model: Model, shader: Shader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	override val renderSize: Vec2 get() = Vec2(1f)
	override val shape: Shape<*> = model.box.shape
	protected var animation: ModelAnimation? = null
	private var animationStartTime: Double = 0.0
	private var animationEndTime: Double = 0.0
	protected var loopAnimation: Boolean = true
	protected var nextAnimation: String = "Still"

	var erp: String = "lerp"

	var debug = 0

	val colliderRenderer = CollisionBox2DRenderer(parent)

	constructor(parent: GameObject) : this(parent, Model.brokeModel)

	override val fields: Array<Field<*>> = arrayOf(
		Field(
			"mdl",
			::DefaultFieldEditor,
			::model,
			{ model = it },
			{ it.name.substringBefore('.') },
			{ _, s -> ModelLoader[ResourceKey(s)] })
	)

	override fun init() {
		super.init()
		parent.addChild(colliderRenderer)
	}

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4sUniform("boneTransforms[0]") {
			model.rootBone?.getAllChildren()?.map { it.getMeshTransform() }?.toTypedArray() ?: emptyArray()
		}
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		updateAnimation()
		shader.setUp(uniforms, renderer)

		// Lighting
		if (shader.hasDirL) setLightUniforms()

		for (mesh in model.meshes) {
			mesh.setMaterial(shader)
			val newModel = parent.worldModel * mesh.transform
			shader.setMat4("model", newModel)

			mesh.bindAndDraw()
		}

		if (debug and Model.DEBUG_BONES > 0) renderBones(parent, renderer.view, renderer.projection)
		colliderRenderer.renderer?.visible = debug and Model.DEBUG_COLLIDER > 0
	}

	fun setLightUniforms() {
		val lights =
			(parent.objects ?: return).getAllComponents().filterIsInstance<LightComponent>().filter { it.light.on }
		lights.firstOrNull { it.light is DirectionalLight }?.setShaderUniforms(shader, "dirLight")
		val pointLights = lights.associate { it.parent.position to it.light }
			.filterValueIsInstance<Vec3, PointLight>().entries.sortedByDescending { (it.key - parent.position).length() / it.value.linear }
		for (l in 0..<min(4, pointLights.size)) {
			val name = "pointLights[$l]"
			shader.setVec3("$name.position", pointLights[l].key)
			pointLights[l].value.setShaderUniforms(shader, name)
		}
		lights.firstOrNull { it.light is SpotLight }?.setShaderUniforms(shader, "spotlight")
	}

	fun renderBones(parent: GameObject, view: Mat4, projection: Mat4) {
		// Render Bones
		Mesh.centerSquareShape.bind()
		val boneShader = Bone.boneShader

		boneShader.use()
		boneShader.setMat4("view", view)
		boneShader.setMat4("projection", projection)

		val bones: List<Bone> = model.rootBone?.getAllChildren() ?: listOf()
		for (it in bones) {
			it.render(boneShader, parent.worldModel)
		}
	}

	fun initAnimation(animation: ModelAnimation, loop: Boolean = true) {
		this.model.reset()
		this.animation = animation
		this.animationStartTime = Timer.frameTime
		this.animationEndTime = if (loop) Double.MAX_VALUE else (this.animationStartTime + animation.length)
	}

	fun getAnimation(name: String) = model.animations.firstOrNull { it.name == name }

	fun updateAnimation() {
		this.animation?.let { animation ->
			if (this.loopAnimation || animationEndTime > Timer.frameTime) {
				val states = animation.getState(animation.getAnimationTime(animationStartTime), erp)
				model.animate(states)
			} else setAnimation(nextAnimation)
		}
	}

	fun setAnimation(name: String, loop: Boolean = true): Boolean {

		if (name.isEmpty()) {
			this.animation = null
			return true
		} else if (name == this.animation?.name) return true
		else if (name == "reset") {
			model.reset()
			return true
		}

		val newAnimation = getAnimation(name) ?: return false
		return setAnimation(newAnimation, loop)
	}

	fun setAnimation(newAnimation: ModelAnimation, loop: Boolean = true): Boolean {
		this.loopAnimation = loop
		return if (newAnimation == this.animation) {
			true
		} else {
			initAnimation(newAnimation, loop)
			this.animation != null
		}
	}

	companion object {
		val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/model"), ResourceKey("fragment/model"))
		val defaultLitShader = ShaderLoader.getShader(ResourceKey("vertex/model"), ResourceKey("fragment/lit_model"))
		val debugShader =
			ShaderLoader.getShader(ResourceKey("vertex/model_weights"), ResourceKey("fragment/model_weights"))
	}
}