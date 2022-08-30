package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.text.BitMapFont
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.I
import glm_.c
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import kotlin.math.abs

open class TextField(final override var origin: Vec2, final override val size: Vec2, window: Window, textOffset: Float = -0.9f, textSize: Int = 2): StaticInteractableMenuItem() {

    open var allowed = all.map { it.c }

    var textPos = relative(0, textOffset)
    var text: String = ""
        set(value) {
            field = value
            textBox.text = text
        }

    var textBox = TextFieldText(text, window, size.y * 100 * textSize, Vec2(origin.x, origin.x + size.x))

    var caret: Int = 0
        set(value) {
            field = value
            caretDist = textBox.getScreenSize(text.substring(0, value)).x
        }
    var caretDist = 0f
        set(value) {
            field = if(value > size.x){
                textPos.x = origin.x + size.x - value
                size.x
            }
            else if(value < 0){
                textPos.x = origin.x - value
                0f
            }
            else{
                textPos.x = origin.x
                value
            }
        }

    override fun init() {
        super.init()
        textBox.init()
    }

    override fun setUniforms() {
        super.setUniforms()

        uniforms.setVec3Uniform("colour"){ Vec3(0.5f) }
    }

    override fun draw() {
        // Draw Background
        super.draw()

        // Draw Caret
        if(this.forceUpdate && Timer.time % 1 > 0.5){


            shader.setMat4("model", I.translate(Vec3(origin.x + caretDist, origin.y, 1)).scale(Vec3(0.01f, size.y, 1)))
            shader.setVec3("colour", Vec3(0.2f))

            shape.draw()
        }

        // Draw Text
        textBox.drawBottomLeft(textPos)
    }

    override fun onPrimary(game: IGameLogic, action: Int, mods: Byte, cursorPos: Vec2): Int {
        super.onPrimary(game, action, mods, cursorPos)
        if(!this.hover){
            this.forceUpdate = false
        }
        else if(this.pressed){
            this.forceUpdate = true
            placeCaret(cursorPos)
        }
        return action
    }

    override fun onInput(game: IGameLogic, input: InputState, action: Int, cursorPos: Vec2): Int {
        super.onInput(game, input, action, cursorPos)

        if(input.controlType == ControlType.KEYBOARD && this.forceUpdate){
            if(action != GLFW_RELEASE) specialCharacter(input)
            return Interactable.INTERRUPT
        }

        return action
    }

    override fun onType(game: IGameLogic, char: Char): Int {
        if(this.forceUpdate){
            if(allowed.contains(char)) type(char)
            return Interactable.INTERRUPT
        }
        return 0
    }

    open fun type(char: Char){
        text = text.substring(0, caret) + char + text.substring(caret)
        caret++
    }

    open fun specialCharacter(bind: InputState){
        when(bind.i){
            GLFW_KEY_ESCAPE -> {
                this.forceUpdate = false
            }
            GLFW_KEY_BACKSPACE -> {
                if(caret > 0){
                    val i: Int = moveCaretLeft(caret - 2, bind.control)
                    text = text.removeRange(i, caret)
                    caret = i
                }
            }
            GLFW_KEY_DELETE -> {
                if(caret < text.length){
                    val i: Int = moveCaretRight(caret + 1, bind.control)
                    text = text.removeRange(caret, i)
                }
            }
            GLFW_KEY_LEFT -> {
                if(caret > 0){
                    caret = moveCaretLeft(caret - 2, bind.control)
                }
            }
            GLFW_KEY_RIGHT -> {
                if(caret < text.length){
                    caret = moveCaretRight(caret + 1, bind.control)
                }
            }
            GLFW_KEY_HOME -> {
                if(caret > 0){
                    caret = 0
                }
            }
            GLFW_KEY_END -> {
                if(caret < text.length){
                    caret = text.length
                }
            }
        }
    }

    fun placeCaret(cursorPos: Vec2){
        if(text.isEmpty()) {
            caret = 0
            return
        }
        var i = 0
        val relativeX = cursorPos.x - textPos.x
        while(i < text.length && (textBox.getScreenSize(text.substring(0, i)).x < relativeX)) i++

        val pos1 = textBox.getScreenSize(text.substring(0, i - 1)).x
        val pos2 = textBox.getScreenSize(text.substring(0, i)).x

        caret = if(abs(relativeX - pos1) < abs(relativeX - pos2)) i - 1
        else i
    }

    fun moveCaretLeft(place: Int, control: Boolean): Int{
        return if(control){
                val lastSpace = text.lastIndexOf(' ', place)
                if(lastSpace >= 0) lastSpace + 1 else 0
            }
            else caret - 1
    }

    fun moveCaretRight(place: Int, control: Boolean): Int{
        return if(control){
            val nextSpace = text.indexOf(' ', place)
            if(nextSpace >= 0) nextSpace else text.length
        }
        else caret + 1
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        textBox.updateAspectRatio(window)
    }

    class TextFieldText(text: String, window: Window, fontSize: Number, private var limits: Vec2,
                        bounds: Vec2 = Vec2(Float.MAX_VALUE),
                        colour: Vec4 = Vec4(1, 1, 1, 1),
                        font: BitMapFont = BitMapFont.defaultFont,
                        shader: Shader = fieldShader):
        SizedStaticText(text, window, fontSize.i, colour, bounds.x, bounds.y, 1f, font, shader) {

        override fun setUniforms() {
            super.setUniforms()
            uniforms.setVec2Uniform("limits"){ limits }
        }

        override fun setIndividualUniforms(shader: Shader, index: Int) {
            super.setIndividualUniforms(shader, index)
            val q = getQuad(index) ?: return
            val left = q.topLeft.x
            // This Vec2 contains the left of the texture and the width
            shader.setVec2("texture_section", Vec2(left, q.bottomRight.x - left))
        }
    }

    companion object{
        val fieldShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/text_field"))

        // A few sets of characters that might be limited in a textField.
        // Just override or set the 'allowed' variable
        val standard = listOf(GLFW_KEY_ESCAPE, GLFW_KEY_BACKSPACE, GLFW_KEY_DELETE, GLFW_KEY_LEFT, GLFW_KEY_RIGHT)
        val all = ((32..254) - 127) + standard
        val integers = (48..57) + standard
        val numbers = integers + '.'.i
        val upperAlphabet = (65..90) + standard
        val lowerAlphabet = (97..122) + standard
        val alphabet = (lowerAlphabet + upperAlphabet).toSet()
    }
}