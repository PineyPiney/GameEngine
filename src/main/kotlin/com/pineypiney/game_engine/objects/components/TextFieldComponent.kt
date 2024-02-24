package com.pineypiney.game_engine.objects.components

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.window.WindowI
import glm_.c
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class TextFieldComponent(parent: GameObject, textOffset: Float = 0f, textSize: Float = 1f): InteractorComponent(parent, "TXF"), UpdatingComponent {

    open var allowed = all

    private var textPos = Vec2(0f, textOffset)
    var text: String
        get() = textBox.getComponent<TextRendererComponent>()!!.text.text
        set(value) {
            textBox.getComponent<TextRendererComponent>()?.text?.text = value
            caret = max(min(caret, value.length), 0)
        }

    var textBox = object : MenuItem() {
        override fun addComponents() {
            super.addComponents()
            components.add(TextFieldText(this, Text("", fontSize = textSize), fieldShader))
        }
    }

    private var caretObject = object : MenuItem(){
        override fun addComponents() {
            super.addComponents()
            components.add(CaretRendererComponent(this, Vec4(.2f, .2f, .2f, 1f), translucentColourShader, VertexShape.cornerSquareShape))
        }

        override fun init() {
            super.init()
            scale = Vec3(0.01f / this@TextFieldComponent.parent.scale.x, 1f, 1f)
        }
    }

    var caret: Int = 0

    var limits = Vec2(); private set

    override fun init() {
        super.init()

        parent.addChild(textBox, caretObject)

        limits = Vec2(parent.transformComponent.worldPosition.x, parent.transformComponent.worldPosition.x + parent.transformComponent.worldScale.x)
    }

    override fun update(interval: Float) {
        caretObject.getComponent<RenderedComponent>()?.visible = this.forceUpdate && Timer.time % 1.0 > 0.5
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte, cursorPos: Vec2): Int {
        super.onPrimary(window, action, mods, cursorPos)
        if(!this.hover){
            finish()
        }
        else if(this.pressed){
            this.forceUpdate = true
            placeCaret(cursorPos, window)
        }
        return action
    }

    override fun onInput(window: WindowI, input: InputState, action: Int, cursorPos: Vec2): Int {
        super.onInput(window, input, action, cursorPos)

        if(input.controlType == ControlType.KEYBOARD && this.forceUpdate){
            if(action != GLFW_RELEASE) specialCharacter(input)
            return INTERRUPT
        }

        return action
    }

    override fun onType(window: WindowI, char: Char): Int {
        if(this.forceUpdate){
            if(allowed.contains(char)) type(char)
            return INTERRUPT
        }
        return 0
    }

    private fun paste(){
        val copy = (Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String).filter { allowed.contains(it) }
        text = text.substring(0, caret) + copy + text.substring(caret)
        caret += copy.length
    }

    open fun type(char: Char){
        text = text.substring(0, caret) + char + text.substring(caret)
        caret++
    }

    open fun specialCharacter(bind: InputState){
        when(bind.i){
            GLFW_KEY_V -> {
                if(bind.control) paste()
            }
            GLFW_KEY_ESCAPE -> {
                finish()
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

    private fun placeCaret(cursorPos: Vec2, window: WindowI){
        if(text.isEmpty()) {
            caret = 0
            return
        }
        var i = 1
        val textRenderer = textBox.getComponent<TextRendererComponent>()!!
        val relativeX = (cursorPos.x - textBox.transformComponent.worldPosition.x) / parent.transformComponent.worldScale.x
        val ar = (parent.getComponent<RenderedComponent>()!!.renderSize * Vec2(parent.transformComponent.worldScale)).run { (x * window.aspectRatio) / y }
        val scaledX = relativeX * ar
        while(i < text.length && (textRenderer.text.getWidth(text.substring(0, i)) < scaledX)) i++

        val pos1 = textRenderer.text.getWidth(text.substring(0, i - 1))
        val pos2 = textRenderer.text.getWidth(text.substring(0, i))

        caret = if(abs(scaledX - pos1) < abs(scaledX - pos2)) i - 1
        else i
    }

    private fun moveCaretLeft(place: Int, control: Boolean): Int{
        return if(control){
                val lastSpace = text.lastIndexOf(' ', place)
                if(lastSpace >= 0) lastSpace + 1 else 0
            }
            else caret - 1
    }

    private fun moveCaretRight(place: Int, control: Boolean): Int{
        return if(control){
            val nextSpace = text.indexOf(' ', place)
            if(nextSpace >= 0) nextSpace else text.length
        }
        else caret + 1
    }

    open fun finish(){
        forceUpdate = false
    }

    inner class TextFieldText(parent: GameObject, text: Text, shader: Shader):
        TextRendererComponent(parent, text, shader) {

        override fun setUniforms() {
            super.setUniforms()
            // Limit is in 0 to Window#width space so must be transformed
            uniforms.setVec2Uniform("limits", parent.parent?.getComponent<TextFieldComponent>()!!::limits)
        }
    }

    companion object{
        val fieldShader = ShaderLoader.getShader(ResourceKey("vertex/menu"), ResourceKey("fragment/text_field"))

        // A few sets of characters that might be limited in a textField.
        // Just override or set the 'allowed' variable
        val standard = listOf(GLFW_KEY_ESCAPE, GLFW_KEY_BACKSPACE, GLFW_KEY_DELETE, GLFW_KEY_LEFT, GLFW_KEY_RIGHT).map { it.c }
        val all = ((' '..254.c) - 127.c) + standard
        val integers = ('0'..'9') + standard + '-'
        val numbers = integers + '.'
        val upperAlphabet = ('A'..'Z') + standard
        val lowerAlphabet = ('a'..'z') + standard
        val alphabet = (lowerAlphabet + upperAlphabet).toSet()
    }
}