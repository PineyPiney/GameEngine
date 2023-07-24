package com.pineypiney.game_engine_test.test3D

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.WindowI
import com.pineypiney.game_engine.objects.Interactable
import com.pineypiney.game_engine.objects.game_objects.objects_2D.RenderedGameObject2D
import com.pineypiney.game_engine.objects.game_objects.objects_3D.RenderedGameObject3D
import com.pineypiney.game_engine.objects.game_objects.objects_3D.SimpleTexturedGameObject3D
import com.pineypiney.game_engine.objects.util.shapes.ArrayShape
import com.pineypiney.game_engine.objects.util.shapes.VertexShape
import com.pineypiney.game_engine.rendering.cameras.PerspectiveCamera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.AxisAlignedCuboid
import com.pineypiney.game_engine.util.maths.shapes.Cuboid
import com.pineypiney.game_engine.util.maths.vectorToEuler
import com.pineypiney.game_engine.util.raycasting.Ray
import com.pineypiney.game_engine_test.Renderer
import glm_.mat4x4.Mat4
import glm_.s
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW.*
import kotlin.math.PI

class Game3D(override val gameEngine: GameEngineI<*>): GameLogic() {

    override val camera: PerspectiveCamera = PerspectiveCamera(window)
    override val renderer: Renderer = Renderer(window)

    private val pressedKeys = mutableSetOf<Short>()
    private var moveMouse = false

    private var updateRay = true

    private val crosshair = object : RenderedGameObject2D(ShaderLoader[ResourceKey("vertex/crosshair"), ResourceKey("fragment/crosshair")]){

        override fun init() {
            super.init()
            scale = Vec2(0.15)
        }

        override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
            super.render(view, projection, tickDelta)
            VertexShape.centerSquareShape2D.bindAndDraw()
        }
    }

    private val cursorRay = object : RenderedGameObject3D(ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/plain")]){

        var shape: VertexShape = VertexShape.centerCubeShape

        override fun init() {
            super.init()
            scale = Vec3(2, 0.1, 0.1)
        }

        override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
            super.render(view, projection, tickDelta)

            shape.bindAndDraw()
        }
    }

    private val object3D = object : SimpleTexturedGameObject3D(ResourceKey("broke")){
        val box: Cuboid get() = Cuboid(position, rotation, scale)

        override fun init() {
            super.init()

            rotation = Vec3(0.4, PI/4, 1.2)
        }
    }

    private val block = object : RenderedGameObject3D(ShaderLoader[ResourceKey("vertex/3D"), ResourceKey("fragment/lit")]){

        var hovered = false

        val box = AxisAlignedCuboid(Vec3(), Vec3(1))

        override fun setUniforms() {
            super.setUniforms()
            uniforms.setFloatUniform("ambient"){0.1f}
            uniforms.setVec3Uniform("blockColour"){ if(hovered) Vec3(0.1, 0.9, 0.1) else Vec3(0.7f)}
            uniforms.setVec3Uniform("lightPosition"){Vec3(1, 5, 2)}
        }

        override fun render(view: Mat4, projection: Mat4, tickDelta: Double) {
            super.render(view, projection, tickDelta)
            VertexShape.centerCubeShape.bindAndDraw()
        }
    }

    override fun init() {
        super.init()
        glfwSetInputMode(window.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }

    override fun addObjects() {
        add(object3D.apply { translate(Vec3(-2, 0, 0)) })
        add(block)
        add(crosshair)
        add(cursorRay)
    }

    override fun render(window: WindowI, tickDelta: Double) {
        renderer.render(window, this, tickDelta)

        val speed = 10 * Timer.frameDelta
        val travel = Vec3()

        val forward = camera.cameraUp cross camera.cameraRight
        if(pressedKeys.contains('W'.s)) travel += forward
        if(pressedKeys.contains('S'.s)) travel -= forward
        if(pressedKeys.contains('A'.s)) travel -= camera.cameraRight
        if(pressedKeys.contains('D'.s)) travel += camera.cameraRight
        if(pressedKeys.contains(' '.s)) travel += camera.cameraUp
        if(pressedKeys.contains(GLFW_KEY_LEFT_CONTROL.s)) travel -= camera.cameraUp

        if(travel != Vec3(0)){
            camera.translate(travel * speed)
        }

        object3D.rotate(Vec3(0.5, 1, 1.5) * Timer.frameDelta)
        val ray = camera.getRay(input.mouse.lastPos)

        object3D.texture = if(object3D.box.intersectedBy(ray).isEmpty()) Texture.broke else TextureLoader[ResourceKey("snake/snake_0")]

        if(updateRay){
            cursorRay.position = ray.rayOrigin + (ray.direction * cursorRay.scale.x * 0.5)
            val (p, y) = vectorToEuler(ray.direction)
            cursorRay.rotation = Vec3(0, y, p)
        }
    }

    override fun onPrimary(window: WindowI, action: Int, mods: Byte) {
        super.onPrimary(window, action, mods)
        if(action == 1) updateRay = !updateRay
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
                'M' -> toggleMouse()
            }
        }


        if(action == 0) pressedKeys.remove(state.key)
        else pressedKeys.add(state.key)
        return action
    }

    override fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
        super.onCursorMove(cursorPos, cursorDelta)

        if(!moveMouse){
            input.mouse.setCursorAt(Vec2(0))
            camera.cameraYaw += cursorDelta.x * 20
            camera.cameraPitch = (camera.cameraPitch + cursorDelta.y * 20).coerceIn(-89.99, 89.99)
            camera.updateCameraVectors()
        }

        val ray = camera.getRay(cursorPos)
        block.hovered = block.box.intersectedBy(ray).isNotEmpty()
    }

    override fun updateAspectRatio(window: WindowI) {
        super.updateAspectRatio(window)
        GLFunc.viewportO = Vec2i(window.width, window.height)
    }

    fun toggleMouse(){
        moveMouse = !moveMouse
        glfwSetInputMode(window.windowHandle, GLFW_CURSOR, if(moveMouse) GLFW_CURSOR_CAPTURED else GLFW_CURSOR_DISABLED)
    }

    fun getRayShape(ray: Ray): VertexShape{
        val a = FloatArray(5)
        val b = FloatArray(3) + a + ray.direction.array + a
        return ArrayShape(b, intArrayOf(3, 3, 2))
    }
}