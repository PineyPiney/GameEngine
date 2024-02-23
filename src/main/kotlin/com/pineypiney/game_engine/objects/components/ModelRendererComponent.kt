package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.util.collision.CollisionBoxRenderer
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.resources.models.Bone
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.animations.ModelAnimation
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

open class ModelRendererComponent(parent: GameObject, var model: Model, shader: Shader = defaultShader): RenderedComponent(parent, shader) {

    override val renderSize: Vec2 get() = Vec2(1f)
    override val shape: Shape = Rect2D(Vec2(), Vec2(1f)) //TODO
    protected var animation: ModelAnimation? = null
    private var animationStartTime: Double = 0.0
    private var animationEndTime: Double = 0.0
    protected var loopAnimation: Boolean = true
    protected var nextAnimation: String = "Still"

    var erp: String = "lerp"

    var debug = 0


    constructor(parent: GameObject): this(parent, Model.brokeModel)

    override val fields: Array<Field<*>> = arrayOf(
        Field("mdl", ::DefaultFieldEditor, ::model, { model = it }, { it.name.substringBefore('.') }, { _, s -> ModelLoader[ResourceKey(s)]} )
    )

    override fun render(renderer: RendererI<*>, tickDelta: Double) {
        shader.setUp(uniforms, renderer)

        for(mesh in model.meshes) {
            mesh.setMaterial(shader)
            mesh.setLights(shader)
            val newModel = parent.worldModel * mesh.transform.model
            shader.setMat4("model", newModel)

            mesh.bindAndDraw()
        }

        if(debug and Model.DEBUG_BONES > 0) renderBones(parent, renderer.view, renderer.projection)
        if(debug and Model.DEBUG_COLLIDER > 0) renderCollider(parent, renderer, tickDelta)
    }

    fun renderBones(parent: GameObject, view: Mat4, projection: Mat4){
        // Render Bones
        VertexShape.centerSquareShape.bind()
        val boneShader = Bone.boneShader

        boneShader.use()
        boneShader.setMat4("view", view)
        boneShader.setMat4("projection", projection)

        val bones: List<Bone> = model.rootBone?.getAllChildren() ?: listOf()
        for(it in bones) { it.render(boneShader, parent.worldModel) }
    }

    fun renderCollider(parent: GameObject, renderer: RendererI<*>, tickDelta: Double){
        val collider = parent.getComponent<ColliderComponent>()
        if(collider != null) {
            val crenderer = CollisionBoxRenderer(collider)
            crenderer.setUniforms()
            crenderer.render(renderer, tickDelta)
        }
    }

    fun initAnimation(animation: ModelAnimation, loop: Boolean = true){
        this.model.reset()
        this.animation = animation
        this.animationStartTime = Timer.frameTime
        this.animationEndTime = if(loop) Double.MAX_VALUE else (this.animationStartTime + animation.length)
    }

    fun getAnimation(name: String) = model.animations.firstOrNull { it.name == name }

    fun updateAnimation(){
        this.animation?.let { animation ->
            if(this.loopAnimation || animationEndTime > Timer.frameTime){
                val states = animation.getState(animation.getAnimationTime(animationStartTime), erp)
                model.animate(states)
            }
            else setAnimation(nextAnimation)
        }
    }

    fun setAnimation(name: String, loop: Boolean = true): Boolean{

        if(name.isEmpty()) {
            this.animation = null
            return true
        }
        else if(name == this.animation?.name) return true
        else if(name == "reset"){
            model.reset()
            return true
        }

        val newAnimation = getAnimation(name) ?: return false
        return setAnimation(newAnimation, loop)
    }

    fun setAnimation(newAnimation: ModelAnimation, loop: Boolean = true): Boolean{
        this.loopAnimation = loop
        return if(newAnimation == this.animation) {
            true
        }
        else{
            initAnimation(newAnimation, loop)
            this.animation != null
        }
    }

    companion object{
        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/model"), ResourceKey("fragment/model"))
        val defaultLitShader = ShaderLoader.getShader(ResourceKey("vertex/model"), ResourceKey("fragment/lit_model"))
        val debugShader = ShaderLoader.getShader(ResourceKey("vertex/model_weights"), ResourceKey("fragment/model_weights"))
    }
}