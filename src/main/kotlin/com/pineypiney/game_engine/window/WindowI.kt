package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.audio.AudioInputDevice
import com.pineypiney.game_engine.audio.AudioOutputDevice
import com.pineypiney.game_engine.util.Cursor
import com.pineypiney.game_engine.util.input.Inputs
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.openal.AL10
import java.io.InputStream


interface WindowI {

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
	 * - [iconified]: Whether the window is iconified
	 * - [decorated]: Whether the window is decorated
	 * - [autoIconify]: Whether the window should auto iconify. This is means that if the window is fullscreen,
	 *                  then when it is iconified the monitors Video Mode is set back to default. If making a
	 *                  full screen window, this should normally be set to true
	 * - [maximised]: Whether the window is maximised
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
	val opacity: Float
	var cursorPos: Vec2d
	var width: Int
	var height: Int
	val aspectRatio: Float
	val focused: Boolean
	val iconified: Boolean
	var decorated: Boolean
	var autoIconify: Boolean
	var maximised: Boolean
	val fullScreen: Boolean
	var shouldClose: Boolean
	var vSync: Boolean
	var monitor: Monitor?
	val videoMode: GLFWVidMode

	fun setAudioOutput(name: String? = null)
	fun setAudioInput(
		name: String? = null,
		freq: Int = 44100,
		format: Int = AL10.AL_FORMAT_STEREO16,
		samples: Int = 1024
	)

	fun focus()

	fun iconify()

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

	/**
	 * Set the window icon
	 *
	 * @param icon The pixel data for the new icon
	 */
	fun setIcon(icon: GLFWImage.Buffer)

	/**
	 * Set the window icon
	 *
	 * @param icon The input stream for the data for the new icon
	 */
	fun setIcon(icon: InputStream)
	fun setResizeCallback(callback: WindowI.() -> Unit)
	fun setFrameBufferResizeCallback(callback: WindowI.() -> Unit)
	fun setIconifyCallback(callback: WindowI.() -> Unit)
	fun setMaximiseCallback(callback: WindowI.() -> Unit)
	fun setFocusCallback(callback: WindowI.() -> Unit)
	fun setRefreshCallback(callback: WindowI.() -> Unit)
	fun setContentScaleCallback(callback: WindowI.() -> Unit)
	fun update()
	fun close(handle: Long = windowHandle)

	companion object {

		fun getSize(handle: Long): Vec2i {
			val widths = IntArray(1)
			val heights = IntArray(1)
			GLFW.glfwGetWindowSize(handle, widths, heights)

			return Vec2i(widths[0], heights[0])
		}
	}
}