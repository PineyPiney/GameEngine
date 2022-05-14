package com.pineypiney.game_engine.objects.game_objects

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.models.animations.Animation
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.normal
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

open class ModelledGameObject(final override val id: ResourceKey, val debug: Int = 0): GameObject() {

    val model: Model = ModelLoader.getModel(id)

    protected var animation: Animation? = null
    private var animationStartTime: Double = 0.0
    private var animationEndTime: Double = 0.0
    protected var loopAnimation: Boolean = true
    protected var nextAnimation: String = "Still"

    var erp: String = "lerp"

    override fun init() {
        super.init()
    }

    override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
        this.animation?.let { animation ->
            if(this.loopAnimation || animationEndTime > Timer.frameTime){
                val states = animation.getState(animation.getAnimationTime(animationStartTime), erp)
                model.animate(states)
            }
            else setAnimation(nextAnimation)
        }
        model.Draw(I.translate(Vec3(position)).rotate(rotation, normal).scale(Vec3(scale, 1)), view, projection, debug)
    }

    override fun renderInstanced(amount: Int, view: Mat4, projection: Mat4, tickDelta: Double) {
        model.DrawInstanced(amount, I.translate(Vec3(position)).rotate(rotation, normal).scale(Vec3(scale, 1)), view)
    }

    fun initAnimation(animation: Animation, loop: Boolean = true){
        this.model.reset()
        this.animation = animation
        this.animationStartTime = Timer.frameTime
        this.animationEndTime = if(loop) Double.MAX_VALUE else (this.animationStartTime + animation.length)
    }

    fun getAnimation(name: String) = model.animations.firstOrNull { it.name == name }

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

    override fun toData(): Array<String> {
        return arrayOf("ModelledItem")
    }

    override fun copy(): GameObject {
        return ModelledGameObject(id, debug)
    }
}