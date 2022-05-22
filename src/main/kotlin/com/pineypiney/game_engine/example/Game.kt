package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.BasicScrollList
import com.pineypiney.game_engine.objects.menu_items.slider.BasicSlider
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.text.StretchyGameText
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.round
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.normal
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW
import kotlin.math.PI

class Game(override val gameEngine: GameEngine): GameLogic() {

    override var camera: Camera = Camera()
    override val renderer: Renderer = Renderer()

    private val pressedKeys = mutableSetOf<Short>()

    private val button = TextButton("button", Vec2(0.6, 0.8), Vec2(0.4, 0.2)){
        println("Pressed!")
    }
    private val textField = ActionTextField(Vec2(-1), Vec2(1, 0.2)){ _, char, _ ->
        println("Typing $char")
    }
    private val slider = BasicSlider(Vec2(0.1, -0.9), Vec2(0.8, 0.1), 0f, 10f, 5f)

    private var text = SizedStaticText("X Part: 0.00 \n Y Part: 0.00")
    private val gameText = StretchyGameText("This is some Game Text", Vec2(17.78, 10), Vec4(0.0, 1.0, 1.0, 1.0))

    private val list = BasicScrollList(Vec2(-1, -0.2), Vec2(1.2), 1f, 0.05f, arrayOf("Hello", "World"))

    private var cycle = 0.0f

    override fun init() {
        super.init()
        text.init()
        gameText.init()
    }

    override fun addObjects() {
        add(button)
        add(textField)
        add(slider)
        add(list)
    }

    private fun drawScene(tickDelta: Double){

        cycle += Timer.frameDelta.f

        backgroundShader.use()
        backgroundShader.setMat4("model", I.rotate(cycle * 2f * PI.f, normal))
        backgroundShader.setMat4("vp", I)
        ArrayShape.cornerSquareShape3D.bind()
        ArrayShape.cornerSquareShape3D.draw()

        gameText.render(renderer.view, renderer.projection, tickDelta)
        button.drawBottomLeft(Vec2(0.6, 0.8))
        text.drawCentered(Vec2())
        textField.drawBottomLeft(Vec2(-1))
        slider.draw()
        list.draw()
    }

    override fun render(window: Window, tickDelta: Double) {
        super.render(window, tickDelta)
        drawScene(tickDelta)

        val speed = 10
        if(pressedKeys.contains('W'.s)) this.camera.setPos(this.camera.cameraPos + Vec3(0, 1, 0) * Timer.frameDelta * speed)
        if(pressedKeys.contains('S'.s)) this.camera.setPos(this.camera.cameraPos - Vec3(0, 1, 0) * Timer.frameDelta * speed)
        if(pressedKeys.contains('A'.s)) this.camera.setPos(this.camera.cameraPos - Vec3(1, 0, 0) * Timer.frameDelta * speed)
        if(pressedKeys.contains('D'.s)) this.camera.setPos(this.camera.cameraPos + Vec3(1, 0, 0) * Timer.frameDelta * speed)
    }

    override fun onCursorMove(window: Window, cursorPos: Vec2, cursorDelta: Vec2) {
        super.onCursorMove(window, cursorPos, cursorDelta)
        val wp = camera.screenToWorld(cursorPos)
        text.text = "X Part: ${wp.x.round(2)} \n Y Part: ${wp.y.round(2)}"
    }

    override fun onInput(key: InputState, action: Int): Int {
        if(super.onInput(key, action) == Interactable.INTERRUPT) return Interactable.INTERRUPT

        if(key.c == 'F' && action == 1){
            toggleFullscreen()
        }
        if(key.i == GLFW.GLFW_KEY_ESCAPE && action == 1){
            window.setShouldClose()
        }

        if(action == 0) pressedKeys.remove(key.key)
        else pressedKeys.add(key.key)
        return action
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
        textField.updateAspectRatio(window)
    }

    override fun cleanUp() {
        super.cleanUp()
        text.delete()
        gameText.delete()
        textField.delete()
    }

    companion object{

        val backgroundShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/rainbow"))

    }
}