package com.pineypiney.game_engine_test.test2D

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.audio.AudioEngine
import com.pineypiney.game_engine.audio.AudioSource
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.*
import com.pineypiney.game_engine.objects.components.rendering.ColourRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.ModelRendererComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.components.rendering.TextRendererComponent
import com.pineypiney.game_engine.objects.components.scrollList.ScrollListComponent
import com.pineypiney.game_engine.objects.components.slider.ColourSliderRendererComponent
import com.pineypiney.game_engine.objects.menu_items.ActionTextField
import com.pineypiney.game_engine.objects.menu_items.TextButton
import com.pineypiney.game_engine.objects.menu_items.scroll_lists.BasicScrollList
import com.pineypiney.game_engine.objects.menu_items.slider.ColourSlider
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.audio.AudioLoader
import com.pineypiney.game_engine.resources.models.Model
import com.pineypiney.game_engine.resources.models.ModelLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.Cursor
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.extension_functions.angle
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.extension_functions.roundedString
import com.pineypiney.game_engine.util.extension_functions.wrap
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Rect3D
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import com.pineypiney.game_engine_test.Renderer
import glm_.f
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.swizzle.xy
import glm_.vec4.Vec4
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.openal.AL10
import kotlin.math.PI
import kotlin.math.sign

class Game2D(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = Renderer(window, OrthographicCamera(window))
	val camera get() = renderer.camera

	private val pressedKeys = mutableSetOf<Short>()

	val standardCursors = intArrayOf(GLFW_RESIZE_EW_CURSOR, GLFW_RESIZE_NESW_CURSOR, GLFW_RESIZE_ALL_CURSOR, GLFW_NOT_ALLOWED_CURSOR).map { Cursor(it) }
	val customCursor = Cursor(gameEngine, "textures/cursor.png", Vec2i(34, 10))

	private val audio get() = AudioLoader[(ResourceKey("clair_de_lune"))]

	private val b = TextButton("Button", Vec2(-0.3, 0.6), Vec2(0.6, 0.2)){ _, _ ->
		val device = AudioEngine.getAllOutputDevices().random()
		window.setAudioOutput(device)
		GameEngineI.info("Setting audio out device to ${window.audioOutputDevice}")

		window.vSync = !window.vSync
		println("Window vSync: ${window.vSync}")
	}
	private val bc = Rect2D(b.position.xy, 0.6f, 0.2f)

	var squareColour = Vec4()
	private val cursorSquare = GameObject.simpleRenderedGameObject(ColourRendererComponent.menuShader, Vec3(), Vec3(.1f, .1f, 1f)){
		uniforms.setVec4Uniform("colour", ::squareColour)
	}

	private val button = TextButton("button", Vec2(window.aspectRatio - .4f, .8f), Vec2(.4f, .2f)){ _, _ ->
		println("Pressed!")
		AudioSource(audio).play()
		window.setCursor(standardCursors.random())
	}
	private val textField = ActionTextField<ActionTextFieldComponent<*>>("Text Field", Vec2(-window.aspectRatio, -1f), Vec2(window.aspectRatio, 0.2)){ _, _, _ ->
//        AudioSource(audio).play()
		window.setCursor(0L)

		window.audioInputDevice?.let {
			val input = it.sample()
			val a = AudioLoader.bufferAudio(input, AL10.AL_FORMAT_MONO8, 44100)
			AudioSource(a).play()
		}
	}

	private val slider = ColourSlider(Vec2(0.1, -0.9), Vec2(0.8, 0.1), ColourSliderRendererComponent.redShader, mutableMapOf("green" to 0.5f, "blue" to 0.5f))

	private val texture = GameObject.simpleTextureGameObject(Texture.broke)
	private val model1 = GameObject.simpleModelledGameObject(ModelLoader.getModel(ResourceKey("goblin")), debug = Model.DEBUG_COLLIDER)
	private val model2 = GameObject.simpleModelledGameObject(ModelLoader.getModel(ResourceKey("goblin")), debug = Model.DEBUG_COLLIDER)

	private val text = Text.makeMenuText(
		"X Part: 0.00 \nY Part: 0.00",
		Vec4(0f, 0f, 0f, 1f),
		1f,
		1f,
		.16f,
		Text.ALIGN_CENTER_LEFT
	)
	private val gameText = Text.makeGameText(
		"This is some Stretchy Game Text",
		Vec4(0.0, 1.0, 1.0, 1.0),
		8.88f,
		10f,
		0f,
	)
	private val siGameText = Text.makeGameText(
		"This is some Sized Game Text",
		Vec4(0.0, 1.0, 1.0, 1.0),
		7f,
		10f,
		alignment = Text.ALIGN_CENTER_LEFT,
	)
	private val testText = Text.makeMenuText(
		"[ [",
		Vec4(0f, 0f, 0f, 1f),
		0.01f,
		2f,
		.2f,
		Text.ALIGN_CENTER_LEFT
	)

	private val list = BasicScrollList(Vec2(-window.aspectRatio, 0.4), Vec2(1f, 0.6f), 1f, 0.05f, arrayOf("Hello", "World"))

//    val video = VideoPlayer(VideoLoader[ResourceKey("ghost"), gameEngine.resourcesLoader], Vec2(0.5, -0.15), Vec2(0.5, 0.3))

	val snake = object: GameObject(){
		val animation: Animation = Animation("slither", 7f, "snake", (0..5).map { "snake_$it" })

		override fun addComponents() {
			super.addComponents()
			components.add(SpriteComponent(this))
			components.add(AnimatedComponent(this, animation, listOf(animation)))
		}

		override fun init() {
			super.init()
			scale = Vec3(4f, 4f, 1f)
		}
	}

	override fun addObjects() {
		add(texture)
		add(model1)
		add(model2)
		add(snake)

		add(button)
		add(textField)
		add(slider)
		add(list)

		add(text, gameText, siGameText, testText)
		text.position = Vec3(-window.aspectRatio, 0f, 0f)
		testText.position = Vec3(-window.aspectRatio, -.3f, 0f)

		add(b)
		add(cursorSquare)
	}

	override fun init() {
		super.init()

		gameText.transformComponent.position = Vec3(0, 2)

//        video.init()
//        video.play()
//        video.video.loop = true

		model1.getComponent<ModelRendererComponent>()?.setAnimation("Wipe Nose")
		model2.getComponent<ModelRendererComponent>()?.setAnimation("Magic Trick")
		model1 translate Vec3(2, -3, 0f)
		model1 scale Vec3(3f)
		model2 translate Vec3(3, -4, 0f)
		snake translate Vec3(-2, -5, 0f)
	}

	private fun rotateGoblin() {

		val ray = camera.getRay(input.mouse.screenSpaceCursor())
		val model = model1.getShape() as Rect2D
		val model1Rect = Rect3D(model)
		val point = model1Rect.intersectedBy(ray).getOrNull(0)

		if(point != null) {
			val pA = (Vec2(point) - model1.position.xy).angle()
			val a = -(model1.rotation.eulerAngles().z + pA).wrap(-PI.f, PI.f)
			model1.rotate(Vec3(0f, 0f, -a.sign * 0.05f))

			window.setCursor(customCursor)
		}
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)

		rotateGoblin()

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

		cursorSquare.position = Vec3(cursorPos, 0f)

		val cursorRect = cursorSquare.getShape()
		squareColour = if((cursorRect as Rect2D) intersects bc) Vec4(0, 1, 0, 0.5) else Vec4(0, 0, 1, 0.5)

//        val cursorRect = Rect2D(camera.screenToWorld(cursorSquare.origin), 0.5f, 0.5f, cursorSquare.rotation)
//        val otherRect = Rect2D(model1.model.collisionBox.originWithParent(model1), model1.model.collisionBox.worldScale, model1.rotation)
//        cursorSquare.colour = if(cursorRect intersects otherRect) Vec4(0, 1, 0, 0.5) else Vec4(0, 0, 1, 0.5)

	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

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
		if(action == 1) cursorSquare.rotate(Vec3(0f, 0f, PI.f / 16f))
	}

	override fun update(interval: Float, input: Inputs) {
		super.update(interval, input)

		slider.getComponent<ColourSliderRendererComponent>()?.run {
			this["green"] = (this["green"] + interval).wrap(0f, 1f)
			this["blue"] = this["green"]
		}

		val text = listOf(text, gameText, siGameText).mapNotNull { it.getComponent<TextRendererComponent>()?.text }
		for (t in text + listOf(*(list.getComponent<ScrollListComponent>()!!.items.mapNotNull { it.children.firstNotNullOfOrNull { it.getComponent<TextRendererComponent>()?.text } }.toTypedArray()))) {
			t.run {
				underlineThickness = 0.06f
				underlineAmount = (underlineAmount + 0.3f * Timer.delta.f).mod(1f)
			}
		}
	}

	fun updateText(cursorPos: Vec2){
		val wp = camera.screenToWorld(cursorPos)
		text.getComponent<TextRendererComponent>()?.text?.text = wp.roundedString(2).let { "X Part: ${it[0]}\nY Part: ${it[1]}" }
	}

	override fun updateAspectRatio(window: WindowI) {
		super.updateAspectRatio(window)
		GLFunc.viewportO = Vec2i(window.width, window.height)

		button.run {
			position = Vec3(window.aspectRatio - .4f, .8f, 0f)
		}
		textField.run {
			position = Vec3(-window.aspectRatio, -1f, 0f)
			scale = Vec3(window.aspectRatio, .2f, 1f)
		}
		list.position = Vec3(-window.aspectRatio, 0.4, 0f)

		text.position = Vec3(-window.aspectRatio, 0f, 0f)
		testText.position = Vec3(-window.aspectRatio, -.3f, 0f)
	}

	override fun cleanUp() {
		super.cleanUp()

		standardCursors.delete()
		customCursor.delete()
	}
}