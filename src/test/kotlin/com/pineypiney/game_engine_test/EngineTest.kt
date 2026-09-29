package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.apps.animator.ObjectAnimator
import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.AnimatedComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.rendering.DefaultWindowGameRenderer
import com.pineypiney.game_engine.rendering.cameras.CameraI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.BitMap3D
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.extension_functions.addAll
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.jna.GameInput
import com.pineypiney.game_engine.util.jna.LibScePad
import com.pineypiney.game_engine.util.jna.StdC
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Circle
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Parallelogram
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.vulkan.VulkanManager
import com.pineypiney.game_engine.window.VulkanWindow
import com.pineypiney.game_engine.window.VulkanWindowedEngine
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine_test.scenes.*
import com.pineypiney.game_engine_test.testVR.TestVREngine
import com.pineypiney.game_engine_test.testVR.TestVRGame
import glm_.asHexString
import glm_.f
import glm_.has
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.junit.Test
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random

fun main() {
	EngineTest().test3D()
}

@Suppress("UNUSED_VARIABLE", "UNUSED")
class EngineTest{

	@Test
	fun test2D() {
		runVulkanEngine(::Game2D, ::OrthographicCamera)
	}

	@Test
	fun test3D(){
		runVulkanEngine(::Game3D, ::PerspectiveCamera)
	}
	@Test
	fun testLighting(){
		runOpenGlEngine(::LightingTest, ::PerspectiveCamera)
	}

	@Test
	fun testVR(){
		runWindowEngine(::TestVREngine, ::TestVRGame)
	}

	@Test
	fun testCollisionVisual(){
		runOpenGlEngine(::CollisionTest, ::OrthographicCamera, 100)
	}

	@Test
	fun testCollision3DVisual(){
		runOpenGlEngine(::Collision3DTest, ::OrthographicCamera, 100)
	}

	@Test
	fun testText(){
		runOpenGlEngine(::TextTest, ::OrthographicCamera)
	}

	@Test
	fun testShader(){
		runOpenGlEngine(::ShaderTest, ::OrthographicCamera)
	}

	@Test
	fun testStencil() {
		runVulkanEngine(::StencilTest, ::OrthographicCamera)
	}

	@Test
	fun testComputeShader(){
		runOpenGlEngine(::ComputeShaderTest, ::OrthographicCamera, version = Vec2i(4, 3))
	}

	@Test
	fun testTesselationShader(){
		runVulkanEngine(::TesselationShaderTest, ::PerspectiveCamera, version = Vec2i(4, 1))
	}

	companion object {

		fun <G : GameLogicI, E : GameEngineI<G>> runWindowEngine(engine: (WindowI, (E) -> G, Int, Int) -> E, screen: (E) -> G, ups: Int = 20, fps: Int = 2000, version: Vec2i = Vec2i(3)) {
			LibrarySetUp.initGLFW()

			val window = TestWindow(version = version)
			window.init()
			engine(window, screen, ups, fps).run()
		}

		fun <G : WindowGameLogic, C : CameraI> runOpenGlEngine(
			screen: (TestOpenGlEngine<G, C>, DefaultWindowGameRenderer<G, C>) -> G,
			camera: (WindowI) -> C,
			ups: Int = 20,
			fps: Int = 2000,
			version: Vec2i = Vec2i(3)
		) {
			LibrarySetUp.initGLFW()
			val window = TestWindow(version = version)
			window.init()
			TestOpenGlEngine(window, screen, camera, ups, fps).run()
		}

		fun <G : WindowGameLogic, C : CameraI> runVulkanEngine(
			screen: (TestVulkanEngine<G, C>, DefaultWindowGameRenderer<G, C>) -> G,
			camera: (WindowI) -> C,
			ups: Int = 20,
			fps: Int = 2000,
			version: Vec2i = Vec2i(3)
		) {
			LibrarySetUp.initGLFW()
			val window = VulkanWindow("Vulkan Window", 960, 540)
			window.init()
			TestVulkanEngine(window, screen, camera, ups, fps).run()
		}
	}

	@Test
	fun testAnimator(){
		LibrarySetUp.initGLFW()
		ObjectAnimator.run(::createSnake)
	}

	@Test
	fun testEditor(){
		LibrarySetUp.initGLFW()
		val window = VulkanWindow("Editor").apply { init() }
		VulkanWindowedEngine(window, VulkanManager(), ::EditorScreen).run()
	}

	@Test
	fun quaternions() {
		//val q = Quaternion(Vec3(0, PI, 0))
		//val e = q.toEulerAngles()
		//val q1 = Quaternion(e)
//
		//val e1 = Vec3(0.1, 0.25, 0.2)
		//val q2 = Quaternion(e1).pow(3f)
		//val e2 = q2.toEulerAngles()

		val q = Quat(Vec3(0f, 0f, 1.2f))
		val model = q.toMat4()
		val q2 = model.getRotation()

		val q3 = (I.translate(2f, -3f, 0f) * model.scale(3f)).getRotation()

	}

	@Test
	fun cuboid() {
		val box = Cuboid(Vec3(0f), Quat(Vec3(0f, PI.f * .25f, 0f)), Vec3(6f, 8f, 6f))
		val point = Vec3(3.3f, -3.9f, 1.0f)
		val c = box containsPoint point
	}

	@Test
	fun bitmap(){
		val bitmap = BitMap3D(8, 8, 8)
		bitmap.or(1, 0, 3, 4, 3, 7)
		val slice = bitmap.sliceXZ(1)
		val vals = bitmap.allTrue()
	}

	@Test
	fun testCollision() {
		val rect1 = Rect2D(0f, 0f, 1f, 1f)
		val rect2 = Rect2D(.5f, 1.5f, 1f, 1f)
		val c = rect2.calculateCollision(rect1, Vec2(-1f, -2f))
		c?.removeShape1FromShape2

		val parallelogram = Parallelogram(Vec2(0f), Vec2(-2f, 1f), Vec2(2f, 1f))

		val insidePoint = Vec2(.6f, .301f)
		val outsidePoint = Vec2(-.2f, 1.91f)
		val right = parallelogram.containsPoint(insidePoint)
		val wrong = parallelogram.containsPoint(outsidePoint)

		val secondPara = Parallelogram(Vec2(1f, 1.49f), Vec2(-1f, .5f), Vec2(1f, .5f))
		val intersects = secondPara intersects parallelogram
		val eject = secondPara.calculateCollision(parallelogram, Vec2(0f, -.01f))

		if (eject != null) {
			println("New Movement: ${eject.collisionNormal.normal().let { it * (it dot eject.shape1Movement).sign }}")
		}
	}

	@Suppress("UNUSED_VARIABLE")
	@Test
	fun colourTest(){
		val colour = Colour(45.075f, 22.799f, 22.102f, 1f, Colour.ColourModel.CIEXYZ)
		val rgb = colour.rgbVec
		val hsv = colour.hsvValue
		val hsl = colour.hslValue
		val cie = colour.cieValue
		val oklab = colour.oklabValue
		val oklch = colour.oklchValue
	}

	@Test
	@Suppress("UNUSED_VARIABLE")
	fun testIntersections() {
		val random = Random(2934875623498652L)
		val iters = 3e2.toInt()
		val combos = (iters * iters + iters) / 2
		val circles = MutableList(iters) { Circle(Vec2(random.nextFloat() * 5f, random.nextFloat() * 5f), random.nextFloat() * 2f) }

		val circleTime = ResourcesLoader.timeAction {
			for (i in 0..<iters - 1) {
				val circle1 = circles[i]
				for (j in (i + 1)..<iters) {
					val circle2 = circles[j]
					val touching = (circle1.center - circle2.center).length() <= (circle1.radius + circle2.radius)
					//if(circle1.intersects(circle2) != touching){
					//	println("Uh Oh!")
					//}
				}
			}
		}
		val circlesSquaredTime = ResourcesLoader.timeAction {
			for (i in 0..<iters - 1) {
				val circle1 = circles[i]
				for (j in (i + 1)..<iters) {
					val circle2 = circles[j]
					val touching = (circle1.center - circle2.center).length2() < (circle1.radius + circle2.radius).let { it * it }
					//if(circle1.intersects(circle2) != touching){
					//	println("Uh Oh Squared!")
					//}
				}
			}
		}
		circles.clear()

		val rects = Array(iters) { Rect2D(Vec2(random.nextFloat() * 5f, random.nextFloat() * 5f), random.nextFloat() * 2f, random.nextFloat() * 2) }
		val rectTime = ResourcesLoader.timeAction {
			for (i in 0..<iters - 1) {
				val rect1 = rects[i]
				for (j in (i + 1)..<iters) {
					val rect2 = rects[j]
					val touching =
						(if (rect1.origin.x > rect2.origin.x) rect2.origin.x + rect2.length1 > rect1.origin.x else rect1.origin.x + rect1.length1 > rect2.origin.x) &&
								if (rect1.origin.y > rect2.origin.y) rect2.origin.y + rect2.length2 > rect1.origin.y else rect1.origin.y + rect1.length2 > rect2.origin.y
					//if(rect1.intersects(rect2) != touching){
					//	println("Uh Oh Rect!")
					//}
				}
			}
		}

		println("Rect time is $rectTime ns, average ${rectTime / combos} ns")
	}

	@Test
	fun testRegex() {
		val s = "textures\\\\snake\\snake"
		val r = Regex("[^\\\\]\\\\[^\\\\]")
	}

	@Test
	fun testScePad() {

		val p = StdC.INSTANCE.calloc(0x2004, 1)

		val initParam = LibScePad.InitParam(true)
		println("Init returned ${LibScePad.INSTANCE.scePadInit3(initParam.p)}")
		initParam.delete()

		val pad = LibScePad.INSTANCE.scePadOpen(1, 0, 0)
		println("Open returned $pad")


		val idInfoSizeError = LibScePad.INSTANCE.scePadGetContainerIdInformation(pad, p)
		val size = p.getInt(0).coerceIn(0x2004, 0x10000)

		val infoPointer = StdC.INSTANCE.malloc(size + 4L)
		val idInfoError = LibScePad.INSTANCE.scePadGetContainerIdInformation(pad, infoPointer)
		val chars = infoPointer.getCharArray(4, size)
		val containerIdInfo = chars.concatToString()
		StdC.INSTANCE.free(infoPointer)

		val busTypeError = LibScePad.INSTANCE.scePadGetControllerBusType(pad, p)
		val busType = p.getInt(0)

		val infoError = LibScePad.INSTANCE.scePadGetControllerInformation(pad, p)
		val info = LibScePad.Info.read(p)

		val typeError = LibScePad.INSTANCE.scePadGetControllerType(pad, p)
		val type = p.getInt(0)

		val jackError = LibScePad.INSTANCE.scePadGetJackState(pad, p)
		val jack = p.getInt(0)

		val trigStateError = LibScePad.INSTANCE.scePadGetTriggerEffectState(pad, p)
		val triggerState = p.getIntArray(0, 2)

		val data = LibScePad.Data()
		while (true) {
			val read = LibScePad.INSTANCE.scePadReadState(pad, data.p)
			if (data.buttonBitmask has LibScePad.BUTTON_CIRCLE) break

			val colour = Colour(.3f * Timer.getCurrentTime().toFloat() % 1f, 1f, 1f, 1f, Colour.ColourModel.HSV)
			val light = LibScePad.LightBar(colour.r, colour.g, colour.b)
			val lightState = LibScePad.INSTANCE.scePadSetLightBar(pad, light.p)

			val triggerEffect = LibScePad.TriggerEffect(
				LibScePad.TriggerEffectSlopeFeedback(3, 6, 1, 5),
				LibScePad.TriggerEffectMultiPosFeedback(ByteArray(10) { ((it / 2 % 2) * 8).toByte() })
			)
			val triggerState = LibScePad.INSTANCE.scePadSetTriggerEffect(pad, triggerEffect.p)

			val vibrationEffect = LibScePad.Vibration(8, 0)
			val vibeModeState = LibScePad.INSTANCE.scePadSetVibrationMode(pad, LibScePad.HAPTICS_MODE)
			val vibeState = LibScePad.INSTANCE.scePadSetVibration(pad, vibrationEffect.p)
			vibrationEffect.delete()
		}
		data.delete()
		StdC.INSTANCE.free(p)

		println("Close returned ${LibScePad.INSTANCE.scePadClose(pad)}")
		println("Terminate returned ${LibScePad.INSTANCE.scePadTerminate()}")
	}

	@Test
	fun testGameInput() {

		val controllers = mutableListOf<GameInput.Controller>()

		GameInput.processError(GameInput.INSTANCE.initInput(), "Failed to initialise GameInput")

		val connectCb = GameInput.ConnectCallback { p ->
			val controller = GameInput.Controller(p)
			val info = controller.info
			println("Controller Connected")
			println("\tName = ${info.displayName}")
			println("\tFamily = ${info.deviceFamily}")
			println("\tType = ${info.supportedInput.asHexString}")
			println("\tGuid = ${info.containerId.toHexString()}")

			synchronized(controllers) {
				controllers.add(controller)
			}
		}
		val disconnectCb = GameInput.DisconnectCallback { device ->

			val controller = controllers.firstOrNull { it.device == device } ?: return@DisconnectCallback

			val info = controller.info
			println("Controller Disconnected")
			println("\tName = ${info.displayName}")
			println("\tFamily = ${info.deviceFamily}")
			println("\tType = ${info.supportedInput.asHexString}")
			println("\tGuid = ${info.containerId.toHexString()}")

			GameInput.INSTANCE.releaseController(controller.p)
			synchronized(controllers) {
				controllers.remove(controller)
			}
		}

		GameInput.INSTANCE.setConnectCallback(connectCb)
		GameInput.INSTANCE.setDisconnectCallback(disconnectCb)

		var run = true
		while (run) {
			synchronized(controllers) {
				for ((p) in controllers) {
					val readError = GameInput.INSTANCE.updateReading(p)
					if (GameInput.processError(readError, "Failed to get current reading for controller")) continue

					val state = GameInput.GamepadState()
					val gamepadError = GameInput.INSTANCE.pollGamepad(p, state.p)

					if (state.buttons.has(GameInput.gamepadB)) run = false

					val lf = max(0f, state.leftStickY - .2f) * 1.25f
					val hf = max(0f, state.rightStickY - .2f) * 1.25f
					GameInput.INSTANCE.setRumble(p, lf, hf, state.leftTrigger, state.rightTrigger)

					state.delete()
				}
			}
			Thread.sleep(1)
		}

		try {
			GameInput.INSTANCE.releaseInput()
		} catch (e: Exception) {

		}
	}
}

fun createSnake(): GameObject{
	val obj = GameObject("snake")

	val animation = Animation("slither", 7f, "snake", (0..5).map { "snake_$it" }, "slitherz")
	val animations: List<Animation> = listOf(animation, Animation("backwards", 7f, "snake", (5 downTo 0).map { "snake_$it" }, "backwards"))

	obj.scale = Vec3(4f)
	obj.components.addAll(
		SpriteComponent(obj),
		Collider2DComponent(obj, Rect2D(Vec2(), Vec2(1))),
		AnimatedComponent(obj, animation, animations)
	)
	return obj
}
