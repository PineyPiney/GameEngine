package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.apps.animator.ObjectAnimator
import com.pineypiney.game_engine.apps.editor.EditorScreen
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.AnimatedComponent
import com.pineypiney.game_engine.objects.components.colliders.Collider2DComponent
import com.pineypiney.game_engine.objects.components.rendering.SpriteComponent
import com.pineypiney.game_engine.objects.util.Animation
import com.pineypiney.game_engine.util.extension_functions.getRotation
import com.pineypiney.game_engine.util.maths.I
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.window.DefaultWindow
import com.pineypiney.game_engine.window.DefaultWindowedEngine
import com.pineypiney.game_engine_test.test2D.Game2D
import com.pineypiney.game_engine_test.test3D.Game3D
import com.pineypiney.game_engine_test.testVR.TestVREngine
import com.pineypiney.game_engine_test.testVR.TestVRGame
import glm_.f
import glm_.quat.Quat
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.junit.Test
import kotlin.math.PI

fun main() {
	EngineTest().test3D()
}

class EngineTest{

	@Test
	fun test2D() {
		run(::TestEngine, ::Game2D)
	}

	@Test
	fun test3D(){
		run(::TestEngine, ::Game3D)
	}

	@Test
	fun testVR(){
		run(::TestVREngine, ::TestVRGame)
	}

	companion object {
		fun <G : GameLogicI, E : GameEngineI<G>> run(engine: ((E) -> G) -> E, screen: (E) -> G) {
			LibrarySetUp.initLibraries()

			TestWindow.INSTANCE.init()
			engine(screen).run()
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
		DefaultWindowedEngine(window, ::EditorScreen).run()
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
	}

	@Test
	fun cuboid(){
		val box = Cuboid(Vec3(0f), Quat(Vec3(0f, PI.f * .25f, 0f)), Vec3(6f, 8f, 6f))
		val point = Vec3(3.3f, -3.9f, 1.0f)
		val c = box containsPoint point
		println(c)
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
