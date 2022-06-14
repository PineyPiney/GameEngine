package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.util.Transform
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.animations.Animation
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import glm_.i
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

open class ModelledGameObject(final override val id: ResourceKey, val debug: Int = 0): RenderedGameObject(if(debug and Model.DEBUG_MESH > 0) debugShader else defaultShader) {

    val model: Model = ModelLoader.getModel(id)

    protected var animation: Animation? = null
    private var animationStartTime: Double = 0.0
    private var animationEndTime: Double = 0.0
    protected var loopAnimation: Boolean = true
    protected var nextAnimation: String = "Still"

    var erp: String = "lerp"

    override fun setUniforms() {
        super.setUniforms()
        val bones = model.rootBone?.getAllChildren() ?: listOf()
        uniforms.setMat4sUniform("boneTransforms"){ bones.map { it.getMeshTransform() }.toTypedArray() }
        if(debug and Model.DEBUG_MESH > 0){
            uniforms.setVec3sUniform("boneColours"){ bones.map { bone -> Vec3((((bone.id + 4) % 6) > 2).i, (((bone.id + 2) % 6) > 2).i, (((bone.id) % 6) > 2).i) }.toTypedArray() }
        }
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        updateAnimation()
        super.render(view, projection, tickDelta)

        model.Draw(this, view, projection, tickDelta, shader, debug)
    }

    override fun renderInstanced(transforms: Array<Transform>, view: Mat4, projection: Mat4, tickDelta: Double) {
        super.renderInstanced(transforms, view, projection, tickDelta)

        updateAnimation()
        model.DrawInstanced(transforms.size, this, view, projection, tickDelta, shader, debug)
    }

    fun initAnimation(animation: Animation, loop: Boolean = true){
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

    fun setAnimation(newAnimation: Animation, loop: Boolean = true): Boolean{
        this.loopAnimation = loop
        return if(newAnimation == this.animation) {
            true
        }
        else{
            initAnimation(newAnimation, loop)
            this.animation != null
        }
    }

    override fun copy(): GameObject {
        return ModelledGameObject(id, debug)
    }

    companion object{

        val defaultShader = ShaderLoader.getShader(ResourceKey("vertex/model"), ResourceKey("fragment/translucent_texture"))
        val debugShader = ShaderLoader.getShader(ResourceKey("vertex/model_weights"), ResourceKey("fragment/model_weights"))

    }
}