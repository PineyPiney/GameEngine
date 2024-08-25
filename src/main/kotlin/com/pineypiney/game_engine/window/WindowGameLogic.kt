package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.GameLogic
import com.pineypiney.game_engine.objects.components.InteractorComponent
import com.pineypiney.game_engine.objects.components.UpdatingAspectRatioComponent
import com.pineypiney.game_engine.rendering.WindowRendererI
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.input.ControlType
import com.pineypiney.game_engine.util.input.InputState
import glm_.vec2.Vec2
import org.lwjgl.glfw.GLFW

abstract class WindowGameLogic : GameLogic() {


	abstract override val gameEngine: WindowedGameEngineI<*>
	open val window get() = gameEngine.window
	open val input get() = gameEngine.window.input
	abstract override val renderer: WindowRendererI<*>

	override fun open() {
		// Force update everything
		gameObjects.update(0f)

		// Reset textures so that the last bound texture isn't carried over
		Texture.broke.bind()

		updateAspectRatio(window)
		onCursorMove(gameEngine.input.mouse.lastPos, Vec2(0f))
	}

	open fun onCursorMove(cursorPos: Vec2, cursorDelta: Vec2) {
		val ray = renderer.camera.getRay(input.mouse.screenSpaceCursor())
		val lengths = mutableMapOf<InteractorComponent, Float>()
		for (component in gameObjects.getAllInteractables()) {
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
		var rayStopped = false
		for ((obj, l) in sorted){

			// If the ray has been stopped then unhover all remaining components
			if(rayStopped){
				if(obj.hover){
					obj.onCursorMove(window, cursorPos, cursorDelta, ray)
					obj.hover = false
					obj.onCursorExit(window, cursorPos, cursorDelta, ray)
				}
				continue
			}

			if(l >= 0f) {
				// If this object was not hovered before then call onCursorEnter
				if (!obj.hover) {
					obj.hover = true
					obj.onCursorEnter(window, cursorPos, cursorDelta, ray)
				}
				// If this component is hovered and does not let the ray pass through
				// Then the cursor ray is stopped and all remaining items should be unhovered
				if(!obj.passThrough) rayStopped = true
			}

			obj.onCursorMove(window, cursorPos, cursorDelta, ray)

		}
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

		val mousePos = input.mouse.lastPos
		var interrupted = false
		for (component in gameObjects.getAllInteractables()) {
			try {
				// If a previous component interrupted the checks still depress anything that needs depressing
				if(state.triggers(InputState(GLFW.GLFW_MOUSE_BUTTON_1, ControlType.MOUSE)) && action == 0 && interrupted && (component.pressed || component.forceUpdate)){
					component.pressed = false
				}
				if (component.shouldInteract()) {
					if (component.onInput(
							window,
							state,
							action,
							mousePos
						) == InteractorComponent.INTERRUPT
					) interrupted = true
				}
			}
			catch (e: Exception){
				GameEngineI.logger.error("Failed to update component $component of object ${component.parent}")
				e.printStackTrace()
			}
		}
		if(interrupted) return InteractorComponent.INTERRUPT

		when {
			state.i == 0 && state.controlType == ControlType.MOUSE -> onPrimary(gameEngine.window, action, state.mods)
			state.i == 1 && state.controlType == ControlType.MOUSE -> onSecondary(gameEngine.window, action, state.mods)
		}
		return action
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

	open fun updateAspectRatio(window: WindowI) {
		renderer.updateAspectRatio(window, gameObjects)
		for (r in gameObjects.getAllComponents().filterIsInstance<UpdatingAspectRatioComponent>()) r.updateAspectRatio(
			renderer
		)

	}
}