package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.objects.menu_items.BasicTextButton
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.text.StretchyGameText
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.round
import com.pineypiney.game_engine.util.input.KeyBind
import com.pineypiney.game_engine.util.normal
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.PI

class Game(override val gameEngine: GameEngine): GameLogic() {
    override var camera: Camera = Camera()

    private val pressedKeys = mutableSetOf<Short>()

    private val button = BasicTextButton("button", Vec2(0.6, 0.8), Vec2(0.4, 0.2)){
        println("Pressed!")
    }

    private var text = SizedStaticText("X Part: 0.00 \n Y Part: 0.00")
    private val gameText = StretchyGameText("This is some Game Text", Vec2(17.78, 10))

    override val renderer: Renderer = Renderer()

    private var cycle = 0.0f

    private fun drawScene(tickDelta: Double){

        cycle += Timer.frameDelta.f

        backgroundShader.use()
        backgroundShader.setMat4("model", I.rotate(cycle * 2f * PI.f, normal))
        backgroundShader.setMat4("vp", I)
        ArrayShape.cornerSquareShape.bind()
        ArrayShape.cornerSquareShape.draw()

        gameText.render(renderer.view, renderer.projection, tickDelta)
        button.drawBottomLeft(Vec2(0.6, 0.8))
        text.drawCentered(Vec2())
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
        text = SizedStaticText("X Part: ${wp.x.round(2)} \n Y Part: ${wp.y.round(2)}", 100, Vec2(2))
    }

    override fun onInput(key: KeyBind, action: Int) {
        super.onInput(key, action)

        if(action == 0) pressedKeys.remove(key.key)
        else pressedKeys.add(key.key)
    }
    override fun addObjects() {
        add(button)
    }

    override fun updateAspectRatio(window: Window) {
        super.updateAspectRatio(window)
        text.updateAspectRatio(window)
    }

    override fun cleanUp() {
        super.cleanUp()
        text.delete()
        gameText.delete()
    }

    companion object{

        val backgroundShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/rainbow"))

    }
}