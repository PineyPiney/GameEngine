package com.pineypiney.game_engine

import com.pineypiney.game_engine.cameras.Camera
import com.pineypiney.game_engine.objects.ScreenObjectCollection
import com.pineypiney.game_engine.objects.Text
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.renderers.BufferedGameRenderer
import com.pineypiney.game_engine.renderers.GameRenderer
import com.pineypiney.game_engine.resources.shaders.Shader
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.util.I
import com.pineypiney.game_engine.util.ResourceKey
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.opengl.GL11C.glClearColor

fun main() {

    val window = Window.INSTANCE
    window.setTitle("Example Window")

    // These variables must all be set after the game engine
    // has been initialised and all resources have been loaded
    var backgroundShader: Shader? = null
    var screenShader: Shader? = null
    var text: Text? = null

    val engine = object : GameEngine(window) {
        override var TARGET_FPS: Int = 1000
        override val TARGET_UPS: Int = 20

        override fun init() {
            super.init()
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
            text = Text("Text", colour = Vec4(1))
            backgroundShader = ShaderLoader.getShader(ResourceKey("vertex/pass_pos"), ResourceKey("fragment/rainbow"))
            screenShader = ShaderLoader.getShader(ResourceKey("vertex/frame_buffer"), ResourceKey("fragment/frame_buffer"))
        }

        override var activeScreen: IGameLogic = object : GameLogic(this){
            override var camera: Camera = Camera()
            override var renderer: GameRenderer = object : BufferedGameRenderer(){
                override fun render(window: Window, camera: Camera, game: IGameLogic, tickDelta: Double) {
                    clearFrameBuffer()
                    drawScene(game, tickDelta)

                    // This draws the buffer onto the screen
                    screenShader?.use()
                    screenShader?.setInt("effects", 0)
                    clearFrameBuffer(0)
                    drawBufferTexture()
                }
                fun drawScene(game: IGameLogic, tickDelta: Double){

                    game.gameObjects.forEachItem { it.render(vp, tickDelta) }

                    backgroundShader?.use()
                    backgroundShader?.setMat4("model", I.translate(Vec3(-1, -1, 0)).scale(Vec3(2)))
                    backgroundShader?.setMat4("vp", I)
                    ArrayShape.cornerSquareShape.bind()
                    ArrayShape.cornerSquareShape.draw()

                    text?.drawCentered(Vec2())

                }
                override fun updateAspectRatio(window: Window, objects: ScreenObjectCollection) {
                    super.updateAspectRatio(window, objects)
                    text?.updateAspectRatio(window)
                }
                override fun delete() {
                    super.delete()
                    text?.delete()
                }
            }
            override fun addObjects() {

            }
        }
    }

    engine.run()
}