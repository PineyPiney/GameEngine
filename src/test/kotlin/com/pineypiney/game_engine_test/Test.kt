package com.pineypiney.game_engine_test

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogicI
import com.pineypiney.game_engine.LibrarySetUp
import com.pineypiney.game_engine.apps.animator.ObjectAnimator
import com.pineypiney.game_engine.objects.Storable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.AnimatedObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_2D.texture_animation.Animation
import com.pineypiney.game_engine.objects.game_objects.transforms.Quaternion
import com.pineypiney.game_engine.objects.util.collision.SoftCollisionBox
import com.pineypiney.game_engine.objects.util.components.ColliderComponent
import com.pineypiney.game_engine.objects.util.components.TextureComponent
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.resources.FileResourcesLoader
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.directory
import com.pineypiney.game_engine_test.test2D.Game2D
import com.pineypiney.game_engine_test.test3D.Game3D
import com.pineypiney.game_engine_test.testVR.TestVREngine
import com.pineypiney.game_engine_test.testVR.TestVRGame
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.junit.Test
import kotlin.math.PI

class Test{

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

    fun <E: GameLogicI, R: GameEngineI<E>> run(engine: (ResourcesLoader, (R) -> E) -> R, screen: (R) -> E){

        LibrarySetUp.initLibraries()
        TestWindow.INSTANCE.init()

        val fileResources = FileResourcesLoader("$directory/src/main/resources")

        val e = engine(fileResources, screen)
        e.run()
    }

    @Test
    fun testAnimator(){
        LibrarySetUp.initLibraries()
        ObjectAnimator.run(::createSnake)
    }

    @Test
    fun quaternions(){
        val q = Quaternion(Vec3(0, PI, 0))
        val e = q.toEulerAngles()
        val q1 = Quaternion(e)

        val e1 = Vec3(0.1, 0.25, 0.2)
        val q2 = Quaternion(e1).pow(3f)
        val e2 = q2.toEulerAngles()
    }
}

fun createSnake(): Storable{
    return object : AnimatedObject2D(defaultShader) {

        override var name: String = "Snake"

        override var animation: Animation = Animation("slither") // Animation("slither", 7f, "snake", (0..5).map { "snake_$it" },"slitherz")
        override val animations: List<Animation> =
            listOf(animation, Animation("backwards", 7f, "snake", (5 downTo 0).map { "snake_$it" }, "backwards"))

        override fun init() {
            super.init()
            scale = Vec2(4)
            components.add(TextureComponent(this))
            components.add(ColliderComponent(this, SoftCollisionBox(this, Vec2(), Vec2(1))))
        }

        override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
            super.render(view, projection, tickDelta)

            getComponent<TextureComponent>()?.texture?.bind()
            VertexShape.centerSquareShape2D.bindAndDraw()
        }
    }
}
