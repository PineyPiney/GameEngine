package com.pineypiney.game_engine.objects.menu_items

import com.pineypiney.game_engine.IGameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.KeyBind
import com.pineypiney.game_engine.util.text.Font
import com.pineypiney.game_engine.util.text.FontLoader
import glm_.c
import glm_.i
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import kotlin.math.abs

open class TextField(final override var origin: Vec2, final override val size: Vec2, textOffset: Float = -0.9f, textSize: Int = 2): StaticInteractableMenuItem() {

    open var parent: MenuItem? = null
    open var allowed = all.map { it.c }

    var textPos = relative(0, textOffset)
    var text: String = ""
        set(value) {
            field = value
            textBox = textBox.rewrite(field)
        }

    var textBox = TextFieldText(text, size.y * 100 * textSize, Vec2(origin.x, origin.x + size.x))

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

    override fun setUniforms() {
        super.setUniforms()
        shader.setVec3("colour", Vec3(0.5f))
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

    override fun onInput(game: IGameLogic, input: KeyBind, action: Int, cursorPos: Vec2): Int {
        super.onInput(game, input, action, cursorPos)

        if(input.controlType == Inputs.ControlType.KEYBOARD && this.forceUpdate){
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

    open fun specialCharacter(bind: KeyBind){
        when(bind.i){
            GLFW_KEY_ESCAPE -> {
                this.forceUpdate = false
            }
            GLFW_KEY_BACKSPACE -> {
                if(caret > 0){
                    val i: Int =
                        if(bind.control){
                            val lastSpace = this.text.substring(0, caret - 1).lastIndexOf(' ')
                            if(lastSpace >= 0) caret - lastSpace else caret
                        }
                        else 1
                    this.text = this.text.removeRange(caret - i, caret)
                    caret -= i
                }
            }
            GLFW_KEY_DELETE -> {
                if(caret < text.length){
                    val i: Int =
                        if(bind.control){
                            val lastSpace = this.text.substring(caret - 1).indexOf(' ', caret)
                            if(lastSpace >= 0) lastSpace else this.text.length - caret
                        }
                        else 1
                    this.text = this.text.removeRange(caret, caret + i)
                }
            }
            GLFW_KEY_LEFT -> {
                if(caret > 0){
                    val i: Int =
                        if(bind.control){
                            val lastSpace = this.text.substring(0, caret - 1).lastIndexOf(' ')
                            if(lastSpace >= 0) caret - lastSpace else caret
                        }
                        else 1
                    caret -= i
                }
            }
            GLFW_KEY_RIGHT -> {
                if(caret < text.length){
                    val i: Int =
                        if(bind.control){
                            val lastSpace = this.text.substring(caret - 1).indexOf(' ', caret)
                            if(lastSpace >= 0) lastSpace else this.text.length - caret
                        }
                        else 1
                    caret += i
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

    class TextFieldText(text: String, fontSize: Number, private var limits: Vec2,
                        bounds: Vec2 = Vec2(Float.MAX_VALUE),
                        colour: Vec4 = Vec4(1, 1, 1, 1),
                        font: Font = FontLoader.getFont(ResourceKey("fonts\\Large Font")),
                        shader: Shader = fieldShader, window: Window = Window.INSTANCE):
        SizedStaticText(text, fontSize.i, colour, bounds.x, bounds.y, 1f, font, shader, window) {

        override fun setUniversalUniforms(shader: Shader) {
            super.setUniversalUniforms(shader)
            shader.setVec2("limits", limits)
        }

        override fun setIndividualUniforms(shader: Shader, index: Int) {
            super.setIndividualUniforms(shader, index)
            val q = getQuad(index) ?: return
            val left = q.topLeft.x
            // This Vec2 contains the left of the texture and the width
            shader.setVec2("texture_section", Vec2(left, q.bottomRight.x - left))
        }

        fun rewrite(newText: String): TextFieldText{
            return TextFieldText(newText, fontSize, limits, Vec2(maxWidth, maxHeight), colour, font, shader, window)
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