package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.apps.animator.ObjectAnimator
import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.AnimatedComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.BitMap3D
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.extension_functions.normal
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Circle
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Parallelogram
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.window.DefaultWindow
import com.pineypiney.game_engine.window.DefaultWindowedEngine
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine_test.scenes.*
import com.pineypiney.game_engine_test.testVR.TestVREngine
import com.pineypiney.game_engine_test.testVR.TestVRGame
import glm_.f
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sign
import kotlin.random.Random

fun main() {
	EngineTest().test3D()
}

@Suppress("UNUSED_VARIABLE", "UNUSED")
class EngineTest{

	@Test
	fun test2D() {
		runWindowEngine(::TestEngine, ::Game2D)
	}

	@Test
	fun test3D(){
		runWindowEngine(::TestEngine, ::Game3D)
	}
	@Test
	fun testLighting(){
		runWindowEngine(::TestEngine, ::LightingTest)
	}

	@Test
	fun testVR(){
		runWindowEngine(::TestVREngine, ::TestVRGame)
	}

	@Test
	fun testCollisionVisual(){
		runWindowEngine(::TestEngine, ::CollisionTest, 100)
	}

	@Test
	fun testCollision3DVisual(){
		runWindowEngine(::TestEngine, ::Collision3DTest, 100)
	}

	@Test
	fun testText(){
		runWindowEngine(::TestEngine, ::TextTest)
	}

	@Test
	fun testShader(){
		runWindowEngine(::TestEngine, ::ShaderTest)
	}

	@Test
	fun testComputeShader(){
		runWindowEngine(::TestEngine, ::ComputeShaderTest, version = Vec2i(4, 3))
	}

	@Test
	fun testTesselationShader(){
		runWindowEngine(::TestEngine, ::TesselationShaderTest, version = Vec2i(4, 1))
	}

	companion object {
		fun <G : GameLogicI, E : GameEngineI<G>> runWindowEngine(engine: (WindowI, (E) -> G, Int, Int) -> E, screen: (E) -> G, ups: Int = 20, fps: Int = 2000, version: Vec2i = Vec2i(3)) {
			LibrarySetUp.initLibraries()

			val window = TestWindow(version = version)
			window.init()
			engine(window, screen, ups, fps).run()
		}
	}

	@Test
	fun testAnimator(){
		LibrarySetUp.initLibraries()
		ObjectAnimator.run(::createSnake)
	}

	@Test
	fun testEditor(){
		LibrarySetUp.initLibraries()
		val window = DefaultWindow("Editor").apply { init() }
		DefaultWindowedEngine(window, { EditorScreen(it) }).run()
	}

	@Test
	fun quaternions(){
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

		println("q2 is $q2\nq3 is $q3")
	}

	@Test
	fun cuboid(){
		val box = Cuboid(Vec3(0f), Quat(Vec3(0f, PI.f * .25f, 0f)), Vec3(6f, 8f, 6f))
		val point = Vec3(3.3f, -3.9f, 1.0f)
		val c = box containsPoint point
		println(c)
	}

	@Test
	fun bitmap(){
		val bitmap = BitMap3D(8, 8, 8)
		bitmap.or(1, 0, 3, 4, 3, 7)
		val slice = bitmap.sliceXZ(1)
		val vals = bitmap.allTrue()
	}

	@Test
	fun testCollision(){
		val rect1 = Rect2D(0f, 0f, 1f, 1f)
		val rect2 = Rect2D(.5f, 1.5f, 1f, 1f)
		val c = rect2.calculateCollision(rect1, Vec2(-1f, -2f))
		c?.removeShape1FromShape2

		val parallelogram = Parallelogram(Vec2(0f), Vec2(-2f, 1f), Vec2(2f, 1f))

		val insidePoint = Vec2(.6f, .301f)
		val outsidePoint = Vec2(-.2f, 1.91f)
		val right = parallelogram.containsPoint(insidePoint)
		val wrong = parallelogram.containsPoint(outsidePoint)
		println("Should be true: $right")
		println("Should be false: $wrong")

		val secondPara = Parallelogram(Vec2(1f, 1.49f), Vec2(-1f, .5f), Vec2(1f, .5f))
		val intersects = secondPara intersects parallelogram
		val eject = secondPara.calculateCollision(parallelogram, Vec2(0f, -.01f))

		println("Intersects: $intersects")
		if(eject != null) {
			println("Ejection: ${eject.removeShape1FromShape2}")
			println("New Movement: ${eject.collisionNormal.normal().let { it * (it dot eject.shape1Movement).sign }}")
		}
	}

	@Suppress("UNUSED_VARIABLE")
	@Test
	fun colourTest(){
		val colour = Colour(45.075f, 22.799f, 22.102f, 1f, Colour.ColourModel.CIEXYZ)
		val rgb = colour.rgbValue
		val hsv = colour.hsvValue
		val hsl = colour.hslValue
		val cie = colour.cieValue
		val oklab = colour.oklabValue
		val oklch = colour.oklchValue
	}

	@Test
	@Suppress("UNUSED_VARIABLE")
	fun testIntersections(){
		val random = Random(2934875623498652L)
		val iters = 3e2.toInt()
		val combos = (iters * iters + iters) / 2
		val circles = MutableList(iters){ Circle(Vec2(random.nextFloat() * 5f, random.nextFloat() * 5f), random.nextFloat() * 2f) }

		val circleTime = ResourcesLoader.timeAction {
			for(i in 0..<iters - 1){
				val circle1 = circles[i]
				for(j in (i+1)..<iters) {
					val circle2 = circles[j]
					val touching = (circle1.center - circle2.center).length() <= (circle1.radius + circle2.radius)
					//if(circle1.intersects(circle2) != touching){
					//	println("Uh Oh!")
					//}
				}
			}
		}
		val circlesSquaredTime = ResourcesLoader.timeAction {
			for(i in 0..<iters - 1){
				val circle1 = circles[i]
				for(j in (i+1)..<iters) {
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
			for(i in 0..<iters - 1){
				val rect1 = rects[i]
				for(j in (i+1)..<iters) {
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

		println("Calculated $combos Combinations")
		println("Circle time is $circleTime ns, average ${circleTime / combos} ns")
		println("CircleSquared time is $circlesSquaredTime ns, average ${circlesSquaredTime / combos} ns")
		println("Rect time is $rectTime ns, average ${rectTime / combos} ns")
	}

	@Test
	fun testRegex(){
		val s = "textures\\\\snake\\snake"
		val r = Regex("[^\\\\]\\\\[^\\\\]")
		println("S is $s")
		println("New String is ${s.replace(r, "T")}")
	}
}

fun createSnake(): GameObject{
	return object : GameObject() {

		override var name: String = "Snake"

		override fun addComponents() {
			super.addComponents()

			val animation = Animation("slither", 7f, "snake", (0..5).map { "snake_$it" },"slitherz")
			val animations: List<Animation> = listOf(animation, Animation("backwards", 7f, "snake", (5 downTo 0).map { "snake_$it" }, "backwards"))

			scale = Vec3(4f)
			components.add(SpriteComponent(this))
			components.add(Collider2DComponent(this, Rect2D(Vec2(), Vec2(1))))
			components.add(AnimatedComponent(this, animation, animations))
		}
	}
}
