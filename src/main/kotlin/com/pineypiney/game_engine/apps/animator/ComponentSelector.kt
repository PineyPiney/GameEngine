package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListEntry
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.SelectableScrollingListItem
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.util.components.Component
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2
import glm_.vec4.Vec4

class ComponentSelector(item: Storable?, override val origin: Vec2, override val size: Vec2, pred: Component.() -> Unit) : SelectableScrollingListItem() {

    var item: Storable? = item
        set(value) {
            field = value
            removeChildren(items)
            items.delete()
            if(field != null){

                items = getComponents(field!!, "").mapIndexed { i, c ->
                    ComponentSelectorEntry(this, c, i)
                }
                addChildren(items)
                items.init()
                updateEntries()
            }
        }



    override var name: String = "Component Selector"

    override val entryHeight: Float = 0.2f
    override val scrollerWidth: Float = 0.05f

    override var items: List<SelectableScrollingListEntry<*>> = getComponents(item, "").mapIndexed { i, s ->
        ComponentSelectorEntry(this, s, i)
    }

    override val action: (Int, SelectableScrollingListEntry<*>?) -> Unit = { i, e ->
        if(i != -1) {
            val c = (e as? ComponentSelectorEntry)?.c ?: ""
            this.item?.getComponent(c)?.pred()
        }
    }

    companion object{

        fun getComponents(o: Storable?, prefix: String): Set<String>{
            if(o == null) return emptySet()
            val list = o.components.map { prefix + it.id }.toMutableSet()
            for(c in o.children) list.addAll(getComponents(c, "$prefix${c.name}."))
            return list
        }
    }

    class ComponentSelectorEntry(parent: ComponentSelector, val c: String, i: Int) : SelectableScrollingListEntry<ComponentSelector>(parent, i){

        override var name: String = "$c Component Entry"
        val text = object : SizedStaticText(c, ObjectAnimator.window, 10f, Vec4(1f), shader = entryTextShader){

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