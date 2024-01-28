package com.pineypiney.game_engine.apps.animator

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.objects.Drawable
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.menu_items.StaticInteractableMenuItem
import com.pineypiney.game_engine.objects.util.components.Component
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.init
import com.pineypiney.game_engine.window.WindowI
import glm_.vec2.Vec2

class ComponentEditor(var o: Storable, component: Component, val window: WindowI, override var origin: Vec2, override var size: Vec2, val callback: (String, String) -> Unit) : StaticInteractableMenuItem() {

    var component: Component = component
        set(value) {
            field = value
            children.filterIsInstance<Deleteable>().delete()
            removeChildren(children)
            generateFields()
        }

    override var name: String = "Component Editor"

    override fun init() {
        super.init()

        generateFields()
    }

    override fun draw() {
        for (c in children.filterIsInstance<Drawable>()) c.draw()
    }

    fun generateFields(){
        var i = component.fields.size
        val h = 0.06f

        origin = Vec2(origin.x, -0.5f * h * i)
        size = Vec2(1f - origin.x, h * i)

        var id = ""
        var p = component.parent
        while(p != o){
            id = p.name + '.' + id
            p = p.parent ?: break
        }
        for(f in component.fields){
            children.add(f.editor(component, id + component.id + '.' + f.id, window, Vec2(origin.x, origin.y + (h * --i)), Vec2(size.x, h), callback))
        }
        children.filterIsInstance<Initialisable>().init()
    }

    fun updateField(id: String){
        val fe = children.filterIsInstance<Component.FieldEditor<*, *>>().firstOrNull { it.id == id }
        if(fe != null) fe.update()
        else GameEngineI.logger.warn("Could not find FieldEditor $id")
    }
}