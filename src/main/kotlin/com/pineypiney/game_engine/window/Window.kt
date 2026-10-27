package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.audio.AudioInputDevice
import com.pineypiney.game_engine.audio.AudioOutputDevice
import com.pineypiney.game_engine.objects.Initialisable
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.input.Inputs
import com.pineypiney.game_engine.util.input.knm.CursorPosition
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import org.lwjgl.glfw.GLFW
import org.lwjgl.openal.AL10
import org.lwjgl.vulkan.VkInstance
import java.io.InputStream
import java.nio.LongBuffer


interface Window: Initialisable {

	val api: WindowApi
	val input: Inputs
	val windowHandle: Long
	val audioOutputDevice: AudioOutputDevice?
	val audioInputDevice: AudioInputDevice?

	/**
	 * Window Attributes
	 *
	 * - [title]: The title of the window
	 * - [pos]: The position of the window in pixels from the top left of the screen
	 * - [size]: The size of the screen in pixels
	 * - [framebufferSize]: The size of the FrameBuffer for this window
	 * - [frameSize]: The size of each edge of the window
	 * - [contentScale]: The content scale of the window
	 * - [opacity]: The opacity of the window
	 * - [cursorPos]: The position of the cursor in pixels from the top left of the content area of the window
	 * - [width]: The width of the window in pixels
	 * - [height]: The height of the window in pixels
	 * - [aspectRatio]: The ratio of the width to the height of the window
	 * - [focused]: Whether the window has input focus
	 * - [minimised]: Whether the window is minimised
	 * - [maximised]: Whether the window is maximised
	 * - [autoMinimise]: Whether the window should auto minimised. This is means that if the window is fullscreen,
	 *                  then when it is minimised the monitors Video Mode is set back to default. If making a
	 *                  full screen window, this should normally be set to true
	 * - [decorated]: Whether the window is decorated
	 * - [fullScreen]: Whether the window is attached to a monitor and is in fullscreen or not
	 * - [shouldClose]: Whether the window should close
	 * - [vSync]: Whether the video is vSynced
	 * - [monitor]
	 * - [videoMode]
	 */
	var title: String
	var pos: Vec2i
	var size: Vec2i
	val framebufferSize: Vec2i
	val frameSize: Vec4i
	val contentScale: Vec2
	var opacity: Float
	var cursorPos: Vec2d
	var width: Int
	var height: Int
	val aspectRatio: Float

	val focused: Boolean
	val minimised: Boolean
	val maximised: Boolean
	var autoMinimise: Boolean
	var decorated: Boolean
	val fullScreen: Boolean
	var shouldClose: Boolean
	var vSync: Boolean
	val monitor: Monitor?
	val videoMode: VideoMode

	fun setAudioOutput(name: String? = null)
	fun setAudioInput(
		name: String? = null,
		freq: Int = 44100,
		format: Int = AL10.AL_FORMAT_STEREO16,
		samples: Int = 1024
	)

	fun focus()

	fun minimise()

	fun maximise()

	fun restore()

	fun getKey(key: Int): Int

	/**
	 * Get the pixel position of the cursor on this window from the bottom left
	 */
	fun getCursorPos(): CursorPosition {
		val (xf, yf) = Vec2(cursorPos)

		val screenSpace = Vec2(xf * 2f / size.x - 1f, 1f - yf * 2f / size.y)
		val newPos = CursorPosition(Vec2(screenSpace.x * aspectRatio, screenSpace.y), screenSpace, Vec2i(xf, size.y - yf))
		return newPos
	}

	fun center() {
		videoMode.let {
			pos = Vec2i(it.width - width, it.height - height) / 2
		}
	}

	fun moveToMonitor(monitor: Monitor){
		pos = monitor.pos
	}

	fun setFullScreen(fullScreen: Boolean, monitor: Monitor = getPrimaryMonitor())

	fun createSystemCursor(systemCursor: SystemCursor): Cursor

	fun createCursor(texture: InputStream, point: Vec2i): Cursor

	fun createCursor(texture: String, point: Vec2i): Cursor? {
		val stream = ResourcesLoader.INSTANCE.getStream(texture) ?: return null
		return createCursor(stream, point)
	}

	/**
	 * Set the cursor for the window
	 *
	 * @param cursor The handle for the new cursor
	 */
	fun setCursor(cursor: Long)

	/**
	 * Set the cursor to a custom cursor
	 *
	 * @param cursor The cursor to be set
	 */
	fun setCursor(cursor: Cursor)

	fun getCursorMode(): CursorMode

	/**
	 * Set the cursor mode
	 *
	 * @param mode One of [GLFW.GLFW_CURSOR_NORMAL], [GLFW.GLFW_CURSOR_HIDDEN], [GLFW.GLFW_CURSOR_DISABLED] or [GLFW.GLFW_CURSOR_CAPTURED]
	 */
	fun setCursorMode(mode: CursorMode)


	/**
	 * Set the window icon
	 *
	 * @param icon The input stream for the data for the new icon
	 */
	fun setIcon(icon: InputStream)
	fun setResizeCallback(callback: Window.() -> Unit)
	fun setFrameBufferResizeCallback(callback: Window.() -> Unit)
	fun setFocusCallback(callback: Window.(Boolean) -> Unit)
	fun setMinimiseCallback(callback: Window.() -> Unit)
	fun setMaximiseCallback(callback: Window.() -> Unit)
	fun setContentScaleCallback(callback: Window.() -> Unit)
	fun update()
	fun close()

	fun getHWND(): Long
	fun getHGLRC(): Long

	fun getPrimaryMonitor(): Monitor
	fun getAllMonitors(): List<Monitor>

	fun createVulkanSurface(instance: VkInstance, buffer: LongBuffer): Int
}