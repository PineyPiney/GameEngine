package com.pineypiney.game_engine.level_editor.util

import com.pineypiney.game_engine.level_editor.PixelEngine
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.getOrNull
import com.pineypiney.game_engine.util.input.ControlType
import org.lwjgl.glfw.GLFW

class KeyBinds {

    companion object{

        private val defaultKeyBinds: Map<ResourceKey, KeyBind> = mapOf(
            ResourceKey("key/left") to KeyBind(GLFW.GLFW_KEY_A),
            ResourceKey("key/right") to KeyBind(GLFW.GLFW_KEY_D),
            ResourceKey("key/sprint") to KeyBind(GLFW.GLFW_KEY_LEFT_SHIFT),
            ResourceKey("key/jump") to KeyBind(GLFW.GLFW_KEY_SPACE),

            ResourceKey("key/primary") to KeyBind(GLFW.GLFW_MOUSE_BUTTON_1, ControlType.MOUSE),
            ResourceKey("key/secondary") to KeyBind(GLFW.GLFW_MOUSE_BUTTON_2, ControlType.MOUSE),

            ResourceKey("key/attack") to KeyBind(GLFW.GLFW_MOUSE_BUTTON_1, ControlType.MOUSE),

            ResourceKey("key/fullscreen") to KeyBind(GLFW.GLFW_KEY_F),
        )

        val keyBinds: MutableMap<ResourceKey, KeyBind> = defaultKeyBinds.toMutableMap()

        fun isActive(id: ResourceKey): Boolean{
            val bind = keyBinds.getOrNull(id) ?: return false
            return bind.state > 0
        }

        fun setKeyBind(key: ResourceKey, bind: KeyBind){
            if(keyBinds[key] != null) keyBinds[key] = bind
            else{
                PixelEngine.logger.info("There is no keybinding for $key")
            }
        }

        @Throws(NoSuchElementException::class)
        fun getKeyBinding(key: ResourceKey): KeyBind {
            return keyBinds[key] ?: defaultKeyBinds[key] ?:
            throw NoSuchElementException("There is no key registered for $key")
        }

        fun getKeyBindingForKey(key: Short, type: ControlType): KeyBind?{
            return keyBinds.values.firstOrNull { bind -> bind.key == key && bind.controlType == type }
        }
    }
}