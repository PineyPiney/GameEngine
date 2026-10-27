package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.audio.AudioInputDevice
import com.pineypiney.game_engine.audio.AudioOutputDevice
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.NativeFuncs.getInt
import com.pineypiney.game_engine.util.NativeFuncs.getVec2
import com.pineypiney.game_engine.util.NativeFuncs.getVec2i
import com.pineypiney.game_engine.util.NativeFuncs.getVec4i
import com.pineypiney.game_engine.util.NativeFuncs.setVec2
import com.pineypiney.game_engine.util.NativeFuncs.setVec2i
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.SDLInputs
import com.pineypiney.game_engine.util.input.gamepads.SDLGamepadInput
import com.pineypiney.game_engine.util.input.knm.SDLKeyboardInput
import com.pineypiney.game_engine.util.input.knm.SDLMouseInput
import glm_.f
import glm_.has
import glm_.hasnt
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import kool.free
import org.lwjgl.glfw.GLFW
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.opengl.WGL
import org.lwjgl.sdl.*
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VkInstance
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.LongBuffer

open class SDLWindow(name: String, width: Int, height: Int, val opengl: Boolean, flags: Long) : Window {


	override val api: WindowApi get() = SDLManager
	override val windowHandle: Long = SDLVideo.SDL_CreateWindow(name, width, height, flags)
	override val input: Inputs = SDLInputs(this)
	override var audioInputDevice: AudioInputDevice? = null
	override var audioOutputDevice: AudioOutputDevice? = null

	override var vSync: Boolean
		get() = getInt(windowHandle, SDLVideo::SDL_GetWindowSurfaceVSync) != SDLVideo.SDL_WINDOW_SURFACE_VSYNC_DISABLED
		set(value) { SDLVideo.SDL_SetWindowSurfaceVSync(windowHandle, value.i) }

	val keyboardState: ByteBuffer = SDLKeyboard.SDL_GetKeyboardState()!!

	override var title: String
		get() = SDLVideo.SDL_GetWindowTitle(windowHandle)!!
		set(value) { SDLVideo.SDL_SetWindowTitle(windowHandle, value) }

	override var pos: Vec2i
		get() = getVec2i(windowHandle, SDLVideo::SDL_GetWindowPosition)
		set(value) = setVec2i(windowHandle, SDLVideo::SDL_SetWindowPosition, value)

	final override var size: Vec2i
		get() = getVec2i(windowHandle, SDLVideo::SDL_GetWindowSize)
		set(value) = setVec2i(windowHandle, GLFW::glfwSetWindowSize, value)

	override val framebufferSize: Vec2i
		get() = getVec2i(windowHandle, SDLVideo::SDL_GetWindowSizeInPixels)

	override val frameSize: Vec4i
		get() = getVec4i(windowHandle, SDLVideo::SDL_GetWindowBordersSize)

	override val contentScale: Vec2
		get() = Vec2(SDLVideo.SDL_GetWindowDisplayScale(windowHandle))

	override var opacity: Float
		get() = SDLVideo.SDL_GetWindowOpacity(windowHandle)
		set(value) { SDLVideo.SDL_SetWindowOpacity(windowHandle, value) }

	override var cursorPos: Vec2d
		get() = Vec2d(getVec2(SDLMouse::SDL_GetGlobalMouseState) - pos)
		set(value) = setVec2(windowHandle, SDLMouse::SDL_WarpMouseInWindow, Vec2(value))

	override var width: Int
		get() = size.x
		set(value) {
			size = Vec2i(value, size.y)
		}

	override var height: Int
		get() = size.y
		set(value) {
			size = Vec2i(size.x, value)
		}

	final override var aspectRatio = size.run { x.f / y }; private set

	override val focused: Boolean
		get() = SDLVideo.SDL_GetWindowFlags(windowHandle) has SDLVideo.SDL_WINDOW_INPUT_FOCUS

	override val minimised: Boolean
		get() = SDLVideo.SDL_GetWindowFlags(windowHandle) has SDLVideo.SDL_WINDOW_MINIMIZED

	override var decorated: Boolean
		get() = SDLVideo.SDL_GetWindowFlags(windowHandle) hasnt SDLVideo.SDL_WINDOW_BORDERLESS
		set(value) { SDLVideo.SDL_SetWindowBordered(windowHandle, value) }

	override var autoMinimise: Boolean
		get() = SDLHints.SDL_GetHint(SDLHints.SDL_HINT_VIDEO_MINIMIZE_ON_FOCUS_LOSS) == "1"
		set(value) { SDLHints.SDL_SetHint(SDLHints.SDL_HINT_VIDEO_MINIMIZE_ON_FOCUS_LOSS, value.toString()) }

	override val maximised: Boolean
		get() = SDLVideo.SDL_GetWindowFlags(windowHandle) has SDLVideo.SDL_WINDOW_MAXIMIZED

	override val fullScreen: Boolean
		get() = SDLVideo.SDL_GetWindowFlags(windowHandle) has SDLVideo.SDL_WINDOW_FULLSCREEN

	override var shouldClose: Boolean = false

	override val monitor: SDLDisplay?
		get() = SDLVideo.SDL_GetDisplayForWindow(windowHandle).let { if (it == 0) null else SDLDisplay(it) }

	override val videoMode: SDLVideoMode get() = (monitor ?: SDLDisplay.primary).videoMode

	private var resizeCallback: SDLWindow.() -> Unit = {}
	private var framebufferResizeCallback: SDLWindow.(x: Int, y: Int) -> Unit = {_, _ -> }
	private var minimiseCallback: SDLWindow.() -> Unit = {}
	private var maximiseCallback: SDLWindow.() -> Unit = {}
	private var focusCallback: SDLWindow.(Boolean) -> Unit = {}
	private var contentScaleCallback: SDLWindow.() -> Unit = {}

	override fun init() {
		configureAL()
	}

	protected open fun configureAL() {
		setAudioOutput(null)

		val cCaps = ALC.getCapabilities()
		AL.createCapabilities(cCaps)
	}

	override fun setAudioOutput(name: String?) {
		audioOutputDevice?.close()
		audioOutputDevice = AudioOutputDevice(name)
	}

	override fun setAudioInput(name: String?, freq: Int, format: Int, samples: Int) {
		audioInputDevice?.stop()
		audioInputDevice?.close()
		audioInputDevice = AudioInputDevice(name, freq, format, samples)
		audioInputDevice?.start()
	}

	override fun focus() {
		SDLVideo.SDL_RaiseWindow(windowHandle)
	}

	override fun minimise() {
		SDLVideo.SDL_MinimizeWindow(windowHandle)
	}

	override fun maximise() {
		SDLVideo.SDL_MaximizeWindow(windowHandle)
	}

	override fun restore() {
		SDLVideo.SDL_RestoreWindow(windowHandle)
	}

	override fun getKey(key: Int): Int {
		val sdl = keys.entries.firstOrNull { it.value == key }?.key ?: return 0
		return keyboardState[sdl].toInt()
	}

	override fun setFullScreen(fullScreen: Boolean, monitor: Monitor) {
		if(fullScreen) maximise()
		moveToMonitor(monitor)
		SDLVideo.SDL_SetWindowFullscreen(windowHandle, fullScreen)
		if(!fullScreen) restore()
	}

	override fun createSystemCursor(systemCursor: SystemCursor): Cursor = SDLCursor(SDLMouse.SDL_CreateSystemCursor(systemCursor.sdl))

	override fun createCursor(texture: InputStream, point: Vec2i): Cursor = SDLCursor(texture, point)

	/**
	 * Set the cursor for the window
	 *
	 * @param cursor The handle for the new cursor
	 */
	override fun setCursor(cursor: Long) {
		SDLMouse.SDL_SetCursor(cursor)
	}

	/**
	 * Set the cursor to a custom cursor
	 *
	 * @param cursor The cursor to be set
	 */
	override fun setCursor(cursor: Cursor) {
		setCursor(cursor.handle)
	}

	override fun getCursorMode(): CursorMode {
		return if(SDLMouse.SDL_GetWindowRelativeMouseMode(windowHandle)) CursorMode.DISABLED
		else if(SDLVideo.SDL_GetWindowMouseGrab(windowHandle)) CursorMode.CAPTURED
		else if(SDLMouse.SDL_CursorVisible()) CursorMode.NORMAL
		else CursorMode.HIDDEN
	}

	override fun setCursorMode(mode: CursorMode) {
		when(mode){
			CursorMode.NORMAL -> {
				SDLVideo.SDL_SetWindowMouseGrab(windowHandle, false)
				SDLMouse.SDL_SetWindowRelativeMouseMode(windowHandle, false)
				SDLMouse.SDL_ShowCursor()
			}
			CursorMode.HIDDEN -> {
				SDLVideo.SDL_SetWindowMouseGrab(windowHandle, false)
				SDLMouse.SDL_SetWindowRelativeMouseMode(windowHandle, false)
				SDLMouse.SDL_HideCursor()
			}
			CursorMode.DISABLED -> {
				SDLMouse.SDL_SetWindowRelativeMouseMode(windowHandle, true)
				SDLMouse.SDL_HideCursor()
			}
			CursorMode.CAPTURED -> {
				SDLVideo.SDL_SetWindowMouseGrab(windowHandle, true)
				SDLMouse.SDL_SetWindowRelativeMouseMode(windowHandle, false)
				SDLMouse.SDL_ShowCursor()
			}
		}
	}

	/**
	 * Set the window icon
	 *
	 * @param icon The input stream for the data for the new icon
	 */
	override fun setIcon(icon: InputStream) {
		val surface = createSurface(icon)
		SDLVideo.SDL_SetWindowIcon(windowHandle, surface)
		SDLSurface.SDL_DestroySurface(surface)
	}

	override fun setResizeCallback(callback: Window.() -> Unit) {
		resizeCallback = callback
	}

	override fun setFrameBufferResizeCallback(callback: Window.() -> Unit) {
		framebufferResizeCallback = { x: Int, y: Int ->
			if(y != 0){
				aspectRatio = x.toFloat() / y.toFloat()
				callback()
			}
		}
	}

	override fun setFocusCallback(callback: Window.(Boolean) -> Unit) {
		focusCallback = callback
	}

	override fun setMinimiseCallback(callback: Window.() -> Unit) {
		minimiseCallback = callback
	}

	override fun setMaximiseCallback(callback: Window.() -> Unit) {
		maximiseCallback = callback
	}

	override fun setContentScaleCallback(callback: Window.() -> Unit) {
		contentScaleCallback = callback
	}

	override fun update() {
		if(opengl) SDLVideo.SDL_GL_SwapWindow(windowHandle)
		val event = SDL_Event.calloc()
		while(SDLEvents.SDL_PollEvent(event)){
			when(event.type()){
				SDLEvents.SDL_EVENT_QUIT -> shouldClose = true
				in SDLEvents.SDL_EVENT_DISPLAY_FIRST..SDLEvents.SDL_EVENT_DISPLAY_LAST -> onDisplayEvent(event.display())
				in SDLEvents.SDL_EVENT_WINDOW_FIRST..SDLEvents.SDL_EVENT_WINDOW_LAST -> onWindowEvent(event.window())
				SDLEvents.SDL_EVENT_KEY_DOWN, SDLEvents.SDL_EVENT_KEY_UP -> input.getInputOrNull<SDLKeyboardInput>()?.onKey(event.key())
				SDLEvents.SDL_EVENT_TEXT_INPUT -> input.getInputOrNull<SDLKeyboardInput>()?.onType(event.text())
				SDLEvents.SDL_EVENT_MOUSE_MOTION -> input.getInputOrNull<SDLMouseInput>()?.onCursorMore(event.motion())
				SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> input.getInputOrNull<SDLMouseInput>()?.onButton(event.button())
				SDLEvents.SDL_EVENT_MOUSE_WHEEL -> input.getInputOrNull<SDLMouseInput>()?.onScroll(event.wheel())
				SDLEvents.SDL_EVENT_GAMEPAD_ADDED -> input.getInputOrNull<SDLGamepadInput>()?.onControllerConnect(event.gdevice())
				SDLEvents.SDL_EVENT_GAMEPAD_REMOVED -> input.getInputOrNull<SDLGamepadInput>()?.onControllerDisconnect(event.gdevice())
				SDLEvents.SDL_EVENT_GAMEPAD_BUTTON_DOWN, SDLEvents.SDL_EVENT_GAMEPAD_BUTTON_UP -> input.getInputOrNull<SDLGamepadInput>()?.onButton(event.gbutton())
				SDLEvents.SDL_EVENT_GAMEPAD_AXIS_MOTION -> input.getInputOrNull<SDLGamepadInput>()?.onAxis(event.gaxis())
			}
		}
		event.free()
	}

	fun onDisplayEvent(event: SDL_DisplayEvent){
		when(event.type()){

		}
	}

	fun onWindowEvent(event: SDL_WindowEvent){
		when(event.type()){
			SDLEvents.SDL_EVENT_WINDOW_RESIZED -> resizeCallback(this)
			SDLEvents.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED -> framebufferResizeCallback(this, event.data1(), event.data2())
			SDLEvents.SDL_EVENT_WINDOW_MINIMIZED -> minimiseCallback(this)
			SDLEvents.SDL_EVENT_WINDOW_MAXIMIZED -> maximiseCallback(this)
			SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED -> focusCallback(this, true)
			SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> focusCallback(this, false)
			SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED -> contentScaleCallback(this)
		}
	}

	override fun close() {
		delete()
	}

	override fun delete() {
		audioOutputDevice?.close()
		input.inputs.delete()
	}

	override fun getHWND(): Long {
		val props = SDLVideo.SDL_GetWindowProperties(windowHandle)
		return SDLProperties.SDL_GetPointerProperty(props, SDLVideo.SDL_PROP_WINDOW_WIN32_HWND_POINTER, 0L)
	}
	override fun getHGLRC(): Long = WGL.wglGetCurrentContext(null)

	override fun getPrimaryMonitor(): SDLDisplay = SDLDisplay.primary
	override fun getAllMonitors(): List<SDLDisplay> = SDLDisplay.getAllMonitors()

	override fun createVulkanSurface(instance: VkInstance, buffer: LongBuffer): Int {
		return if(SDLVulkan.SDL_Vulkan_CreateSurface(windowHandle, instance, null, buffer)) 0 else VK10.VK_ERROR_UNKNOWN
	}

	companion object {

		fun createSurface(texture: InputStream): SDL_Surface {
			val bytes = ResourcesLoader.ioResourceToByteBuffer(texture)
			val (data, info) = TextureLoader.loadTextureData(bytes, false)
			bytes.free()

			// The data must be in RGBA 32-bit format
			val surface = SDLSurface.SDL_CreateSurfaceFrom(info.x, info.y, SDLPixels.SDL_PIXELFORMAT_ABGR8888, data, info.x * 4)!!
			return surface
		}

		val keys = mapOf(
			SDLScancode.SDL_SCANCODE_SPACE to GLFW.GLFW_KEY_SPACE,
			SDLScancode.SDL_SCANCODE_APOSTROPHE to GLFW.GLFW_KEY_APOSTROPHE,
			SDLScancode.SDL_SCANCODE_COMMA to GLFW.GLFW_KEY_COMMA,
			SDLScancode.SDL_SCANCODE_MINUS to GLFW.GLFW_KEY_MINUS,
			SDLScancode.SDL_SCANCODE_PERIOD to GLFW.GLFW_KEY_PERIOD,
			SDLScancode.SDL_SCANCODE_SLASH to GLFW.GLFW_KEY_SLASH,
			SDLScancode.SDL_SCANCODE_0 to GLFW.GLFW_KEY_0,
			SDLScancode.SDL_SCANCODE_1 to GLFW.GLFW_KEY_1,
			SDLScancode.SDL_SCANCODE_2 to GLFW.GLFW_KEY_2,
			SDLScancode.SDL_SCANCODE_3 to GLFW.GLFW_KEY_3,
			SDLScancode.SDL_SCANCODE_4 to GLFW.GLFW_KEY_4,
			SDLScancode.SDL_SCANCODE_5 to GLFW.GLFW_KEY_5,
			SDLScancode.SDL_SCANCODE_6 to GLFW.GLFW_KEY_6,
			SDLScancode.SDL_SCANCODE_7 to GLFW.GLFW_KEY_7,
			SDLScancode.SDL_SCANCODE_8 to GLFW.GLFW_KEY_8,
			SDLScancode.SDL_SCANCODE_9 to GLFW.GLFW_KEY_9,
			SDLScancode.SDL_SCANCODE_SEMICOLON to GLFW.GLFW_KEY_SEMICOLON,
			SDLScancode.SDL_SCANCODE_EQUALS to GLFW.GLFW_KEY_EQUAL,
			SDLScancode.SDL_SCANCODE_A to GLFW.GLFW_KEY_A,
			SDLScancode.SDL_SCANCODE_B to GLFW.GLFW_KEY_B,
			SDLScancode.SDL_SCANCODE_C to GLFW.GLFW_KEY_C,
			SDLScancode.SDL_SCANCODE_D to GLFW.GLFW_KEY_D,
			SDLScancode.SDL_SCANCODE_E to GLFW.GLFW_KEY_E,
			SDLScancode.SDL_SCANCODE_F to GLFW.GLFW_KEY_F,
			SDLScancode.SDL_SCANCODE_G to GLFW.GLFW_KEY_G,
			SDLScancode.SDL_SCANCODE_H to GLFW.GLFW_KEY_H,
			SDLScancode.SDL_SCANCODE_I to GLFW.GLFW_KEY_I,
			SDLScancode.SDL_SCANCODE_J to GLFW.GLFW_KEY_J,
			SDLScancode.SDL_SCANCODE_K to GLFW.GLFW_KEY_K,
			SDLScancode.SDL_SCANCODE_L to GLFW.GLFW_KEY_L,
			SDLScancode.SDL_SCANCODE_M to GLFW.GLFW_KEY_M,
			SDLScancode.SDL_SCANCODE_N to GLFW.GLFW_KEY_N,
			SDLScancode.SDL_SCANCODE_O to GLFW.GLFW_KEY_O,
			SDLScancode.SDL_SCANCODE_P to GLFW.GLFW_KEY_P,
			SDLScancode.SDL_SCANCODE_Q to GLFW.GLFW_KEY_Q,
			SDLScancode.SDL_SCANCODE_R to GLFW.GLFW_KEY_R,
			SDLScancode.SDL_SCANCODE_S to GLFW.GLFW_KEY_S,
			SDLScancode.SDL_SCANCODE_T to GLFW.GLFW_KEY_T,
			SDLScancode.SDL_SCANCODE_U to GLFW.GLFW_KEY_U,
			SDLScancode.SDL_SCANCODE_V to GLFW.GLFW_KEY_V,
			SDLScancode.SDL_SCANCODE_W to GLFW.GLFW_KEY_W,
			SDLScancode.SDL_SCANCODE_X to GLFW.GLFW_KEY_X,
			SDLScancode.SDL_SCANCODE_Y to GLFW.GLFW_KEY_Y,
			SDLScancode.SDL_SCANCODE_Z to GLFW.GLFW_KEY_Z,
			SDLScancode.SDL_SCANCODE_LEFTBRACKET to GLFW.GLFW_KEY_LEFT_BRACKET,
			SDLScancode.SDL_SCANCODE_BACKSLASH to GLFW.GLFW_KEY_BACKSLASH,
			SDLScancode.SDL_SCANCODE_RIGHTBRACKET to GLFW.GLFW_KEY_RIGHT_BRACKET,
			SDLScancode.SDL_SCANCODE_GRAVE to GLFW.GLFW_KEY_GRAVE_ACCENT,

			SDLScancode.SDL_SCANCODE_ESCAPE to GLFW.GLFW_KEY_ESCAPE,
			SDLScancode.SDL_SCANCODE_RETURN to GLFW.GLFW_KEY_ENTER,
			SDLScancode.SDL_SCANCODE_TAB to GLFW.GLFW_KEY_TAB,
			SDLScancode.SDL_SCANCODE_BACKSPACE to GLFW.GLFW_KEY_BACKSPACE,
			SDLScancode.SDL_SCANCODE_INSERT to GLFW.GLFW_KEY_INSERT,
			SDLScancode.SDL_SCANCODE_DELETE to GLFW.GLFW_KEY_DELETE,
			SDLScancode.SDL_SCANCODE_RIGHT to GLFW.GLFW_KEY_RIGHT,
			SDLScancode.SDL_SCANCODE_LEFT to GLFW.GLFW_KEY_LEFT,
			SDLScancode.SDL_SCANCODE_DOWN to GLFW.GLFW_KEY_DOWN,
			SDLScancode.SDL_SCANCODE_UP to GLFW.GLFW_KEY_UP,
			SDLScancode.SDL_SCANCODE_PAGEUP to GLFW.GLFW_KEY_PAGE_UP,
			SDLScancode.SDL_SCANCODE_PAGEDOWN to GLFW.GLFW_KEY_PAGE_DOWN,
			SDLScancode.SDL_SCANCODE_HOME to GLFW.GLFW_KEY_HOME,
			SDLScancode.SDL_SCANCODE_END to GLFW.GLFW_KEY_END,
			SDLScancode.SDL_SCANCODE_CAPSLOCK to GLFW.GLFW_KEY_CAPS_LOCK,
			SDLScancode.SDL_SCANCODE_SCROLLLOCK to GLFW.GLFW_KEY_SCROLL_LOCK,
			SDLScancode.SDL_SCANCODE_NUMLOCKCLEAR to GLFW.GLFW_KEY_NUM_LOCK,
			SDLScancode.SDL_SCANCODE_PRINTSCREEN to GLFW.GLFW_KEY_PRINT_SCREEN,
			SDLScancode.SDL_SCANCODE_PAUSE to GLFW.GLFW_KEY_PAUSE,
			SDLScancode.SDL_SCANCODE_F1 to GLFW.GLFW_KEY_F1,
			SDLScancode.SDL_SCANCODE_F2 to GLFW.GLFW_KEY_F2,
			SDLScancode.SDL_SCANCODE_F3 to GLFW.GLFW_KEY_F3,
			SDLScancode.SDL_SCANCODE_F4 to GLFW.GLFW_KEY_F4,
			SDLScancode.SDL_SCANCODE_F5 to GLFW.GLFW_KEY_F5,
			SDLScancode.SDL_SCANCODE_F6 to GLFW.GLFW_KEY_F6,
			SDLScancode.SDL_SCANCODE_F7 to GLFW.GLFW_KEY_F7,
			SDLScancode.SDL_SCANCODE_F8 to GLFW.GLFW_KEY_F8,
			SDLScancode.SDL_SCANCODE_F9 to GLFW.GLFW_KEY_F9,
			SDLScancode.SDL_SCANCODE_F10 to GLFW.GLFW_KEY_F10,
			SDLScancode.SDL_SCANCODE_F11 to GLFW.GLFW_KEY_F11,
			SDLScancode.SDL_SCANCODE_F12 to GLFW.GLFW_KEY_F12,
			SDLScancode.SDL_SCANCODE_F13 to GLFW.GLFW_KEY_F13,
			SDLScancode.SDL_SCANCODE_F14 to GLFW.GLFW_KEY_F14,
			SDLScancode.SDL_SCANCODE_F15 to GLFW.GLFW_KEY_F15,
			SDLScancode.SDL_SCANCODE_F16 to GLFW.GLFW_KEY_F16,
			SDLScancode.SDL_SCANCODE_F17 to GLFW.GLFW_KEY_F17,
			SDLScancode.SDL_SCANCODE_F18 to GLFW.GLFW_KEY_F18,
			SDLScancode.SDL_SCANCODE_F19 to GLFW.GLFW_KEY_F19,
			SDLScancode.SDL_SCANCODE_F20 to GLFW.GLFW_KEY_F20,
			SDLScancode.SDL_SCANCODE_F21 to GLFW.GLFW_KEY_F21,
			SDLScancode.SDL_SCANCODE_F22 to GLFW.GLFW_KEY_F22,
			SDLScancode.SDL_SCANCODE_F23 to GLFW.GLFW_KEY_F23,
			SDLScancode.SDL_SCANCODE_F24 to GLFW.GLFW_KEY_F24,

			SDLScancode.SDL_SCANCODE_KP_0 to GLFW.GLFW_KEY_KP_0,
			SDLScancode.SDL_SCANCODE_KP_1 to GLFW.GLFW_KEY_KP_1,
			SDLScancode.SDL_SCANCODE_KP_2 to GLFW.GLFW_KEY_KP_2,
			SDLScancode.SDL_SCANCODE_KP_3 to GLFW.GLFW_KEY_KP_3,
			SDLScancode.SDL_SCANCODE_KP_4 to GLFW.GLFW_KEY_KP_4,
			SDLScancode.SDL_SCANCODE_KP_5 to GLFW.GLFW_KEY_KP_5,
			SDLScancode.SDL_SCANCODE_KP_6 to GLFW.GLFW_KEY_KP_6,
			SDLScancode.SDL_SCANCODE_KP_7 to GLFW.GLFW_KEY_KP_7,
			SDLScancode.SDL_SCANCODE_KP_8 to GLFW.GLFW_KEY_KP_8,
			SDLScancode.SDL_SCANCODE_KP_9 to GLFW.GLFW_KEY_KP_9,
			SDLScancode.SDL_SCANCODE_KP_PERIOD to GLFW.GLFW_KEY_KP_DECIMAL,
			SDLScancode.SDL_SCANCODE_KP_DIVIDE to GLFW.GLFW_KEY_KP_DIVIDE,
			SDLScancode.SDL_SCANCODE_KP_MULTIPLY to GLFW.GLFW_KEY_KP_MULTIPLY,
			SDLScancode.SDL_SCANCODE_KP_MINUS to GLFW.GLFW_KEY_KP_SUBTRACT,
			SDLScancode.SDL_SCANCODE_KP_PLUS to GLFW.GLFW_KEY_KP_ADD,
			SDLScancode.SDL_SCANCODE_KP_ENTER to GLFW.GLFW_KEY_KP_ENTER,
			SDLScancode.SDL_SCANCODE_KP_EQUALS to GLFW.GLFW_KEY_KP_EQUAL,

			SDLScancode.SDL_SCANCODE_LSHIFT to GLFW.GLFW_KEY_LEFT_SHIFT,
			SDLScancode.SDL_SCANCODE_LCTRL to GLFW.GLFW_KEY_LEFT_CONTROL,
			SDLScancode.SDL_SCANCODE_LALT to GLFW.GLFW_KEY_LEFT_ALT,
			SDLScancode.SDL_SCANCODE_LGUI to GLFW.GLFW_KEY_LEFT_SUPER,
			SDLScancode.SDL_SCANCODE_RSHIFT to GLFW.GLFW_KEY_RIGHT_SHIFT,
			SDLScancode.SDL_SCANCODE_RCTRL to GLFW.GLFW_KEY_RIGHT_CONTROL,
			SDLScancode.SDL_SCANCODE_RALT to GLFW.GLFW_KEY_RIGHT_ALT,
			SDLScancode.SDL_SCANCODE_RGUI to GLFW.GLFW_KEY_RIGHT_SUPER,
		)
	}
}