package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.objects.components.ButtonComponent

abstract class AbstractButton : MenuItem() {

    abstract val action: (ButtonComponent) -> Unit
    val interactor: ButtonComponent get() = getComponent()!!

    override fun addComponents() {
        super.addComponents()
        components.add(ButtonComponent(this, action))
    }
}