package com.pineypiney.game_engine.util.input

import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.getOrNull
import glm_.b
import glm_.s
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW.*

class Inputs(val window: Window) {

    val keyboard = KeyboardInput(this)
    val mouse = MouseInput(this)

    var mouseMoveCallback = { _: Window, _: Vec2, _: Vec2 -> {} }           // gameEngine.activeScreen.onCursorMove(win, screenPos, cursorOffset)
    var mouseScrollCallback = { _: Window, _: Vec2 -> {} }                  // gameEngine.activeScreen.onScroll(Window.getWindow(handle), scrollOffset)
    var keyPressCallback = { _: KeyBind, _: Int -> {} }                     // gameEngine.activeScreen.onInput(input, action)
    var keyboardCharCallback = { _: Int -> {} }

    val primary get() = getKeyBinding(ResourceKey("key/primary"))
    val secondary get() = getKeyBinding(ResourceKey("key/secondary"))

    private val defaultKeyBinds: Map<ResourceKey, KeyBind> = mapOf(
        Pair(ResourceKey("key/left"), KeyBind(GLFW_KEY_A)),
        Pair(ResourceKey("key/right"), KeyBind(GLFW_KEY_D)),
        Pair(ResourceKey("key/sprint"), KeyBind(GLFW_KEY_LEFT_SHIFT)),
        Pair(ResourceKey("key/jump"), KeyBind(GLFW_KEY_SPACE)),

        Pair(ResourceKey("key/primary"), KeyBind(GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)),
        Pair(ResourceKey("key/secondary"), KeyBind(GLFW_MOUSE_BUTTON_2, ControlType.MOUSE)),

        Pair(ResourceKey("key/attack"), KeyBind(GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)),

        Pair(ResourceKey("key/fullscreen"), KeyBind(GLFW_KEY_F)),
    )

    val keyBinds: MutableMap<ResourceKey, KeyBind> = defaultKeyBinds.toMutableMap()

    private val modStates: MutableMap<Short, Boolean> = mutableMapOf(
        Pair(GLFW_KEY_LEFT_SHIFT.s, false),
        Pair(GLFW_KEY_LEFT_CONTROL.s, false),
        Pair(GLFW_KEY_LEFT_ALT.s, false),
        Pair(GLFW_KEY_LEFT_SUPER.s, false),
        Pair(GLFW_KEY_CAPS_LOCK.s, false),
        Pair(GLFW_KEY_NUM_LOCK.s, false),
    )

    fun setKeyBind(key: ResourceKey, bind: KeyBind){
        if(keyBinds[key] != null) keyBinds[key] = bind
        else{
            print("There is no keybinding for $key")
        }
    }

    @Throws(NoSuchElementException::class)
    fun getKeyBinding(key: ResourceKey): KeyBind{
        return keyBinds[key] ?: defaultKeyBinds[key] ?:
        throw NoSuchElementException("There is no key registered for $key")
    }

    fun getKeyBindingForKey(key: Short, type: ControlType): KeyBind?{
        return keyBinds.values.firstOrNull { bind -> bind.key == key && bind.controlType == type }
    }

    fun onInput(key: Short, scancode: Int, action: Int, mods: Byte, type: ControlType){
        // First check if the key is a mod key and if so update the mods map
        if(modStates.containsKey(key.s)) modStates[key.s] = action > 0

        val input = KeyBind(key, type, mods)
        val bind = getKeyBindingForKey(key, type)

        if(bind != null){
            // Set action if either the mods match or the key is being released
            if(input.matchMods(bind.mods) || action == 0) bind.state = action
        }

        keyPressCallback(input, action)
    }

    fun isActive(id: ResourceKey): Boolean{
        val bind = keyBinds.getOrNull(id) ?: return false
        return bind.state > 0
    }
    fun getMod(mod: Int) = modStates[mod.s] ?: false
    fun getMods() = modsToByte(
        getMod(GLFW_KEY_LEFT_SHIFT), getMod(GLFW_KEY_LEFT_CONTROL), getMod(GLFW_KEY_LEFT_ALT),
        getMod(GLFW_KEY_LEFT_SUPER), getMod(GLFW_KEY_CAPS_LOCK), getMod(GLFW_KEY_NUM_LOCK))

    fun modsToByte(shift: Boolean, control: Boolean, alt: Boolean, super_: Boolean, caps: Boolean, num: Boolean): Byte
        = (shift.b * 1 + control.b * 2 + alt.b * 4 + super_.b * 8 + caps.b * 16  + num.b * 32).b

    fun setCursorAt(pos: Vec2, drag: Boolean = false){
        this.mouse.setCursorAt(pos, drag)
    }

    enum class ControlType{
        KEYBOARD,
        MOUSE,
        JOYSTICK,
        GAMEPAD_BUTTON,
        GAMEPAD_AXIS
    }
}