package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.LightComponent
import com.pineypiney.game_engine.objects.components.fields.IntFieldRange
import com.pineypiney.game_engine.objects.util.collision.CollisionBox2DRenderer
import com.pineypiney.game_engine.objects.util.shapes.Mesh
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.lighting.DirectionalLight
import com.pineypiney.game_engine.rendering.lighting.Light
import com.pineypiney.game_engine.rendering.lighting.PointLight
import com.pineypiney.game_engine.rendering.lighting.SpotLight
import com.pineypiney.game_engine.resources.models.Bone
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.mat4x4.Mat4

open class ModelRendererComponent(parent: GameObject, var model: Model = Model.brokeModel, shader: Shader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	override val shape: Shape<*> = model.box.shape
	protected var animation: ModelAnimation? = null
	private var animationStartTime: Double = 0.0
	private var animationEndTime: Double = 0.0
	protected var loopAnimation: Boolean = true
	protected var nextAnimation: String = "Still"

	var erp: String = "lerp"

	@IntFieldRange(0, 7)
	var debug = 0

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
			mesh.setMaterialUniforms(shader)
			val newModel = parent.worldModel * mesh.transform
			shader.setMat4("model", newModel)

			mesh.bindAndDraw()
		}

		if (debug and Model.DEBUG_BONES > 0) renderBones(parent, renderer.view, renderer.projection)
		val renderer = parent.getChild(parent.name + " Collider Renderer")?.renderer
		if(debug and Model.DEBUG_COLLIDER > 0){
			if(renderer == null) parent.addChild(CollisionBox2DRenderer(parent))
			else renderer.visible = true
		}
		else renderer?.visible = false
	}

	fun setLightUniforms() {
		val lights = (parent.objects ?: return).getAllComponents().filterIsInstance<LightComponent>().filter { it.light.on }

		val dirLight = lights.firstOrNull { it.light is DirectionalLight }
		if(dirLight == null) Light.setShaderUniformsOff(shader, "dirLight")
		else dirLight.setShaderUniforms(shader, "dirLight")

		val pointLights = lights.associateWith { it.parent.position }.filter{ it.key.light is PointLight }.entries.sortedByDescending { (it.value - parent.position).length() / (it.key.light as PointLight).linear }
		for (l in 0..<4) {
			val name = "pointLights[$l]"
			if(l < pointLights.size) pointLights[l].key.setShaderUniforms(shader, name)
			else Light.setShaderUniformsOff(shader, name)
		}

		val spotLight = lights.firstOrNull { it.light is SpotLight }
		if(spotLight == null) Light.setShaderUniformsOff(shader, "spotlight")
		else spotLight.setShaderUniforms(shader, "spotlight")
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
		val debugShader = ShaderLoader.getShader(ResourceKey("vertex/model_weights"), ResourceKey("fragment/model_weights"))
		val pbrShader = ShaderLoader.getShader(ResourceKey("vertex/model"), ResourceKey("fragment/pbr_lit_model"))
	}
}