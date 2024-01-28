package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.game_objects.objects_2D.Animated
import com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation.Animation
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListEntry
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListItem
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class AnimationSelector(item: Animated?, override val origin: Vec2, override val size: Vec2, pred: () -> Unit) : SelectableScrollingListItem() {

    var item: Animated? = item
        set(value) {
            field = value
            removeChildren(items)
            items.delete()
            if(field != null){
                items = field!!.animations.mapIndexed { i, a ->
                    AnimationSelectorEntry(this, a, i)
                }
                addChildren(items)
                items.init()
                updateEntries()
            }
        }

    override var name: String = "Animation Selector"

    override val entryHeight: Float = 0.2f
    override val scrollerWidth: Float = 0.05f
    override val action: (Int, SelectableScrollingListEntry<*>?) -> Unit = { i, e ->
        (e as? AnimationSelectorEntry)?.let { this.item?.setAnimation(it.text.text) }
        pred()
    }
    override var items: List<SelectableScrollingListEntry<*>> = item?.animations?.mapIndexed { i, a ->
        AnimationSelectorEntry(this, a, i)
    } ?: emptyList()

    class AnimationSelectorEntry(parent: AnimationSelector, a: Animation, i: Int) : SelectableScrollingListEntry<AnimationSelector>(parent, i){

        override var name: String = "${a.name} Animation Entry"
        val text = object : SizedStaticText(a.name, ObjectAnimator.window, 10f, Vec4(1f), shader = entryTextShader){

            override fun setUniforms() {
                super.setUniforms()

                // Limit is in 0 to Window#height space so must be transformed
                uniforms.setVec2Uniform("limits"){ (limits + Vec2(1)) * (GLFunc.viewportO.y / 2f) }
            }
        }

        override fun init() {
            super.init()
            text.init()
        }

        override fun draw() {
            super.draw()
            text.drawCenteredLeft(relative(0.1f, 0.5f))
        }

        override fun updateAspectRatio(window: WindowI) {
            super.updateAspectRatio(window)
            text.updateAspectRatio(window)
        }

        override fun delete() {
            super.delete()
            text.delete()
        }
    }
}