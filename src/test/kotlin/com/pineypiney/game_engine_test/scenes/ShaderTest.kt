package com.pineypiney.game_engine_test.scenes

import com.pineypiney.game_engine.objects.GameObject
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.rendering.ShaderRenderedComponent
import com.pineypiney.game_engine.objects.menu_items.slider.BasicActionSlider
import com.pineypiney.game_engine.objects.util.meshes.Mesh
import com.pineypiney.game_engine.rendering.DefaultWindowRenderer
import com.pineypiney.game_engine.rendering.RendererI
import com.pineypiney.game_engine.rendering.cameras.OrthographicCamera
import com.pineypiney.game_engine.resources.shaders.ShaderLoader
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniform
import com.pineypiney.game_engine.resources.shaders.uniforms.Uniforms
import com.pineypiney.game_engine.util.Colour
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.ResourceKey
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.maths.shapes.Rect2D
import com.pineypiney.game_engine.util.maths.shapes.Shape
import com.pineypiney.game_engine.window.WindowGameLogic
import com.pineypiney.game_engine.window.WindowI
import com.pineypiney.game_engine.window.WindowedGameEngineI
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

class ShaderTest(override val gameEngine: WindowedGameEngineI<*>): WindowGameLogic() {

	override val renderer = DefaultWindowRenderer<ShaderTest, OrthographicCamera>(window, OrthographicCamera(window))

	val shader = ShaderLoader[ResourceKey("vertex/menu"), ResourceKey("fragment/squircle")]
	val uniformValues = mutableMapOf<String, Any>()

	val obj = GameObject("Shader Object").apply {
		components.add(object : ShaderRenderedComponent(this, shader){
			val mesh = Mesh.cornerSquareShape
			override fun getScreenShape(): Shape<*> = mesh.shape

			override fun setUniforms() {
				super.setUniforms()
				for((name, type) in shader.uniforms){
					if(defaultUniforms.contains(name)) continue
					val uniform = uniforms[name] ?: continue
					setUniform(uniform, uniforms)
				}
				uniformValues["bottomColour"] = Colour(0xE6CEC0u).rgbValue
				uniformValues["topColour"] = Colour(0xF7E3DDu).rgbValue
				uniformValues["delta"] = .7f
				//uniforms.setVec3Uniform("colour"){ Colour(0xEED8CFu).rgbValue }
				//uniforms.setFloatUniform("delta"){ 1f }
			}

			override fun render(renderer: RendererI, tickDelta: Double) {
				shader.setUp(uniforms, renderer)
				mesh.bindAndDraw()
			}
		})
	}

	var grabState = ResizeState.NONE
	var grabPoint: Vec2? = null
	var oppositePoint = Vec2(0f)

	override fun addObjects() {
		add(obj.apply { position = Vec3(-.5f) })
		addUniforms()
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
			val shape = obj.getShape() as? Rect2D ?: return
			val min = shape.origin
			val max = shape.origin + shape.side1 + shape.side2

			grabState = ResizeState.fromPointInBounds(cursorPos.position, min, max)
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