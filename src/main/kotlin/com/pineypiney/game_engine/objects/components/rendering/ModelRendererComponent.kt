package com.pineypiney.game_engine.objects.components.rendering

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.fields.IntFieldRange
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionBox3DRenderer
import com.pineypiney.game_engine.objects.components.rendering.collision.CollisionPolygonRenderer
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.meshes.Mesh
import com.pineypiney.game_engine.resources.models.Bone
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.shaders.RenderShader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.parameters.PolygonMode
import com.pineypiney.game_engine.resources.shaders.parameters.RenderShaderParameters
import com.pineypiney.game_engine.util.DeletionQueue
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.Collider2D
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

open class ModelRendererComponent(parent: GameObject, var model: Model = Model.missing, shader: RenderShader = defaultShader) :
	ShaderRenderedComponent(parent, shader) {

	protected var animation: ModelAnimation? = null
	private var animationStartTime: Double = 0.0
	private var animationEndTime: Double = 0.0
	protected var loopAnimation: Boolean = true
	protected var nextAnimation: String = "Still"

	var erp: String = "lerp"

	val queue = DeletionQueue()
	val wireframeShader = ShaderLoader[ResourceKey(shader.vertex.getName()), ResourceKey(shader.fragment.getName()), RenderShaderParameters(fillMode = PolygonMode.LINE), queue]

	@IntFieldRange(0, 7)
	var debug = 0

	override fun setUniforms() {
		super.setUniforms()
		uniforms.setMat4sUniform("boneTransforms[0]") {
			model.rootBone?.getAllChildren()?.map {
				it.getMeshTransform()
			}?.toTypedArray() ?: emptyArray()
		}
	}

	override fun render(renderer: RendererI, tickDelta: Double) {
		updateAnimation()

		if(debug and Model.DEBUG_WIREFRAME > 0) {
			wireframeShader.setUp(uniforms, renderer)
			wireframeShader.setVec3("colour", Vec3(0f))
			for (mesh in model.meshes) {
				val newModel = parent.worldModel * mesh.transform
				wireframeShader.setMat4("model", newModel)
				shader.draw("vertexBuffer", mesh, renderer)
			}
		}


		shader.setUp(uniforms, renderer)

		// Lighting
		if (shader.hasDirL) setLightUniforms()

		for (mesh in model.meshes) {
			mesh.setMaterialUniforms(shader)
			val newModel = parent.worldModel * mesh.transform
			shader.setMat4("model", newModel)
			shader.draw("vertexBuffer", mesh, renderer)
		}

		if (debug and Model.DEBUG_BONES > 0) renderBones(renderer, parent, renderer.view, renderer.projection)
		val renderer = parent.getChild(parent.name + " Collider Renderer")?.renderer
		if(debug and Model.DEBUG_COLLIDER > 0){
			if(renderer == null) {
				if (model.box is Collider2D) parent.addChild(CollisionPolygonRenderer.create(parent).apply { init() })

				else parent.addChild(CollisionBox3DRenderer.create(parent).apply { init() })
			}
			else renderer.visible = true
		}
		else renderer?.visible = false
	}

	override fun getMeshes(): Collection<Mesh> = model.meshes.toList()

	fun setLightUniforms() {
		shader.setLightUniforms(parent)
	}

	fun renderBones(renderer: RendererI, parent: GameObject, view: Mat4, projection: Mat4) {
		// Render Bones
		val boneShader = Bone.boneShader

		boneShader.use(renderer.getRenderingApi())
		boneShader.setMat4("view", view)
		boneShader.setMat4("projection", projection)

		val bones: List<Bone> = model.rootBone?.getAllChildren() ?: listOf()
		for (it in bones) {
			it.render(renderer, boneShader, parent.worldModel)
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

	override fun delete() {
		super.delete()
		queue.flush()
	}

	@Suppress("UNUSED")
	companion object {
		val bonelessShader = ShaderLoader[ResourceKey("vertex/model_boneless"), ResourceKey("fragment/model")]
		val defaultShader = ShaderLoader[ResourceKey("vertex/model"), ResourceKey("fragment/model")]
		val defaultLitShader = ShaderLoader[ResourceKey("vertex/model"), ResourceKey("fragment/lit_model")]

		val bonelessPbrShader = ShaderLoader[ResourceKey("vertex/model_boneless"), ResourceKey("fragment/pbr_lit_model")]
		val pbrShader = ShaderLoader[ResourceKey("vertex/model"), ResourceKey("fragment/pbr_lit_model")]
		val tangentBonesShader = ShaderLoader[ResourceKey("vertex/tangent_bones_model"), ResourceKey("fragment/model")]


		val debugTrisShader = ShaderLoader[ResourceKey("vertex/tangent_bones_model"), ResourceKey("fragment/colour_primitives")]
		val debugWeightsShader = ShaderLoader[ResourceKey("vertex/model_weights"), ResourceKey("fragment/model_weights")]
	}
}