package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.TransformComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.CursorPosition
import com.pineypiney.game_engine.util.input.InputState
import com.pineypiney.game_engine.util.raycasting.Ray
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW

abstract class WindowGameLogic : GameLogic() {


	abstract override val gameEngine: WindowedGameEngineI<*>
	open val window get() = gameEngine.window
	open val input get() = gameEngine.window.input
	abstract override val renderer: WindowRendererI<*>

	override fun open() {
		super.open()

		updateAspectRatio()
		onCursorMove(gameEngine.input.mouse.lastPos, CursorPosition(Vec2(0f), Vec2(0f), Vec2i(0)))
	}

	open fun onCursorMove(cursorPos: CursorPosition, cursorDelta: CursorPosition) {
		val ray = renderer.camera.getRay(input.mouse.lastPos.screenSpace)
		checkHovers(gameObjects.getAllInteractables(), ray, cursorPos, cursorDelta)
	}

	fun checkHovers(components: Collection<InteractorComponent>, ray: Ray, cursorPos: CursorPosition, cursorDelta: CursorPosition){
		val lengths = mutableMapOf<InteractorComponent, Float>()
		for (component in components) {
			val len = component.checkHover(ray, cursorPos)
			if(len >= 0) lengths[component] = len
			else {
				// The cursor is no longer hovering over this component
				if(component.hover){
					component.hover = false
					component.onCursorExit(window, cursorPos, cursorDelta, ray)
				}
				if(component.shouldInteract()) {
					// If this is not hovered but shouldInteract returned true anyway
					// add this component to the map with a distance of -1
					lengths[component] = -1f
				}
			}
		}
		val sorted = lengths.entries.sortedBy { it.value }
		var rayReach = 0f
		var rayStopped = false
		for ((obj, l) in sorted){
			if(checkComponent(obj, l, cursorPos, cursorDelta, ray, rayStopped, rayReach)){
				rayStopped = true
				rayReach = l
			}
		}
	}

	fun checkComponent(obj: InteractorComponent, l: Float, cursorPos: CursorPosition, cursorDelta: CursorPosition, ray: Ray, rayStopped: Boolean, rayReach: Float): Boolean{

		// If the ray has been stopped
		// and this component is further that the ray has reached
		// then unhover all remaining components
		if(rayStopped && l > rayReach){
			if(obj.hover){
				obj.onCursorMove(window, cursorPos, cursorDelta, ray)
				obj.hover = false
				obj.onCursorExit(window, cursorPos, cursorDelta, ray)
			}
			return false
		}

		var ret = false
		if(l >= 0f) {
			// If this object was not hovered before then call onCursorEnter
			if (!obj.hover) {
				obj.hover = true
				obj.onCursorEnter(window, cursorPos, cursorDelta, ray)
			}
			// If this component is hovered and does not let the ray pass through
			// Then the cursor ray is stopped and all remaining items should be unhovered
			if(!obj.passThrough) ret = true
		}

		obj.onCursorMove(window, cursorPos, cursorDelta, ray)
		return ret
	}

	open fun onScroll(scrollDelta: Vec2): Int {
		for (component in gameObjects.getAllInteractables()) {
			if (component.shouldInteract()) {
				if (component.onScroll(
						window,
						scrollDelta
					) == InteractorComponent.INTERRUPT
				) return InteractorComponent.INTERRUPT
			}
		}
		return 0
	}

	open fun onInput(state: InputState, action: Int): Int {

		var interrupted = false
		for (component in gameObjects.getAllInteractables()) {
			interrupted = componentInput(component, state, action, input.mouse.lastPos, interrupted) || interrupted
		}
		if(interrupted) return InteractorComponent.INTERRUPT

		when {
			state.i == 0 && state.controlType == ControlType.MOUSE -> onPrimary(gameEngine.window, action, state.mods)
			state.i == 1 && state.controlType == ControlType.MOUSE -> onSecondary(gameEngine.window, action, state.mods)
		}
		return action
	}

	fun componentInput(component: InteractorComponent, state: InputState, action: Int, mousePos: CursorPosition, interrupted: Boolean): Boolean {
		try {
			// If a previous component interrupted the checks still depress anything that needs depressing
			if (state.triggers(InputState(GLFW.GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)) && action == 0 && interrupted && (component.pressed || component.forceUpdate)) {
				component.pressed = false
			}
			if (component.shouldInteract()) {
				if (component.onInput(window, state, action, mousePos) == InteractorComponent.INTERRUPT) return true
			}
			return false
		}

		catch (e: Exception){
			GameEngineI.logger.error("Failed to update component $component of object ${component.parent}")
			e.printStackTrace()
			return false
		}
	}

	open fun onType(char: Char): Int {
		for (component in gameObjects.getAllInteractables()) {
			if (component.shouldInteract()) {
				if (component.onType(
						window,
						char
					) == InteractorComponent.INTERRUPT
				) return InteractorComponent.INTERRUPT
			}
		}
		return 0
	}

	open fun onPrimary(window: WindowI, action: Int, mods: Byte) {}
	open fun onSecondary(window: WindowI, action: Int, mods: Byte) {}

	open fun setFullscreen(monitor: Monitor?) {
		window.monitor = monitor
	}

	open fun toggleFullscreen() {
		setFullscreen(if (window.fullScreen) null else Monitor.primary)
	}

	open fun updateAspectRatio() {
		renderer.updateAspectRatio(window, gameObjects)
		val viewport = renderer.getViewport()
		gameObjects.forAllObjects {
			// Ensure it updates the aspect ratio of transform components first
			val components = components.filterIsInstance<UpdatingAspectRatioComponent>().sortedByDescending { it is TransformComponent }
			for (r in components) r.updateAspectRatio(viewport)
		}
	}
}