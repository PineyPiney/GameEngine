package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.game_objects.ModelledGameObject
import com.pineypiney.game_engine.objects.game_objects.SimpleTexturedGameObject
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.BasicScrollList
import com.pineypiney.game_engine.objects.menu_items.slider.BasicSlider
import com.pineypiney.game_engine.objects.text.SizedGameText
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.text.StretchyGameText
import com.pineypiney.game_engine.rendering.cameras.Camera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.roundedString
import com.pineypiney.game_engine.util.input.InputState
import glm_.s
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW

class Game(override val gameEngine: GameEngine): GameLogic() {

    override var camera: Camera = Camera(window)
    override val renderer: Renderer = Renderer(window)

    private val pressedKeys = mutableSetOf<Short>()

    private val button = TextButton("button", Vec2(0.6, 0.8), Vec2(0.4, 0.2), window){
        println("Pressed!")
    }
    private val textField = ActionTextField(Vec2(-1), Vec2(1, 0.2), window){ _, char, _ ->
        println("Typing $char")
    }
    private val slider = BasicSlider(Vec2(0.1, -0.9), Vec2(0.8, 0.1), 0f, 10f, 5f, window)

    private val texture = SimpleTexturedGameObject(ResourceKey("texture"), ResourceKey("menu_items/slider/pointer"))
    private val model1 = ModelledGameObject(ResourceKey("goblin"))
    private val model2 = ModelledGameObject(ResourceKey("goblin"))

    private val text = SizedStaticText("X Part: 0.00 \n Y Part: 0.00", window)
    private val gameText = StretchyGameText("This is some Game Text", Vec2(17.78, 10), Vec4(0.0, 1.0, 1.0, 1.0))
    private val siGameText = SizedGameText("This is some Sized Game Text", 300, Vec2(11, 10), Vec4(0.0, 1.0, 1.0, 1.0))

    private val list = BasicScrollList(Vec2(-1, -0.2), Vec2(1.2), 1f, 0.05f, arrayOf("Hello", "World"), window)

    private var cycle = 0.0f

    override fun init() {
        super.init()
        text.init()
        gameText.init()
        siGameText.init()

        model1.setAnimation("Wipe Nose")
        model2.setAnimation("Magic Trick")
        model1.translate(Vec2(2, -5))
        model2.translate(Vec2(5, -5))
    }

    override fun addObjects() {
        add(texture)
        add(model1)
        add(model2)

        add(button)
        add(textField)
        add(slider)
        add(list)
    }

    private fun drawScene(tickDelta: Double){

        gameText.render(renderer.view, renderer.projection, tickDelta)
        siGameText.render(renderer.view, renderer.projection, tickDelta)
        text.drawCentered(Vec2())
        button.drawBottomLeft(Vec2(0.6, 0.8))
        textField.drawBottomLeft(Vec2(-1))
        slider.draw()
        list.draw()

        /*
        cycle += Timer.frameDelta.f

        backgroundShader.use()
        backgroundShader.setMat4("model", I.rotate(cycle * 2f * PI.f, normal))
        backgroundShader.setMat4("view", I)
        backgroundShader.setMat4("projection", I)
        Shape.cornerSquareShape2D.bind()
        Shape.cornerSquareShape2D.draw()

         */
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
        text.text = wp.roundedString(2).let { "X Part: ${it[0]} \n Y Part: ${it[1]}" }
    }

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == Interactable.INTERRUPT) return Interactable.INTERRUPT

        if(state.c == 'F' && action == 1){
            toggleFullscreen()
        }
        if(state.i == GLFW.GLFW_KEY_ESCAPE && action == 1){
            window.setShouldClose()
        }

        if(action == 0) pressedKeys.remove(state.key)
        else pressedKeys.add(state.key)
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
        siGameText.delete()
        textField.delete()
    }

    companion object{

        val backgroundShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/rainbow"))

    }
}