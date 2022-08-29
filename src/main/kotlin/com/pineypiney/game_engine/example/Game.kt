package com.pineypiney.game_engine.example

import com.pineypiney.game_engine.GameEngine
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.Window
import com.pineypiney.game_engine.audio.AudioSource
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.ModelledGameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_2D.SimpleTexturedGameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_3D.SimpleTexturedGameObject3D
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.MenuItem
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.menu_items.VideoPlayer
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.BasicScrollList
import com.pineypiney.game_engine.objects.menu_items.slider.BasicSlider
import com.pineypiney.game_engine.objects.text.SizedGameText
import com.pineypiney.game_engine.objects.text.SizedStaticText
import com.pineypiney.game_engine.objects.text.StretchyGameText
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.objects.util.shapes.Shape
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.resources.video.VideoLoader
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.extension_functions.fromAngle
import com.pineypiney.game_engine.util.extension_functions.roundedString
import com.pineypiney.game_engine.util.extension_functions.wrap
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Rect
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11
import java.awt.Font
import java.awt.font.FontRenderContext
import kotlin.math.PI
import kotlin.math.sign

class Game(override val gameEngine: GameEngine<*>): GameLogic() {

    override val camera: OrthographicCamera = OrthographicCamera(window)
    override val renderer: Renderer = Renderer(window)

    private val pressedKeys = mutableSetOf<Short>()
    private val audio = AudioLoader[(ResourceKey("clair_de_lune"))]

    private val button = TextButton("button", Vec2(0.6, 0.8), Vec2(0.4, 0.2), window){
        println("Pressed!")
        AudioSource(audio).play()
        window.setCursor(intArrayOf(GLFW_HRESIZE_CURSOR, GLFW_RESIZE_NESW_CURSOR, GLFW_RESIZE_ALL_CURSOR, GLFW_NOT_ALLOWED_CURSOR).random())
    }
    private val textField = ActionTextField(Vec2(-1), Vec2(1, 0.2), window){ _, char, _ ->
        println("Typing $char")
        AudioSource(audio).play()
        window.setCursor(0L)
    }
    private val slider = BasicSlider(Vec2(0.1, -0.9), Vec2(0.8, 0.1), 0f, 10f, 5f, window)

    private val texture = SimpleTexturedGameObject2D(ResourceKey("texture"), Texture.broke)
    private val model1 = ModelledGameObject2D(ModelLoader.getModel(ResourceKey("goblin")), Model.DEBUG_COLLIDER)
    private val model2 = ModelledGameObject2D(ModelLoader.getModel(ResourceKey("goblin")), Model.DEBUG_COLLIDER)

    private val object3D = SimpleTexturedGameObject3D(ResourceKey("3d"), ResourceKey("broke"))

    private val text = SizedStaticText("X Part: 0.00 \nY Part: 0.00", window, 24, Vec2(0.6, 0.2))
    private val gameText = StretchyGameText("This is some Game Text", Vec2(17.78, 10), Vec4(0.0, 1.0, 1.0, 1.0))
    private val siGameText = SizedGameText("This is some Sized Game Text", 300, Vec2(11, 10), Vec4(0.0, 1.0, 1.0, 1.0))

    private val list = BasicScrollList(Vec2(-1, 0.4), Vec2(0.6), 1f, 0.05f, arrayOf("Hello", "World"), window)

    val video = VideoPlayer(VideoLoader[ResourceKey("ghost"), gameEngine.resourcesLoader], Vec2(0.5, -0.15), Vec2(0.5, 0.3))

    val f = Font.createFont(Font.TRUETYPE_FONT, gameEngine.resourcesLoader.getStream("textures/fonts/LightSlab.ttf"))
    val v = f.createGlyphVector(FontRenderContext(null, false, false), "q").outline
    val shape: Shape

    init {
        val iterator = v.getPathIterator(null)
        val floats = mutableListOf<List<Float>>()
        while(!iterator.isDone){
            val floatA = FloatArray(5)
            iterator.currentSegment(floatA)
            floats.add(listOf(floatA[0], -floatA[1]))
            iterator.next()
        }
        val cut = floats.filter { it != listOf(0f, 0f) }
        shape = ArrayShape(cut.flatten().toFloatArray(), intArrayOf(2))
    }

    override fun init() {
        super.init()
        text.init()
        gameText.init()
        siGameText.init()
//        video.init()
//        video.play()
//        video.video.loop = true

        model1.setAnimation("Wipe Nose")
        model2.setAnimation("Magic Trick")
        model1.translate(Vec2(2, -3))
        model2.translate(Vec2(5, -3))
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
    }

    private fun drawLetter(){
        val shader = MenuItem.opaqueColourShader
        shader.use()
        shader.setMat4("model", I)
        shader.setVec3("colour", Vec3(1, 0, 1))

        shape.bind()
        shape.draw(GL11.GL_LINE_LOOP)
    }

    private fun drawScene(tickDelta: Double){

        gameText.render(renderer.view, renderer.projection, tickDelta)
        siGameText.render(renderer.view, renderer.projection, tickDelta)

        drawHUD()
        drawLetter()

        val ray = camera.getRay()
        val up = Vec3(Vec2.fromAngle(model1.rotation, model1.model.collisionBox.size.y))
        val right = Vec3((Vec2.fromAngle(model1.rotation + PI.f / 2, model1.model.collisionBox.size.x)))
        val model1Rect = Rect(Vec3(model1.model.collisionBox.originWithParent(model1)), up, right)
        val point = ray intersects model1Rect

        if(point != null){
            val t = Timer.getCurrentTime()
            val passes = model1Rect containsPoint point
            val dt = Timer.getCurrentTime() - t
            if(passes){
                println("Calc Time is $dt")
                val pA = (Vec2(point) - model1.position).angle()
                val a = (model1.rotation - pA).wrap(-PI.f, PI.f)
                model1.rotate(a.sign * 0.05f)

                window.setCursor(TextureLoader[ResourceKey("cursor")], Vec2i(34, 10))
            }
        }
    }

    fun drawHUD(){
        text.drawCenteredLeft(Vec2(-1, 0))
        button.draw()
        textField.draw()
        slider.draw()
        list.draw()
        video.draw()
    }

    override fun render(window: Window, tickDelta: Double) {
        renderer.render(window, this, tickDelta)

        drawScene(tickDelta)

        val speed = 10
        if(pressedKeys.contains('W'.s)) this.camera.setPos(this.camera.cameraPos + Vec3(0, 1, 0) * Timer.frameDelta * speed)
        if(pressedKeys.contains('S'.s)) this.camera.setPos(this.camera.cameraPos - Vec3(0, 1, 0) * Timer.frameDelta * speed)
        if(pressedKeys.contains('A'.s)) this.camera.setPos(this.camera.cameraPos - Vec3(1, 0, 0) * Timer.frameDelta * speed)
        if(pressedKeys.contains('D'.s)) this.camera.setPos(this.camera.cameraPos + Vec3(1, 0, 0) * Timer.frameDelta * speed)
    }

    override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
        super.onCursorMove(cursorPos, cursorDelta)
        val wp = camera.screenToWorld(cursorPos)
        text.text = wp.roundedString(2).let { "X Part: ${it[0]} \nY Part: ${it[1]}" }
    }

    override fun onInput(state: InputState, action: Int): Int {
        if(super.onInput(state, action) == Interactable.INTERRUPT) return Interactable.INTERRUPT

        if(state.c == 'F' && action == 1){
            toggleFullscreen()
        }
        if(state.c == 'C' && action == 1){
            input.mouse.setCursorAt(Vec2(0.5))
        }
        if(state.i == GLFW_KEY_ESCAPE && action == 1){
            window.setShouldClose()
        }

        if(action == 0) pressedKeys.remove(state.key)
        else pressedKeys.add(state.key)
        return action
    }

    override fun update(interval: Float, input: Inputs) {
        super.update(interval, input)

        object3D.rotate(Vec3(0.5, 1, 1.5) * interval)
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
}