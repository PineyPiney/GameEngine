package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.audio.AudioInputDevice
import com.pineypiney.game_engine.audio.AudioOutputDevice
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.GLFunc
import com.pineypiney.game_engine.util.NativeFuncs.getVec2
import com.pineypiney.game_engine.util.NativeFuncs.getVec2d
import com.pineypiney.game_engine.util.NativeFuncs.getVec2i
import com.pineypiney.game_engine.util.NativeFuncs.getVec4i
import com.pineypiney.game_engine.util.NativeFuncs.setVec2d
import com.pineypiney.game_engine.util.NativeFuncs.setVec2i
import com.pineypiney.game_engine.util.extension_functions.delete
import com.pineypiney.game_engine.util.input.Inputs
import glm_.bool
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import kool.free
import org.lwjgl.glfw.*
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.stb.STBImage
import org.lwjgl.vulkan.VkInstance
import java.io.InputStream
import java.nio.LongBuffer

open class GLFWWindow(
	title: String,
	width: Int,
	height: Int,
	fullScreen: Boolean,
	vSync: Boolean,
	input: (Window) -> Inputs,
	val opengl: Boolean,
	hints: Map<Int, Int> = defaultHints
) : Window {

	override val api: WindowApi get() = GLFWManager
	final override val windowHandle: Long = createWindow(title, width, height, fullScreen, hints)
	override val input: Inputs = input(this)
	final override var audioOutputDevice: AudioOutputDevice? = null
	final override var audioInputDevice: AudioInputDevice? = null

	override var title: String
		get() = GLFW.glfwGetWindowTitle(windowHandle) ?: ""
		set(value) = GLFW.glfwSetWindowTitle(windowHandle, value)

	override var pos: Vec2i
		get() = getVec2i(windowHandle, GLFW::glfwGetWindowPos)
		set(value) = setVec2i(windowHandle, GLFW::glfwSetWindowPos, value)

	final override var size: Vec2i
		get() = getVec2i(windowHandle, GLFW::glfwGetWindowSize)
		set(value) = setVec2i(windowHandle, GLFW::glfwSetWindowSize, value)

	override val framebufferSize: Vec2i
		get() = getVec2i(windowHandle, GLFW::glfwGetFramebufferSize)

	override val frameSize: Vec4i
		get() = getVec4i(windowHandle, GLFW::glfwGetWindowFrameSize)

	override val contentScale: Vec2
		get() = getVec2(windowHandle, GLFW::glfwGetWindowContentScale)

	override var opacity: Float
		get() = GLFW.glfwGetWindowOpacity(windowHandle)
		set(value) = GLFW.glfwSetWindowOpacity(windowHandle, value)

	override var cursorPos: Vec2d
		get() = getVec2d(windowHandle, GLFW::glfwGetCursorPos)
		set(value) = setVec2d(windowHandle, GLFW::glfwSetCursorPos, value)

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

	final override var aspectRatio = size.run { x.f / y }

	override val focused: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED).bool

	override val minimised: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_ICONIFIED).bool

	override val maximised: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_MAXIMIZED).bool

	override var autoMinimise: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_AUTO_ICONIFY).bool
		set(value) = GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_AUTO_ICONIFY, value.i)

	override var decorated: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED).bool
		set(value) = GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED, value.i)

	override val fullScreen: Boolean
		get() = GLFW.glfwGetWindowMonitor(windowHandle) != 0L

	override var shouldClose: Boolean
		get() = GLFW.glfwWindowShouldClose(windowHandle)
		set(value) = GLFW.glfwSetWindowShouldClose(windowHandle, value)

	override var vSync: Boolean = vSync
		set(value) {
			field = value
			if (GLFunc.isLoaded) GLFW.glfwSwapInterval(field.i)
		}

	override val monitor: GLFWMonitor?
		get() = GLFW.glfwGetWindowMonitor(windowHandle).let { if (it == 0L) null else GLFWMonitor(it) }

	override val videoMode: GLFWVideoMode get() = (monitor ?: GLFWMonitor.primary).videoMode

	init {
		GLFW.glfwSetWindowCloseCallback(windowHandle) {
			delete()
		}
	}

	override fun init() {
		configureAL()
	}

	private fun createWindow(title: String, width: Int, height: Int, fullScreen: Boolean, hints: Map<Int, Int>): Long {

		GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default

		hints.forEach(GLFW::glfwWindowHint)

		// Create the window
		val monitor = if (fullScreen) GLFW.glfwGetPrimaryMonitor() else 0L

		val windowHandle = GLFW.glfwCreateWindow(width, height, title, monitor, 0)
		if (windowHandle == 0L) {
			throw RuntimeException("Failed to create the GLFW window")
		}

		return windowHandle
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
		GLFW.glfwFocusWindow(windowHandle)
	}

	override fun minimise() {
		GLFW.glfwIconifyWindow(windowHandle)
	}

	override fun maximise() {
		GLFW.glfwMaximizeWindow(windowHandle)
	}

	override fun restore() {
		GLFW.glfwRestoreWindow(windowHandle)
	}

	override fun getKey(key: Int): Int = GLFW.glfwGetKey(windowHandle, key)

	override fun setFullScreen(fullScreen: Boolean, monitor: Monitor) {
		val monitor = if(fullScreen) monitor as? GLFWMonitor else null
		val mode = monitor?.videoMode
		if(fullScreen) maximise()
		GLFW.glfwSetWindowMonitor(windowHandle, monitor?.handle ?: 0L, 0, 0, mode?.width ?: width, mode?.height ?: height, GLFW.GLFW_DONT_CARE)
		if(!fullScreen) restore()

	}

	override fun createSystemCursor(systemCursor: SystemCursor): Cursor = GLFWCursor(GLFW.glfwCreateStandardCursor(systemCursor.glfw))

	override fun createCursor(texture: InputStream, point: Vec2i): Cursor = GLFWCursor(texture, point)

	override fun setCursor(cursor: Long) {
		if (System.getProperty("os.name").contains("Windows")) GLFW.glfwSetCursor(windowHandle, cursor)
	}

	override fun setCursor(cursor: Cursor) {
		setCursor(cursor.handle)
	}

	override fun getCursorMode(): CursorMode {
		return GLFW.glfwGetInputMode(windowHandle, GLFW.GLFW_CURSOR).let { i -> CursorMode.entries.first{ it.glfw == i } }
	}

	override fun setCursorMode(mode: CursorMode) {
		GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, mode.glfw)
	}

	fun setIcon(icon: GLFWImage.Buffer) {
		if (System.getProperty("os.name").contains("Windows")) {
			GLFW.glfwSetWindowIcon(windowHandle, icon)
		}
	}

	/**
	 * Set the window icon
	 *
	 * @param icon The input stream for the data for the new icon
	 */
	override fun setIcon(icon: InputStream) {
		val iconByteBuffer = ResourcesLoader.ioResourceToByteBuffer(icon)
		val (loadedBuffer, v) = TextureLoader.loadTextureData(iconByteBuffer, false)
		iconByteBuffer.free()
		if (loadedBuffer == null) return

		val iconBuffer = GLFWImage.create(1)
		val iconImage = GLFWImage.create().set(v.x, v.y, loadedBuffer)
		iconBuffer.put(0, iconImage)
		setIcon(iconBuffer)

		STBImage.stbi_image_free(loadedBuffer)
	}

	override fun setResizeCallback(callback: Window.() -> Unit) {
		// Setup resize callback
		GLFW.glfwSetWindowSizeCallback(windowHandle) { _: Long, _: Int, _: Int ->
			this.callback()
		}
	}

	override fun setFrameBufferResizeCallback(callback: Window.() -> Unit) {
		GLFW.glfwSetFramebufferSizeCallback(windowHandle) { handle: Long, x: Int, y: Int ->
			if(handle == windowHandle && y != 0) {
				aspectRatio = x.toFloat() / y
				this.callback()
			}
		}
	}

	override fun setFocusCallback(callback: Window.(Boolean) -> Unit) {
		GLFW.glfwSetWindowFocusCallback(windowHandle) { _: Long, b: Boolean ->
			this.callback(b)
		}
	}

	override fun setMinimiseCallback(callback: Window.() -> Unit) {
		GLFW.glfwSetWindowIconifyCallback(windowHandle) { _: Long, _: Boolean ->
			this.callback()
		}
	}

	override fun setMaximiseCallback(callback: Window.() -> Unit) {
		GLFW.glfwSetWindowMaximizeCallback(windowHandle) { _: Long, _: Boolean ->
			this.callback()
		}
	}

	override fun setContentScaleCallback(callback: Window.() -> Unit) {
		GLFW.glfwSetWindowContentScaleCallback(windowHandle) { _: Long, _: Float, _: Float ->
			this.callback()
		}
	}

	override fun update() {
		if(opengl) GLFW.glfwSwapBuffers(windowHandle)
		GLFW.glfwPollEvents()
	}

	override fun close() {
		delete()
	}

	override fun delete() {
		audioOutputDevice?.close()
		input.inputs.delete()
	}

	override fun getHWND(): Long = GLFWNativeWin32.glfwGetWin32Window(windowHandle)
	override fun getHGLRC(): Long = GLFWNativeWGL.glfwGetWGLContext(windowHandle)

	override fun getPrimaryMonitor(): GLFWMonitor = GLFWMonitor.primary
	override fun getAllMonitors(): List<GLFWMonitor> = GLFWMonitor.getAllMonitors()

	override fun createVulkanSurface(instance: VkInstance, buffer: LongBuffer): Int {
		return GLFWVulkan.glfwCreateWindowSurface(instance, windowHandle, null, buffer)
	}

	companion object {
		val defaultHints = mapOf(
			GLFW.GLFW_VISIBLE to 0, // the window will stay hidden after creation
			GLFW.GLFW_RESIZABLE to 1, // the window will be resizable
			GLFW.GLFW_SAMPLES to 4
		)
	}
}