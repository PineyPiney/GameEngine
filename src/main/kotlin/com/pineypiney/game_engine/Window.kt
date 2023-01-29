package com.pineypiney.game_engine

import com.pineypiney.game_engine.audio.AudioDevice
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import glm_.bool
import glm_.f
import glm_.i
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import kool.DoubleBuffer
import kool.IntBuffer
import kool.lib.toList
import kool.toBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.opengl.GL
import org.lwjgl.stb.STBImage
import java.io.InputStream
import java.nio.DoubleBuffer
import java.nio.IntBuffer

abstract class Window(title: String, width: Int, height: Int, fullScreen: Boolean, vSync: Boolean, val version: Vec2i = Vec2i(4, 6), samples: Int = 1): WindowI {

    override var vSync: Boolean = vSync
        set(value) {
            field = value
            GLFW.glfwSwapInterval(field.i)
        }

    final override val windowHandle: Long = loadGL(title, width, height, fullScreen, samples)
    final override var audioDevice: AudioDevice? = null

    override var title: String = title
        set(value) {
            field = value
            GLFW.glfwSetWindowTitle(windowHandle, value)
        }

    override var pos: Vec2i
        get() = getVec2i(GLFW::glfwGetWindowPos)
        set(value) = setVec2i(GLFW::glfwSetWindowPos, value)

    override var size: Vec2i
        get() = getVec2i(GLFW::glfwGetWindowSize)
        set(value) = setVec2i(GLFW::glfwSetWindowSize, value)

    override val frameSize: Vec2i
        get() = getVec2i(GLFW::glfwGetFramebufferSize)

    override var cursorPos: Vec2d
        get() = getVec2d(GLFW::glfwGetCursorPos)
        set(value) = setVec2d(GLFW::glfwSetCursorPos, value)

    override var width: Int
        get() = size.x
        set(value) { size = Vec2i(value, size.y) }

    override var height: Int
        get() = size.y
        set(value) { size = Vec2i(size.x, value) }

    override val aspectRatio get() = width.f/height

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
            if(value) GLFW.glfwMaximizeWindow(windowHandle)
            else GLFW.glfwRestoreWindow(windowHandle)
        }

    override val fullScreen: Boolean
        get() = GLFW.glfwGetWindowMonitor(windowHandle) != 0L

    override var shouldClose: Boolean
        get() = GLFW.glfwWindowShouldClose(windowHandle)
        set(value) = GLFW.glfwSetWindowShouldClose(windowHandle, value)

    override var monitor: Monitor?
        get() = GLFW.glfwGetWindowMonitor(windowHandle).let { if(it == 0L) null else Monitor(it) }
        set(value) {
            GLFW.glfwSetWindowMonitor(windowHandle, value?.handle ?: 0L, 0, 0, width, height, GLFW.GLFW_DONT_CARE)
            center()
        }
    override val videoMode: GLFWVidMode get() = (monitor ?: Monitor.primary).videoMode

    init{
        loadAL()

        GLFW.glfwSetWindowCloseCallback(windowHandle, ::close)
    }

    private fun loadGL(title: String, width: Int, height: Int, fullScreen: Boolean, samples: Int): Long{

        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, 0) // the window will stay hidden after creation

        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, 1) // the window will be resizable

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, version.x)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, version.y)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, 1)

        if(samples > 1) GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples)

        // Create the window
        val monitor = if(fullScreen) GLFW.glfwGetPrimaryMonitor() else 0L

        val windowHandle = GLFW.glfwCreateWindow(width, height, title, monitor, 0)
        if (windowHandle == 0L) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(windowHandle)
        // Make the window visible
        GLFW.glfwShowWindow(windowHandle)

        GL.createCapabilities()

        if (vSync) {
            // Enable v-sync
            GLFW.glfwSwapInterval(1)
        }

        return windowHandle
    }

    private fun loadAL(name: String? = null){
        setAudioOutput(name)

        val cCaps = ALC.getCapabilities()
        AL.createCapabilities(cCaps)
    }

    override fun setAudioOutput(name: String?){
        audioDevice?.close()
        audioDevice = AudioDevice(name)
    }

    override fun focus() {
        GLFW.glfwFocusWindow(windowHandle)
    }

    override fun iconify() {
        GLFW.glfwIconifyWindow(windowHandle)
    }

    fun getVec2i(func: (Long, IntBuffer, IntBuffer) -> Unit): Vec2i{
        val (wa, ha) = arrayOf(IntBuffer(1), IntBuffer(1))
        func(windowHandle, wa, ha)
        return Vec2i(wa[0], ha[0])
    }

    fun setVec2i(func: (Long, Int, Int) -> Unit, value: Vec2i){
        func(windowHandle, value.x, value.y)
    }

    fun getVec2d(func: (Long, DoubleBuffer, DoubleBuffer) -> Unit): Vec2d {
        val (wa, ha) = arrayOf(DoubleBuffer(1), DoubleBuffer(1))
        func(windowHandle, wa, ha)
        return Vec2d(wa[0], ha[0])
    }

    fun setVec2d(func: (Long, Double, Double) -> Unit, value: Vec2d){
        func(windowHandle, value.x, value.y)
    }

    fun center(){
        videoMode.let {
            pos = Vec2i(it.width() - width, it.height() - height) / 2
        }
    }

    /**
     * Set the cursor for the window
     *
     * @param cursor The handle for the new cursor
     */
    override fun setCursor(cursor: Long){
        if(System.getProperty("os.name").contains("Windows")) GLFW.glfwSetCursor(windowHandle, cursor)
    }

    /**
     * Set the cursor to a standard cursor defined by GLFW
     *
     * @param cursor One of the standard GLFW cursors. One of:
     * <br><table><tr><td>[GLFW.GLFW_ARROW_CURSOR]</td><td>[GLFW.GLFW_IBEAM_CURSOR]</td><td>[GLFW.GLFW_CROSSHAIR_CURSOR]</td><td>[GLFW.GLFW_POINTING_HAND_CURSOR]</td><td>[GLFW.GLFW_RESIZE_EW_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NS_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NWSE_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NESW_CURSOR]</td><td>[GLFW.GLFW_RESIZE_ALL_CURSOR]</td><td>[GLFW.GLFW_NOT_ALLOWED_CURSOR]</td></tr></table>
     */
    override fun setCursor(cursor: Int){
        setCursor(GLFW.glfwCreateStandardCursor(cursor))
    }

    /**
     * Set the cursor to a custom cursor
     *
     * @param texture The new texture for the cursor
     * @param point The pixel offset of the texture, where (0, 0) is the top left
     */
    override fun setCursor(texture: InputStream, point: Vec2i){
        // The data must be in RGBA 32-bit format
        val bytes = ResourcesLoader.ioResourceToByteBuffer(texture, 2048)
        val (data, info) = TextureLoader.loadImageFromMemory(bytes)
        val pixels = data?.toList()?.chunked(info.z) ?: return
        val flipped = pixels.chunked(info.x).reversed().flatten()
        val rgba = flipped.flatMap { p -> List(4){ p.getOrElse(it){-1} } }

        val image = GLFWImage.create().set(info.x, info.y, rgba.toByteArray().toBuffer())
        val handle = GLFW.glfwCreateCursor(image, point.x, point.y)
        setCursor(handle)
    }

    /**
     * Set the window icon
     *
     * @param icon The pixel data for the new icon
     */
    override fun setIcon(icon: GLFWImage.Buffer){
        if(System.getProperty("os.name").contains("Windows")){
            GLFW.glfwSetWindowIcon(windowHandle, icon)
        }
    }

    /**
     * Set the window icon
     *
     * @param icon The input stream for the data for the new icon
     */
    override fun setIcon(icon: InputStream){
        val iconByteBuffer = ResourcesLoader.ioResourceToByteBuffer(icon, 1024)
        val (loadedBuffer, v) = TextureLoader.loadImageFromMemory(iconByteBuffer, false)
        if(loadedBuffer == null) return

        val iconBuffer = GLFWImage.create(1)
        val iconImage = GLFWImage.create().set(v.x, v.y, loadedBuffer)
        iconBuffer.put(0, iconImage)
        setIcon(iconBuffer)

        STBImage.stbi_image_free(loadedBuffer)
    }

    override fun setResizeCallback(callback: WindowI.() -> Unit){
        // Setup resize callback
        GLFW.glfwSetWindowSizeCallback(windowHandle) { _: Long, _: Int, _: Int ->
            this.callback()
        }
    }

    override fun setFrameBufferResizeCallback(callback: WindowI.() -> Unit) {
        GLFW.glfwSetFramebufferSizeCallback(windowHandle) { _: Long, _: Int, _: Int ->
            this.callback()
        }
    }

    override fun setIconifyCallback(callback: WindowI.() -> Unit){
        GLFW.glfwSetWindowIconifyCallback(windowHandle){ _: Long, _: Boolean ->
            this.callback()
        }
    }

    override fun setMaximiseCallback(callback: WindowI.() -> Unit) {
        GLFW.glfwSetWindowMaximizeCallback(windowHandle){ _: Long, _: Boolean ->
            this.callback()
        }
    }

    override fun setFocusCallback(callback: WindowI.() -> Unit) {
        GLFW.glfwSetWindowFocusCallback(windowHandle){ _: Long, _: Boolean ->
            this.callback()
        }
    }

    override fun setRefreshCallback(callback: WindowI.() -> Unit) {
        GLFW.glfwSetWindowRefreshCallback(windowHandle){ _: Long ->
            this.callback()
        }
    }

    override fun update(){
        GLFW.glfwSwapBuffers(windowHandle)
        GLFW.glfwPollEvents()
    }

    override fun close(handle: Long){
        audioDevice?.close()
    }
}