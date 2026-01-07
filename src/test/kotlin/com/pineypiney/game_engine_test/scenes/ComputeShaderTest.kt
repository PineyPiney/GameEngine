package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.Timer
import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.Component
import com.pineypiney.game_engine.objects.components.FPSCounter
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.MeshedTextureComponent
import com.pineypiney.game_engine.objects.components.rendering.PreRenderComponent
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.objects.text.Text
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Shape2D
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL42C
import org.lwjgl.opengl.GL43C

@Suppress("UNUSED")
class ComputeShaderTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<ComputeShaderTest, OrthographicCamera>(window, OrthographicCamera(window))

	val shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/texture")]
	val uniformValues = mutableMapOf<String, Any>()
	val compute = ShaderLoader[ResourceKey("compute/gradient")]

	val texture = Texture("ComputeTexture", TextureLoader.createTexture(null, 1024, 1024, GL43C.GL_RGBA, GL30C.GL_RGBA, debug = true))
	init {
		GL43C.glBindImageTexture(0, texture.texturePointer, 0, false, 0, GL43C.GL_READ_WRITE, GL43C.GL_RGBA8UI)
	}

	val obj = GameObject("Shader Object").apply {
		components.add(object : Component(this), PreRenderComponent{
			override val whenVisible: Boolean = true

			override fun preRender(renderer: RendererI, tickDelta: Double) {
				texture.bind()
				compute.use()
				compute.setFloat("time", Timer.time.toFloat())
				compute.setFloat("speed", 500f)
				compute.dispatch(32, 32)

				// make sure writing to image has finished before read
				GL42C.glMemoryBarrier(GL42C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
			}
		})
		components.add(MeshedTextureComponent(this, texture, shader))
	}

	var grabState = ResizeState.NONE
	var grabPoint: Vec2? = null
	var oppositePoint = Vec2(0f)

	override fun addObjects() {
		add(obj.apply { position = Vec3(-.5f) })
		addUniforms()
		add(FPSCounter.createCounterWithText(GameObject("FPS Text").apply { relative(Vec3(-1f, 0f, 0f), Vec2(1f))}, 2.0, "FPS: $", Text.Params(fontSize = 24)))
	}

	fun addUniforms(){

		var y = 0
		for((name, type) in shader.uniforms){
			if(defaultUniforms.contains(name)) continue
			when(type){
				"float" -> {
					y -= 50
					val slider = BasicActionSlider("$name Slider", Vec2i(-200, y+5), Vec2i(180, 40), Vec2(1f), 0f, 1f, .5f){
						uniformValues[name] = it.value
					}
					add(slider)
				}
			}
		}
	}

	override fun render(tickDelta: Double) {
		renderer.render(this, tickDelta)
	}

	override fun onInput(state: InputState, action: Int): Int {
		if(super.onInput(state, action) == InteractorComponent.INTERRUPT) return InteractorComponent.INTERRUPT

		if(action == 1){
			if(state.i == GLFW_KEY_ESCAPE){
				window.shouldClose = true
			}
			else when(state.c){
				'F' -> toggleFullscreen()
			}
		}
		return action
	}

	override fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
		super.onCursorMove(cursorPos, cursorDelta)

		if(grabPoint == null) {
			val shape = obj.getShape() as? Shape2D ?: return

			grabState = ResizeState.fromPointInBounds(cursorPos.position, shape.min, shape.max)
			window.setCursor(GLFW.glfwCreateStandardCursor(grabState.glfwCursor))
		}
		else {
			when(grabState) {
				ResizeState.N -> obj.scale = Vec3(obj.scale.x, cursorPos.position.y - oppositePoint.y, obj.scale.z)
				ResizeState.S -> {
					obj.position = Vec3(obj.position.x, cursorPos.position.y, obj.position.z)
					obj.scale = Vec3(obj.scale.x, oppositePoint.y - cursorPos.position.y, obj.scale.z)
				}
				ResizeState.E -> obj.scale = Vec3(cursorPos.position.x - oppositePoint.x, obj.scale.y, obj.scale.z)
				ResizeState.W -> {
					obj.position = Vec3(cursorPos.position.x, obj.position.y, obj.position.z)
					obj.scale = Vec3(oppositePoint.x - cursorPos.position.x, obj.scale.y, obj.scale.z)
				}
				ResizeState.NE -> {
					obj.scale = Vec3(cursorPos.position - oppositePoint, obj.scale.z)
				}
				ResizeState.SE -> {
					obj.position = Vec3(obj.position.x, cursorPos.position.y, obj.position.z)
					obj.scale = Vec3(cursorPos.position.x - oppositePoint.x, oppositePoint.y - cursorPos.position.y, obj.scale.z)
				}
				ResizeState.SW -> {
					obj.position = Vec3(cursorPos.position, obj.position.z)
					obj.scale = Vec3(oppositePoint - cursorPos.position, obj.scale.z)
				}
				ResizeState.NW -> {
					obj.position = Vec3(cursorPos.position.x, obj.position.y, obj.position.z)
					obj.scale = Vec3(oppositePoint.x - cursorPos.position.x, cursorPos.position.y - oppositePoint.y, obj.scale.z)
				}
				ResizeState.CENTER -> obj.position = Vec3(cursorPos.position + oppositePoint, obj.position.z)
				else -> {}
			}
		}
	}

	override fun onPrimary(window: WindowI, action: Int, mods: Byte) {
		super.onPrimary(window, action, mods)

		when(action){
			GLFW.GLFW_RELEASE -> grabPoint = null
			GLFW.GLFW_PRESS -> {
				grabPoint = window.input.mouse.lastPos.position
				when(grabState){
					ResizeState.N -> oppositePoint.y = obj.position.y
					ResizeState.S -> oppositePoint.y = obj.position.y + obj.scale.y
					ResizeState.E -> oppositePoint.x = obj.position.x
					ResizeState.W -> oppositePoint.x = obj.position.x + obj.scale.x
					ResizeState.NE -> oppositePoint = Vec2(obj.position)
					ResizeState.SE -> oppositePoint = Vec2(obj.position.x, obj.position.y + obj.scale.y)
					ResizeState.SW -> oppositePoint = Vec2(obj.position + obj.scale)
					ResizeState.NW -> oppositePoint = Vec2(obj.position.x + obj.scale.x, obj.position.y)
					ResizeState.CENTER -> oppositePoint = Vec2(obj.position) - grabPoint!!
					else -> {}
				}
			}
		}
	}

	override fun updateAspectRatio() {
		super.updateAspectRatio()
		GLFunc.viewportO = Vec2i(window.width, window.height)
	}

	@Suppress("UNCHECKED_CAST")
	inline fun <reified U: Uniform<E>, E : Any> setUniform(uniform: U, uniforms: Uniforms){
		uniforms.set(uniform.name) { r ->
			uniformValues[uniform.name] as? E
		}
	}

	companion object {
		val defaultUniforms = setOf("model", "view", "projection", "guiProjection", "viewport", "viewPos")
	}

	enum class ResizeState(val glfwCursor: Int) {
		N(GLFW.GLFW_RESIZE_NS_CURSOR),
		E(GLFW.GLFW_RESIZE_EW_CURSOR),
		S(GLFW.GLFW_RESIZE_NS_CURSOR),
		W(GLFW.GLFW_RESIZE_EW_CURSOR),
		NE(GLFW.GLFW_RESIZE_NESW_CURSOR),
		SE(GLFW.GLFW_RESIZE_NWSE_CURSOR),
		SW(GLFW.GLFW_RESIZE_NESW_CURSOR),
		NW(GLFW.GLFW_RESIZE_NWSE_CURSOR),
		CENTER(GLFW.GLFW_RESIZE_ALL_CURSOR),
		NONE(GLFW.GLFW_ARROW_CURSOR);

		companion object {
			fun fromPointInBounds(pos: Vec2, min: Vec2, max: Vec2, hoverWidth: Float = 0.02f): ResizeState{
				val inBounds = min.x < pos.x + hoverWidth && pos.x - hoverWidth < max.x &&
						min.y < pos.y + hoverWidth && pos.y - hoverWidth < max.y
				if(!inBounds) return NONE

				val nearLeft = pos.x - min.x <= hoverWidth
				val nearRight = max.x - pos.x <= hoverWidth
				val nearBottom = pos.y - min.y <= hoverWidth
				val nearTop = max.y - pos.y <= hoverWidth

				return if(nearLeft){
					when {
						nearBottom -> SW
						nearTop -> NW
						else -> W
					}
				}
				else if(nearRight){
					when {
						nearBottom -> SE
						nearTop -> NE
						else -> E
					}
				}
				else if(nearBottom) S else if(nearTop) N
				else CENTER
			}
		}
	}
}