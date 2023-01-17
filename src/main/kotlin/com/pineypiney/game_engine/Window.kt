package com.pineypiney.game_engine

import com.pineypiney.game_engine.audio.AudioDevice
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import com.pineypiney.game_engine.util.input.Inputs
import glm_.f
import glm_.i
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i
import kool.lib.toList
import kool.toBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.opengl.GL
import org.lwjgl.stb.STBImage
import java.io.InputStream


abstract class Window(title: String, width: Int, height: Int, vSync: Boolean, val version: Vec2i = Vec2i(4, 6), samples: Int = 1) {

    abstract val input: Inputs

    var windowHandle = 0L; private set
    var audioDevice: AudioDevice? = null; private set

    var title: String = title
    set(value) {
        field = value
        GLFW.glfwSetWindowTitle(windowHandle, value)
    }

    var pos: Vec2i
    get() = getVec2i(GLFW::glfwGetWindowPos)
    set(value) = setVec2i(GLFW::glfwSetWindowPos, value)

    var size: Vec2i
    get() = getVec2i(GLFW::glfwGetWindowSize)
    set(value) = setVec2i(GLFW::glfwSetWindowSize, value)

    val frameSize: Vec2i
    get() = getVec2i(GLFW::glfwGetFramebufferSize)

    var cursorPos: Vec2d
    get() = getVec2d(GLFW::glfwGetCursorPos)
    set(value) = setVec2d(GLFW::glfwSetCursorPos, value)

    var width: Int
    get() = size.x
    set(value) { size = Vec2i(value, size.y) }

    var height: Int
    get() = size.y
    set(value) { size = Vec2i(size.x, value) }

    val aspectRatio get() = width.f/height

    var fullScreen: Boolean
        get() = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED) == 0
        set(value) {

            if(value) {
                // Must be done in this order so that it fills the whole screen
                // and does not leave a bar at the top where the decoration was
                GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED, 0)
                GLFW.glfwMaximizeWindow(windowHandle)
            }
            else {
                // Must be done in reverse order so that the window does not shrink
                GLFW.glfwRestoreWindow(windowHandle)
                GLFW.glfwSetWindowAttrib(windowHandle, GLFW.GLFW_DECORATED, 1)
            }
        }

    var shouldClose: Boolean
    get() = GLFW.glfwWindowShouldClose(windowHandle)
    set(value) = GLFW.glfwSetWindowShouldClose(windowHandle, value)

    var vSync: Boolean = vSync
        set(value) {
            field = value
            GLFW.glfwSwapInterval(field.i)
        }

    init{
        loadGL(title, width, height, samples)
        loadAL()

        GLFW.glfwSetWindowCloseCallback(windowHandle, ::close)
    }

    fun loadGL(title: String, width: Int, height: Int, samples: Int){
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, 0) // the window will stay hidden after creation

        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, 1) // the window will be resizable

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, version.x)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, version.y)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, 1)

        if(samples > 1) GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, samples)

        // Create the window
        windowHandle = GLFW.glfwCreateWindow(width, height, title, 0, 0)
        if (windowHandle == 0L) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Get the resolution of the primary monitor
        val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
        // Center our window
        vidmode?.let {
            GLFW.glfwSetWindowPos(
                windowHandle,
                (it.width() - width) / 2,
                (it.height() - height) / 2
            )
        }


        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(windowHandle)
        // Make the window visible
        GLFW.glfwShowWindow(windowHandle)

        if (vSync) {
            // Enable v-sync
            GLFW.glfwSwapInterval(1)
        }

        GL.createCapabilities()
    }

    fun loadAL(){
        //val devices = ALUtil.getStringList(0L, ALC10.ALC_DEVICE_SPECIFIER)
        audioDevice = AudioDevice()

        val cCaps = ALC.getCapabilities()
        AL.createCapabilities(cCaps)
    }

    fun getVec2i(func: (Long, IntArray, IntArray) -> Unit): Vec2i{
        val (wa, ha) = arrayOf(IntArray(1), IntArray(1))
        func(windowHandle, wa, ha)
        return Vec2i(wa[0], ha[0])
    }

    fun setVec2i(func: (Long, Int, Int) -> Unit, value: Vec2i){
        func(windowHandle, value.x, value.y)
    }

    fun getVec2d(func: (Long, DoubleArray, DoubleArray) -> Unit): Vec2d{
        val (wa, ha) = arrayOf(DoubleArray(1), DoubleArray(1))
        func(windowHandle, wa, ha)
        return Vec2d(wa[0], ha[0])
    }

    fun setVec2d(func: (Long, Double, Double) -> Unit, value: Vec2d){
        func(windowHandle, value.x, value.y)
    }

    /**
     * Set the cursor for the window
     *
     * @param cursor The handle for the new cursor
     */
    fun setCursor(cursor: Long){
        if(System.getProperty("os.name").contains("Windows")) GLFW.glfwSetCursor(windowHandle, cursor)
    }

    /**
     * Set the cursor to a standard cursor defined by GLFW
     *
     * @param cursor One of the standard GLFW cursors. One of:
     * <br><table><tr><td>[GLFW.GLFW_ARROW_CURSOR]</td><td>[GLFW.GLFW_IBEAM_CURSOR]</td><td>[GLFW.GLFW_CROSSHAIR_CURSOR]</td><td>[GLFW.GLFW_POINTING_HAND_CURSOR]</td><td>[GLFW.GLFW_RESIZE_EW_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NS_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NWSE_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NESW_CURSOR]</td><td>[GLFW.GLFW_RESIZE_ALL_CURSOR]</td><td>[GLFW.GLFW_NOT_ALLOWED_CURSOR]</td></tr></table>
     */
    fun setCursor(cursor: Int){
        setCursor(GLFW.glfwCreateStandardCursor(cursor))
    }

    /**
     * Set the cursor to a custom cursor
     *
     * @param texture The new texture for the cursor
     * @param point The pixel offset of the texture, where (0, 0) is the top left
     */
    fun setCursor(texture: InputStream, point: Vec2i){
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
    fun setIcon(icon: GLFWImage.Buffer){
        if(System.getProperty("os.name").contains("Windows")){
            GLFW.glfwSetWindowIcon(windowHandle, icon)
        }
    }

    /**
     * Set the window icon
     *
     * @param icon The input stream for the data for the new icon
     */
    fun setIcon(icon: InputStream){
        val iconByteBuffer = ResourcesLoader.ioResourceToByteBuffer(icon, 1024)
        val (loadedBuffer, v) = TextureLoader.loadImageFromMemory(iconByteBuffer, false)
        if(loadedBuffer == null) return

        val iconBuffer = GLFWImage.create(1)
        val iconImage = GLFWImage.create().set(v.x, v.y, loadedBuffer)
        iconBuffer.put(0, iconImage)
        setIcon(iconBuffer)

        STBImage.stbi_image_free(loadedBuffer)
    }

    fun setResizeCallback(callback: (Window) -> Unit){
        // Setup resize callback
        GLFW.glfwSetFramebufferSizeCallback(windowHandle) { _: Long, _: Int, _: Int ->
            callback(this)
        }
    }

    open fun update(){
        GLFW.glfwSwapBuffers(windowHandle)
        GLFW.glfwPollEvents()
    }

    open fun close(handle: Long = windowHandle){
        audioDevice?.close()
    }

    companion object {

        fun getSize(handle: Long): Vec2i{
            val widths = IntArray(1)
            val heights = IntArray(1)
            GLFW.glfwGetWindowSize(handle, widths, heights)

            return Vec2i(widths[0], heights[0])
        }
    }
}