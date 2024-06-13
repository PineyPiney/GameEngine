package com.pineypiney.game_engine.util.input

import glm_.*
import org.lwjgl.glfw.GLFW.*

class InputState(val key: Short, val controlType: ControlType = ControlType.KEYBOARD, val mods: Byte = 0) {

    constructor(key: Number, controlType: ControlType = ControlType.KEYBOARD, mods: Number = 0) : this(key.s, controlType, mods.b)
    constructor(key: Char, mods: Number = 0, controlType: ControlType = ControlType.KEYBOARD): this(key.s, controlType, mods)

    val c; get() = key.c
    val i; get() = key.i

    val shift get() = (this.mods and GLFW_MOD_SHIFT) > 0
    val control get() = (this.mods and GLFW_MOD_CONTROL) > 0
    val alt get() = (this.mods and GLFW_MOD_ALT) > 0
    val super_ get() = (this.mods and GLFW_MOD_SUPER) > 0
    val caps get() = (this.mods and GLFW_MOD_CAPS_LOCK) > 0
    val num get() = (this.mods and GLFW_MOD_NUM_LOCK) > 0

    infix fun triggers(other: InputState): Boolean{
        return if(other.mods == 0.b) this.removeMods() == other
        else this == other
    }

    fun removeMods() = InputState(key, controlType)

    override fun equals(other: Any?): Boolean {
        (other as? InputState)?.let {
            return other.key == this.key &&
                    other.controlType == this.controlType &&
                    other.mods == this.mods
        }
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode * controlType.hashCode() * mods.hashCode
    }

    override fun toString(): String {
        var string = ""
        if(this.control) string += "Ctrl + "
        if(this.shift) string += "Shift + "
        if(this.alt) string += "Alt + "
        if(this.super_) string += "Super + "
        if(this.caps) string += "Caps + "
        if(this.num) string += "Num + "

        string += when(controlType) {
            ControlType.KEYBOARD -> keyNames[this.key] ?: "Unknown"
            ControlType.MOUSE -> mouseKeyNames[this.key] ?: "Unknown"
            ControlType.GAMEPAD_BUTTON -> gamepadKeyNames[this.key] ?: "Unknown"
            ControlType.GAMEPAD_AXIS -> gamepadAxisKeyNames[this.key] ?: "Unknown"
        }
        return string
    }

    companion object{

        fun shift(mods: Byte) = (mods and GLFW_MOD_SHIFT) > 0
        fun control(mods: Byte) = (mods and GLFW_MOD_CONTROL) > 0
        fun alt(mods: Byte) = (mods and GLFW_MOD_ALT) > 0
        fun super_(mods: Byte) = (mods and GLFW_MOD_SUPER) > 0
        fun caps(mods: Byte) = (mods and GLFW_MOD_CAPS_LOCK) > 0
        fun num(mods: Byte) = (mods and GLFW_MOD_NUM_LOCK) > 0

        val keyNames: Map<Short, String> = mapOf(
            Pair(GLFW_KEY_SPACE         , "SPACE"),
            Pair(GLFW_KEY_APOSTROPHE    , "APOSTROPHE"),
            Pair(GLFW_KEY_COMMA         , "COMMA"),
            Pair(GLFW_KEY_MINUS         , "MINUS"),
            Pair(GLFW_KEY_PERIOD        , "PERIOD"),
            Pair(GLFW_KEY_SLASH         , "SLASH"),
            Pair(GLFW_KEY_0             , "0"),
            Pair(GLFW_KEY_1             , "1"),
            Pair(GLFW_KEY_2             , "2"),
            Pair(GLFW_KEY_3             , "3"),
            Pair(GLFW_KEY_4             , "4"),
            Pair(GLFW_KEY_5             , "5"),
            Pair(GLFW_KEY_6             , "6"),
            Pair(GLFW_KEY_7             , "7"),
            Pair(GLFW_KEY_8             , "8"),
            Pair(GLFW_KEY_9             , "9"),
            Pair(GLFW_KEY_SEMICOLON     , "SEMICOLON"),
            Pair(GLFW_KEY_EQUAL         , "EQUAL"),
            Pair(GLFW_KEY_A             , "A"),
            Pair(GLFW_KEY_B             , "B"),
            Pair(GLFW_KEY_C             , "C"),
            Pair(GLFW_KEY_D             , "D"),
            Pair(GLFW_KEY_E             , "E"),
            Pair(GLFW_KEY_F             , "F"),
            Pair(GLFW_KEY_G             , "G"),
            Pair(GLFW_KEY_H             , "H"),
            Pair(GLFW_KEY_I             , "I"),
            Pair(GLFW_KEY_J             , "J"),
            Pair(GLFW_KEY_K             , "K"),
            Pair(GLFW_KEY_L             , "L"),
            Pair(GLFW_KEY_M             , "M"),
            Pair(GLFW_KEY_N             , "N"),
            Pair(GLFW_KEY_O             , "O"),
            Pair(GLFW_KEY_P             , "P"),
            Pair(GLFW_KEY_Q             , "Q"),
            Pair(GLFW_KEY_R             , "R"),
            Pair(GLFW_KEY_S             , "S"),
            Pair(GLFW_KEY_T             , "T"),
            Pair(GLFW_KEY_U             , "U"),
            Pair(GLFW_KEY_V             , "V"),
            Pair(GLFW_KEY_W             , "W"),
            Pair(GLFW_KEY_X             , "X"),
            Pair(GLFW_KEY_Y             , "Y"),
            Pair(GLFW_KEY_Z             , "Z"),
            Pair(GLFW_KEY_LEFT_BRACKET  , "LEFT BRACKET"),
            Pair(GLFW_KEY_BACKSLASH     , "BACKSLASH"),
            Pair(GLFW_KEY_RIGHT_BRACKET , "RIGHT BRACKET"),
            Pair(GLFW_KEY_GRAVE_ACCENT  , "GRAVE ACCENT"),
            Pair(GLFW_KEY_WORLD_1       , "WORLD 1"),
            Pair(GLFW_KEY_WORLD_2       , "WORLD 2"),

            Pair(GLFW_KEY_ESCAPE        , "ESCAPE"),
            Pair(GLFW_KEY_ENTER         , "ENTER"),
            Pair(GLFW_KEY_TAB           , "TAB"),
            Pair(GLFW_KEY_BACKSPACE     , "BACKSPACE"),
            Pair(GLFW_KEY_INSERT        , "INSERT"),
            Pair(GLFW_KEY_DELETE        , "DELETE"),
            Pair(GLFW_KEY_RIGHT         , "RIGHT"),
            Pair(GLFW_KEY_LEFT          , "LEFT"),
            Pair(GLFW_KEY_DOWN          , "DOWN"),
            Pair(GLFW_KEY_UP            , "UP"),
            Pair(GLFW_KEY_PAGE_UP       , "PAGE UP"),
            Pair(GLFW_KEY_PAGE_DOWN     , "PAGE DOWN"),
            Pair(GLFW_KEY_HOME          , "HOME"),
            Pair(GLFW_KEY_END           , "END"),
            Pair(GLFW_KEY_CAPS_LOCK     , "CAPS LOCK"),
            Pair(GLFW_KEY_SCROLL_LOCK   , "SCROLL LOCK"),
            Pair(GLFW_KEY_NUM_LOCK      , "NUM LOCK"),
            Pair(GLFW_KEY_PRINT_SCREEN  , "PRINT SCREEN"),
            Pair(GLFW_KEY_PAUSE         , "PAUSE"),
            Pair(GLFW_KEY_F1            , "F1"),
            Pair(GLFW_KEY_F2            , "F2"),
            Pair(GLFW_KEY_F3            , "F3"),
            Pair(GLFW_KEY_F4            , "F4"),
            Pair(GLFW_KEY_F5            , "F5"),
            Pair(GLFW_KEY_F6            , "F6"),
            Pair(GLFW_KEY_F7            , "F7"),
            Pair(GLFW_KEY_F8            , "F8"),
            Pair(GLFW_KEY_F9            , "F9"),
            Pair(GLFW_KEY_F10           , "F10"),
            Pair(GLFW_KEY_F11           , "F11"),
            Pair(GLFW_KEY_F12           , "F12"),
            Pair(GLFW_KEY_F13           , "F13"),
            Pair(GLFW_KEY_F14           , "F14"),
            Pair(GLFW_KEY_F15           , "F15"),
            Pair(GLFW_KEY_F16           , "F16"),
            Pair(GLFW_KEY_F17           , "F17"),
            Pair(GLFW_KEY_F18           , "F18"),
            Pair(GLFW_KEY_F19           , "F19"),
            Pair(GLFW_KEY_F20           , "F20"),
            Pair(GLFW_KEY_F21           , "F21"),
            Pair(GLFW_KEY_F22           , "F22"),
            Pair(GLFW_KEY_F23           , "F23"),
            Pair(GLFW_KEY_F24           , "F24"),
            Pair(GLFW_KEY_F25           , "F25"),
            Pair(GLFW_KEY_KP_0          , "KP_0"),
            Pair(GLFW_KEY_KP_1          , "KP_1"),
            Pair(GLFW_KEY_KP_2          , "KP_2"),
            Pair(GLFW_KEY_KP_3          , "KP_3"),
            Pair(GLFW_KEY_KP_4          , "KP_4"),
            Pair(GLFW_KEY_KP_5          , "KP_5"),
            Pair(GLFW_KEY_KP_6          , "KP_6"),
            Pair(GLFW_KEY_KP_7          , "KP_7"),
            Pair(GLFW_KEY_KP_8          , "KP_8"),
            Pair(GLFW_KEY_KP_9          , "KP_9"),
            Pair(GLFW_KEY_KP_DECIMAL    , "KP_DECIMAL"),
            Pair(GLFW_KEY_KP_DIVIDE     , "KP_DIVIDE"),
            Pair(GLFW_KEY_KP_MULTIPLY   , "KP_MULTIPLY"),
            Pair(GLFW_KEY_KP_SUBTRACT   , "KP_SUBTRACT"),
            Pair(GLFW_KEY_KP_ADD        , "KP_ADD"),
            Pair(GLFW_KEY_KP_ENTER      , "KP_ENTER"),
            Pair(GLFW_KEY_KP_EQUAL      , "KP_EQUAL"),
            Pair(GLFW_KEY_LEFT_SHIFT    , "LEFT SHIFT"),
            Pair(GLFW_KEY_LEFT_CONTROL  , "LEFT CONTROL"),
            Pair(GLFW_KEY_LEFT_ALT      , "LEFT ALT"),
            Pair(GLFW_KEY_LEFT_SUPER    , "LEFT SUPER"),
            Pair(GLFW_KEY_RIGHT_SHIFT   , "RIGHT SHIFT"),
            Pair(GLFW_KEY_RIGHT_CONTROL , "RIGHT CONTROL"),
            Pair(GLFW_KEY_RIGHT_ALT     , "RIGHT ALT"),
            Pair(GLFW_KEY_RIGHT_SUPER   , "RIGHT SUPER"),
            Pair(GLFW_KEY_MENU          , "MENU"),
        ).mapKeys { it.key.s }

        // Mouse Control Names
        val mouseKeyNames: Map<Short, String> = mapOf(
            Pair(GLFW_MOUSE_BUTTON_1, "Mouse 1"),
            Pair(GLFW_MOUSE_BUTTON_2, "Mouse 2"),
            Pair(GLFW_MOUSE_BUTTON_3, "Mouse 3"),
            Pair(GLFW_MOUSE_BUTTON_4, "Mouse 4"),
            Pair(GLFW_MOUSE_BUTTON_5, "Mouse 5"),
            Pair(GLFW_MOUSE_BUTTON_6, "Mouse 6"),
            Pair(GLFW_MOUSE_BUTTON_7, "Mouse 7"),
            Pair(GLFW_MOUSE_BUTTON_8, "Mouse 8"),
        ).mapKeys { it.key.s }

        // Joystick Control Names
        val joystickKeyNames: Map<Short, String> = mapOf(
            Pair(GLFW_JOYSTICK_1  , "Joystick 1"),
            Pair(GLFW_JOYSTICK_2  , "Joystick 2"),
            Pair(GLFW_JOYSTICK_3  , "Joystick 3"),
            Pair(GLFW_JOYSTICK_4  , "Joystick 4"),
            Pair(GLFW_JOYSTICK_5  , "Joystick 5"),
            Pair(GLFW_JOYSTICK_6  , "Joystick 6"),
            Pair(GLFW_JOYSTICK_7  , "Joystick 7"),
            Pair(GLFW_JOYSTICK_8  , "Joystick 8"),
            Pair(GLFW_JOYSTICK_9  , "Joystick 9"),
            Pair(GLFW_JOYSTICK_10 , "Joystick 10"),
            Pair(GLFW_JOYSTICK_11 , "Joystick 11"),
            Pair(GLFW_JOYSTICK_12 , "Joystick 12"),
            Pair(GLFW_JOYSTICK_13 , "Joystick 13"),
            Pair(GLFW_JOYSTICK_14 , "Joystick 14"),
            Pair(GLFW_JOYSTICK_15 , "Joystick 15"),
            Pair(GLFW_JOYSTICK_16 , "Joystick 16"),
        ).mapKeys { it.key.s }

        // Game Pad Control Names
        val gamepadKeyNames: Map<Short, String> = mapOf(
            Pair(GLFW_GAMEPAD_BUTTON_A            , "GAMEPAD_BUTTON_A"),
            Pair(GLFW_GAMEPAD_BUTTON_B            , "GAMEPAD_BUTTON_B"),
            Pair(GLFW_GAMEPAD_BUTTON_X            , "GAMEPAD_BUTTON_X"),
            Pair(GLFW_GAMEPAD_BUTTON_Y            , "GAMEPAD_BUTTON_Y"),
            Pair(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER  , "GAMEPAD_BUTTON_LEFT_BUMPER"),
            Pair(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER , "GAMEPAD_BUTTON_RIGHT_BUMPER"),
            Pair(GLFW_GAMEPAD_BUTTON_BACK         , "GAMEPAD_BUTTON_BACK"),
            Pair(GLFW_GAMEPAD_BUTTON_START        , "GAMEPAD_BUTTON_START"),
            Pair(GLFW_GAMEPAD_BUTTON_GUIDE        , "GAMEPAD_BUTTON_GUIDE"),
            Pair(GLFW_GAMEPAD_BUTTON_LEFT_THUMB   , "GAMEPAD_BUTTON_LEFT_THUMB"),
            Pair(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB  , "GAMEPAD_BUTTON_RIGHT_THUMB"),
            Pair(GLFW_GAMEPAD_BUTTON_DPAD_UP      , "GAMEPAD_BUTTON_DPAD_UP"),
            Pair(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT   , "GAMEPAD_BUTTON_DPAD_RIGHT"),
            Pair(GLFW_GAMEPAD_BUTTON_DPAD_DOWN    , "GAMEPAD_BUTTON_DPAD_DOWN"),
            Pair(GLFW_GAMEPAD_BUTTON_DPAD_LEFT    , "GAMEPAD_BUTTON_DPAD_LEFT")
        ).mapKeys { it.key.s }

        // Gamepad Axis Control Names
        val gamepadAxisKeyNames: Map<Short, String> = mapOf(
            Pair(GLFW_GAMEPAD_AXIS_LEFT_X        ,  "GAMEPAD_AXIS_LEFT_X"),
            Pair(GLFW_GAMEPAD_AXIS_LEFT_Y        ,  "GAMEPAD_AXIS_LEFT_Y"),
            Pair(GLFW_GAMEPAD_AXIS_RIGHT_X       ,  "GAMEPAD_AXIS_RIGHT_X"),
            Pair(GLFW_GAMEPAD_AXIS_RIGHT_Y       ,  "GAMEPAD_AXIS_RIGHT_Y"),
            Pair(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER  ,  "GAMEPAD_AXIS_LEFT_TRIGGER"),
            Pair(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER ,  "GAMEPAD_AXIS_RIGHT_TRIGGER"),
        ).mapKeys { it.key.s }
    }
}