package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.audio.AudioEngine
import com.pineypiney.game_engine.audio.AudioSource
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.ColourSquare
import com.pineypiney.game_engine.objects.game_objects.objects_2D.ModelledGameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_2D.SimpleTexturedGameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_3D.SimpleTexturedGameObject3D
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.menu_items.VideoPlayer
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.BasicScrollList
import com.pineypiney.game_engine.objects.menu_items.slider.ColourSlider
import com.pineypiney.game_engine.objects.text.SizedGameText
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.text.SizedText
import com.pineypiney.game_engine.objects.text.StretchyGameText
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.video.VideoLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.extension_functions.roundedString
import com.pineypiney.game_engine.util.extension_functions.wrap
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Rect3D
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sign

class Game(override val gameEngine: GameEngineI<*>): GameLogic() {

    override val camera: OrthographicCamera = OrthographicCamera(window)
    override val renderer: Renderer = Renderer(window)

    private val pressedKeys = mutableSetOf<Short>()
    private val audio get() = AudioLoader[(ResourceKey("clair_de_lune"))]

    private val b = TextButton("Button", Vec2(-0.3, 0.6), Vec2(0.6, 0.2), window){
        val device = AudioEngine.getAllOutputDevices()[(0..1).random()]
        window.setAudioOutput(device)
        GameEngineI.logger.info("Setting audio out device to $device")

        window.vSync = !window.vSync
        println("Window vSync: ${window.vSync}")
    }
    private val bc = Rect2D(b.origin, 0.6f, 0.2f)
    private val cursorSquare = ColourSquare(size = Vec2(0.1f, 0.1f * window.aspectRatio))

    private val button = TextButton("button", Vec2(0.6, 0.8), Vec2(0.4, 0.2), window){
        println("Pressed!")
        AudioSource(audio).play()
        window.setCursor(intArrayOf(GLFW_RESIZE_EW_CURSOR, GLFW_RESIZE_NESW_CURSOR, GLFW_RESIZE_ALL_CURSOR, GLFW_NOT_ALLOWED_CURSOR).random())
    }
    private val textField = ActionTextField(Vec2(-1), Vec2(1, 0.2), window){ _, char, _ ->
        println("Typing $char")
        AudioSource(audio).play()
        window.setCursor(0L)
    }
    private val slider = ColourSlider(Vec2(0.1, -0.9), Vec2(0.8, 0.1), window, ColourSlider.redShader, mutableMapOf("green" to 0.5f, "blue" to 0.5f))

    private val texture = SimpleTexturedGameObject2D(Texture.broke)
    private val model1 = ModelledGameObject2D(ModelLoader.getModel(ResourceKey("goblin")), Model.DEBUG_COLLIDER)
    private val model2 = ModelledGameObject2D(ModelLoader.getModel(ResourceKey("goblin")), Model.DEBUG_COLLIDER)

    private val object3D = SimpleTexturedGameObject3D(ResourceKey("broke"))

    private val text = SizedStaticText("X Part: 0.00 \nY Part: 0.00", window, 16, Vec2(0.5, 0.2))
    private val gameText = StretchyGameText("This is some Stretchy Game Text", Vec2(8.88, 10), Vec4(0.0, 1.0, 1.0, 1.0))
    private val siGameText = SizedGameText("This is some Sized Game Text", 100, Vec2(7, 10), Vec4(0.0, 1.0, 1.0, 1.0)).apply { alignment = SizedText.ALIGN_CENTER }

    private val list = BasicScrollList(Vec2(-1, 0.4), Vec2(0.6), 1f, 0.05f, arrayOf("Hello", "World"), window)

    val video = VideoPlayer(VideoLoader[ResourceKey("ghost"), gameEngine.resourcesLoader], Vec2(0.5, -0.15), Vec2(0.5, 0.3))

    override fun init() {
        super.init()
        text.init()

        gameText.transform.position = Vec2(0, 2)

        gameText.init()
        siGameText.init()
//        video.init()
//        video.play()
//        video.video.loop = true

        model1.setAnimation("Wipe Nose")
        model2.setAnimation("Magic Trick")
        model1.translate(Vec2(2, -3))
        model2.translate(Vec2(3, -4))
    }

    override fun addObjects() {
        add(texture)
        add(model1)
        add(model2)

        add(object3D.apply { translate(Vec3(-2, 0, 0)) })

        add(button)
        add(textField)
        add(slider)
        add(list)

        add(b)
        add(cursorSquare)
    }

    private fun drawScene(tickDelta: Double){

        gameText.render(renderer.view, renderer.projection, tickDelta)
        siGameText.render(renderer.view, renderer.projection, tickDelta)

        drawHUD()

        val ray = camera.getRay()
        val up = Vec3(Vec2.fromAngle(model1.rotation, model1.model.collisionBox.size.y))
        val right = Vec3((Vec2.fromAngle(model1.rotation + PI.f / 2, model1.model.collisionBox.size.x)))
        val model1Rect = Rect3D(Vec3(model1.model.collisionBox.originWithParent(model1)), up, right)
        val point = ray intersects model1Rect

        if(point != null){
            val t = Timer.getCurrentTime()
            val passes = model1Rect containsPoint point
            val dt = Timer.getCurrentTime() - t
            if(passes){
                val pA = (Vec2(point) - model1.position).angle()
                val a = (model1.rotation - pA).wrap(-PI.f, PI.f)
                model1.rotate(a.sign * 0.05f)

                gameEngine.resourcesLoader.getStream("textures/cursor.png")?.let {
                    window.setCursor(it, Vec2i(34, 10))
                }
            }
        }
    }

    fun drawHUD(){
        text.drawCenteredLeft(Vec2(-1f, 0f))
        button.draw()
        textField.draw()
        slider.draw()
        list.draw()

        b.draw()
        cursorSquare.draw()

        MenuItem.opaqueColourShader.setMat4("model", I.translate(Vec3(cursorSquare.origin - (Vec2(0.05f).rotate(-cursorSquare.rotation) * Vec2(1, window.aspectRatio)))).scale(Vec3(0.02f, 0.02f*window.aspectRatio, 0)))
        Shape.centerSquareShape2D.draw()

        //video.draw()
        //bezier.draw()
    }

    override fun render(window: WindowI, tickDelta: Double) {
        renderer.render(window, this, tickDelta)

        drawScene(tickDelta)

        val speed = 10 * Timer.frameDelta
        val travel = Vec2()

        if(pressedKeys.contains('W'.s)) travel += Vec2(0, speed)
        if(pressedKeys.contains('S'.s)) travel -= Vec2(0, speed)
        if(pressedKeys.contains('A'.s)) travel -= Vec2(speed, 0)
        if(pressedKeys.contains('D'.s)) travel += Vec2(speed, 0)

        if(travel != Vec2(0)){
            camera.translate(travel)
            updateText(input.mouse.lastPos)
        }
    }

    override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
        super.onCursorMove(cursorPos, cursorDelta)
        updateText(cursorPos)

        cursorSquare.origin = cursorPos

        val a = window.aspectRatio
        val r = cursorSquare.rotation
        val m = cursorSquare.model
        val l1 = m[0, 0] / cos(r)
        val l2 = m[1, 1] / cos(r)
        val cursorRect = Rect2D(cursorSquare.origin - (Vec2(0.05f).rotate(-r) * Vec2(1, a)), l1, l2, r)
        cursorSquare.colour = if(cursorRect intersects bc) Vec4(0, 1, 0, 0.5) else Vec4(0, 0, 1, 0.5)

//        val cursorRect = Rect2D(camera.screenToWorld(cursorSquare.origin), 0.5f, 0.5f, cursorSquare.rotation)
//        val otherRect = Rect2D(model1.model.collisionBox.originWithParent(model1), model1.model.collisionBox.worldScale, model1.rotation)
//        cursorSquare.colour = if(cursorRect intersects otherRect) Vec4(0, 1, 0, 0.5) else Vec4(0, 0, 1, 0.5)

    }

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == Interactable.INTERRUPT) return Interactable.INTERRUPT

        if(action == 1){
            if(state.i == GLFW_KEY_ESCAPE){
                window.shouldClose = true
            }
            else when(state.c){
                'F' -> toggleFullscreen()
                'C' -> input.mouse.setCursorAt(Vec2(0.75))
                'Z' -> window.size = Vec2i(window.videoMode.width(), window.videoMode.height())
            }
        }


        if(action == 0) pressedKeys.remove(state.key)
        else pressedKeys.add(state.key)
        return action
    }

    override fun onSecondary(window: WindowI, action: Int, mods: Byte) {
        super.onSecondary(window, action, mods)
        if(action == 1) cursorSquare.rotation += PI.f / 16
    }

    override fun update(interval: Float, input: Inputs) {
        super.update(interval, input)

        slider.run {
            this["green"] = (this["green"] + interval).wrap(0f, 1f)
            this["blue"] = this["green"]
        }

        for (t in listOf(text, gameText, siGameText, *(list.items.map { it.text }.toTypedArray()))) {
            t.run {
                underlineThickness = 0.06f
                underlineAmount = (underlineAmount + 0.3f * Timer.delta.f).mod(1f)
            }
        }

        object3D.rotate(Vec3(0.5, 1, 1.5) * interval)
    }

    fun updateText(cursorPos: Vec2){
        val wp = camera.screenToWorld(cursorPos)
        text.text = wp.roundedString(2).let { "X Part: ${it[0]}\nY Part: ${it[1]}" }
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        GLFunc.viewportO = Vec2i(window.width, window.height)
        text.updateAspectRatio(window)
    }

    override fun cleanUp() {
        super.cleanUp()
        text.delete()
        gameText.delete()
        siGameText.delete()
    }
}