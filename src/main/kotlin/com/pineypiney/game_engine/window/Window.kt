package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.audio.AudioInputDevice
import com.pineypiney.game_engine.audio.AudioOutputDevice
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.Cursor
import com.pineypiney.game_engine.util.GLFunc.Companion.getVec2
import com.pineypiney.game_engine.util.GLFunc.Companion.getVec2d
import com.pineypiney.game_engine.util.GLFunc.Companion.getVec2i
import com.pineypiney.game_engine.util.GLFunc.Companion.getVec4i
import com.pineypiney.game_engine.util.GLFunc.Companion.setVec2d
import com.pineypiney.game_engine.util.GLFunc.Companion.setVec2i
import glm_.bool
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.opengl.GL
import org.lwjgl.stb.STBImage
import java.io.InputStream

abstract class Window(
	title: String,
	width: Int,
	height: Int,
	fullScreen: Boolean,
	vSync: Boolean,
	hints: Map<Int, Int> = defaultHints
) : WindowI, Initialisable {

	override var vSync: Boolean = vSync
		set(value) {
			field = value
			GLFW.glfwSwapInterval(field.i)
		}

	final override val windowHandle: Long = createWindow(title, width, height, fullScreen, hints)
	final override var audioOutputDevice: AudioOutputDevice? = null
	final override var audioInputDevice: AudioInputDevice? = null

	override var title: String = title
		set(value) {
			field = value
			GLFW.glfwSetWindowTitle(windowHandle, value)
		}

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

	override val opacity: Float
		get() = GLFW.glfwGetWindowOpacity(windowHandle)

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

	final override var aspectRatio = size.run { x.f / y }; private set

	override val focused: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED).bool

	override val iconified: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_ICONIFIED).bool

	override var decorated: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED).bool
		set(value) = GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED, value.i)

	override var autoIconify: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_AUTO_ICONIFY).bool
		set(value) = GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_AUTO_ICONIFY, value.i)

	override var maximised: Boolean
		get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_MAXIMIZED).bool
		set(value) {
			if (value) GLFW.glfwMaximizeWindow(windowHandle)
			else GLFW.glfwRestoreWindow(windowHandle)
		}

	override val fullScreen: Boolean
		get() = GLFW.glfwGetWindowMonitor(windowHandle) != 0L

	override var shouldClose: Boolean
		get() = GLFW.glfwWindowShouldClose(windowHandle)
		set(value) = GLFW.glfwSetWindowShouldClose(windowHandle, value)

	override var monitor: Monitor?
		get() = GLFW.glfwGetWindowMonitor(windowHandle).let { if (it == 0L) null else Monitor(it) }
		set(value) {
			GLFW.glfwSetWindowMonitor(windowHandle, value?.handle ?: 0L, 0, 0, width, height, GLFW.GLFW_DONT_CARE)
			center()
		}
	override val videoMode: GLFWVidMode get() = (monitor ?: Monitor.primary).videoMode

	init {
		GLFW.glfwSetWindowCloseCallback(windowHandle, ::close)
	}

	override fun init() {
		configureGL()
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

	protected open fun configureGL() {
		// Make the OpenGL context current
		GLFW.glfwMakeContextCurrent(windowHandle)
		// Make the window visible
		GLFW.glfwShowWindow(windowHandle)

		GL.createCapabilities()

		if (vSync) {
			// Enable v-sync
			GLFW.glfwSwapInterval(1)
		}
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

	override fun iconify() {
		GLFW.glfwIconifyWindow(windowHandle)
	}

	fun center() {
		videoMode.let {
			pos = Vec2i(it.width() - width, it.height() - height) / 2
		}
	}

	/**
	 * Set the cursor for the window
	 *
	 * @param cursor The handle for the new cursor
	 */
	override fun setCursor(cursor: Long) {
		if (System.getProperty("os.name").contains("Windows")) GLFW.glfwSetCursor(windowHandle, cursor)
	}

	/**
	 * Set the cursor to a custom cursor
	 *
	 * @param cursor The cursor to be set
	 */
	override fun setCursor(cursor: Cursor) {
		setCursor(cursor.handle)
	}

	/**
	 * Set the window icon
	 *
	 * @param icon The pixel data for the new icon
	 */
	override fun setIcon(icon: GLFWImage.Buffer) {
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
		val iconByteBuffer = ResourcesLoader.ioResourceToByteBuffer(icon, 1024)
		val (loadedBuffer, v) = TextureLoader.loadTextureData(iconByteBuffer, false)
		if (loadedBuffer == null) return

		val iconBuffer = GLFWImage.create(1)
		val iconImage = GLFWImage.create().set(v.x, v.y, loadedBuffer)
		iconBuffer.put(0, iconImage)
		setIcon(iconBuffer)

		STBImage.stbi_image_free(loadedBuffer)
	}

	override fun setResizeCallback(callback: WindowI.() -> Unit) {
		// Setup resize callback
		GLFW.glfwSetWindowSizeCallback(windowHandle) { _: Long, _: Int, _: Int ->
			this.callback()
		}
	}

	override fun setFrameBufferResizeCallback(callback: WindowI.() -> Unit) {
		GLFW.glfwSetFramebufferSizeCallback(windowHandle) { handle: Long, x: Int, y: Int ->
			if(handle == windowHandle && y != 0) {
				aspectRatio = x.toFloat() / y
				this.callback()
			}
		}
	}

	override fun setIconifyCallback(callback: WindowI.() -> Unit) {
		GLFW.glfwSetWindowIconifyCallback(windowHandle) { _: Long, _: Boolean ->
			this.callback()
		}
	}

	override fun setMaximiseCallback(callback: WindowI.() -> Unit) {
		GLFW.glfwSetWindowMaximizeCallback(windowHandle) { _: Long, _: Boolean ->
			this.callback()
		}
	}

	override fun setFocusCallback(callback: WindowI.() -> Unit) {
		GLFW.glfwSetWindowFocusCallback(windowHandle) { _: Long, _: Boolean ->
			this.callback()
		}
	}

	override fun setRefreshCallback(callback: WindowI.() -> Unit) {
		GLFW.glfwSetWindowRefreshCallback(windowHandle) { _: Long ->
			this.callback()
		}
	}

	override fun setContentScaleCallback(callback: WindowI.() -> Unit) {
		GLFW.glfwSetWindowContentScaleCallback(windowHandle) { _: Long, x: Float, y: Float ->
			this.callback()
		}
	}

	override fun update() {
		GLFW.glfwSwapBuffers(windowHandle)
		GLFW.glfwPollEvents()
	}

	override fun close(handle: Long) {
		delete()
	}

	override fun delete() {
		audioOutputDevice?.close()
	}

	companion object {
		val defaultHints = mapOf(
			GLFW.GLFW_VISIBLE to 0, // the window will stay hidden after creation
			GLFW.GLFW_RESIZABLE to 1, // the window will be resizable
			GLFW.GLFW_CONTEXT_VERSION_MAJOR to 4,
			GLFW.GLFW_CONTEXT_VERSION_MINOR to 6,
			GLFW.GLFW_OPENGL_PROFILE to GLFW.GLFW_OPENGL_CORE_PROFILE,
			GLFW.GLFW_OPENGL_FORWARD_COMPAT to 1,
		)
	}
}