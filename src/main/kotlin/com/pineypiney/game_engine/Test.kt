package com.pineypiney.game_engine

import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.menu_items.BasicTextButton
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.text.StretchyGameText
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.renderers.BufferedGameRenderer
import com.pineypiney.game_engine.renderers.GameRenderer
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
import org.lwjgl.opengl.GL11C.glClearColor
import kotlin.math.PI

fun main() {

    val window = Window.INSTANCE
    window.setTitle("Example Window")

    var cycle = 0.0f

    val engine = object : GameEngine(window) {
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20

        override fun init() {
            super.init()
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        }

        override var activeScreen: IGameLogic = object : GameLogic(this){
            override var camera: Camera = Camera()

            val pressedKeys = mutableSetOf<Short>()

            var button = BasicTextButton("button", Vec2(0.6, 0.8), Vec2(0.4, 0.2)){
                println("Pressed!")
            }

            var text = SizedStaticText("X Part: 0.00 \n Y Part: 0.00")
            val gameText = StretchyGameText("This is some Game Text", Vec2(17.78, 10))

            val backgroundShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/rainbow"))
            val screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))

            override var renderer: GameRenderer = object : BufferedGameRenderer(){

                var view = I
                var projection = I

                override fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double) {

                    view = camera.getViewMatrix()
                    projection = getPerspective(window, camera)

                    clearFrameBuffer()
                    drawScene(window, game, tickDelta)

                    // This draws the buffer onto the screen
                    screenShader.use()
                    screenShader.setInt("effects", 0)
                    clearFrameBuffer(0)
                    drawBufferTexture()

                    button.drawBottomLeft(Vec2(0.6, 0.8))
                    gameText.render(view, projection, tickDelta)
                }
                fun drawScene(window: Window, game: IGameLogic, tickDelta: Double){

                    game.gameObjects.forEachItem { it.render(camera.getViewMatrix(), getPerspective(window, camera), tickDelta) }

                    cycle += Timer.frameDelta.f

                    backgroundShader.use()
                    backgroundShader.setMat4("model", I.rotate(cycle * 2f * PI.f, normal))
                    backgroundShader.setMat4("vp", I)
                    ArrayShape.cornerSquareShape.bind()
                    ArrayShape.cornerSquareShape.draw()

                    text.drawCentered(Vec2())

                }
                override fun updateAspectRatio(window: Window, objects: ScreenObjectCollection) {
                    super.updateAspectRatio(window, objects)
                    text.updateAspectRatio(window)
                }
                override fun delete() {
                    super.delete()
                    text.delete()
                    gameText.delete()
                }
            }

            override fun render(window: Window, tickDelta: Double) {
                super.render(window, tickDelta)

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
        }
    }

    engine.run()
}