package com.pineypiney.game_engine

import com.pineypiney.game_engine.audio.AudioDevice
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.Texture
import com.pineypiney.game_engine.util.input.Inputs
import glm_.d
import glm_.f
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec2.Vec2t
import kool.lib.toByteArray
import kool.toBuffer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.glViewport
import org.lwjgl.stb.STBImage
import java.io.InputStream


abstract class Window(title: String, var width: Int, var height: Int, vSync: Boolean, val version: Vec2i = Vec2i(4, 6)) {

    var vSync: Boolean = vSync
        set(value) {
            field = value
            glfwSwapInterval(field.i)
        }

    var windowHandle = 0L; private set
    var audioDevice: AudioDevice? = null; private set
    var aspectRatio = width.f/height.f

    val size: Vec2i; get() = Vec2i(width, height)
    abstract val input: Inputs

    var fullScreen: Boolean
        get() = glfwGetWindowAttrib(windowHandle, GLFW_DECORATED) == 0
        set(value) {

            if(value) glfwMaximizeWindow(windowHandle)
            else glfwRestoreWindow(windowHandle)

            glfwSetWindowAttrib(windowHandle, GLFW_DECORATED, if(value) 0  else 1)
        }

    val defaultResizeCallback = { width: Int, height: Int ->
        this.width = width
        this.height = height
        aspectRatio = width.f / height
        glViewport(0, 0, width, height)
    }

    init{
        loadGL(title)
        loadAL()

        glfwSetWindowCloseCallback(windowHandle, ::close)
    }

    fun loadGL(title: String){
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        glfwDefaultWindowHints() // optional, the current window hints are already the default

        glfwWindowHint(GLFW_VISIBLE, 0) // the window will stay hidden after creation

        glfwWindowHint(GLFW_RESIZABLE, 1) // the window will be resizable

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, version.x)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, version.y)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1)

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title, 0, 0)
        if (windowHandle == 0L) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        // Get the resolution of the primary monitor
        val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        // Center our window
        vidmode?.let {
            glfwSetWindowPos(
                windowHandle,
                (it.width() - width) / 2,
                (it.height() - height) / 2
            )
        }


        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle)
        // Make the window visible
        glfwShowWindow(windowHandle)

        if (vSync) {
            // Enable v-sync
            glfwSwapInterval(1)
        }

        GL.createCapabilities()
    }

    fun loadAL(){
        //val devices = ALUtil.getStringList(0L, ALC10.ALC_DEVICE_SPECIFIER)
        audioDevice = AudioDevice()

        val cCaps = ALC.getCapabilities()
        AL.createCapabilities(cCaps)
    }

    fun setTitle(title: String){
        glfwSetWindowTitle(windowHandle, title)
    }

    fun setSize(width: Int, height: Int){
        glfwSetWindowSize(windowHandle, width, height)
    }

    fun setSize(size: Vec2i){
        setSize(size.x, size.y)
    }

    /**
     * Set the cursor for the window
     *
     * @param cursor The handle for the new cursor
     */
    fun setCursor(cursor: Long){
        glfwSetCursor(windowHandle, cursor)
    }

    /**
     * Set the cursor to a standard cursor defined by GLFW
     *
     * @param cursor One of the standard GLFW cursors. One of:
     * <br><table><tr><td>[GLFW_ARROW_CURSOR]</td><td>[GLFW_IBEAM_CURSOR]</td><td>[GLFW_CROSSHAIR_CURSOR]</td><td>[GLFW_POINTING_HAND_CURSOR]</td><td>[GLFW_RESIZE_EW_CURSOR]</td><td>[GLFW_RESIZE_NS_CURSOR]</td><td>[GLFW_RESIZE_NWSE_CURSOR]</td><td>[GLFW_RESIZE_NESW_CURSOR]</td><td>[GLFW_RESIZE_ALL_CURSOR]</td><td>[GLFW_NOT_ALLOWED_CURSOR]</td></tr></table>
     */
    fun setCursor(cursor: Int){
        setCursor(glfwCreateStandardCursor(cursor))
    }

    /**
     * Set the cursor to a custom cursor
     *
     * @param texture The new texture for the cursor
     * @param point The relative offset of the texture, where (0, 0) is the top left and (1, 1) is the bottom right
     */
    fun setCursor(texture: Texture, point: Vec2){
        setCursor(texture, Vec2i(point * texture.size))
    }

    /**
     * Set the cursor to a custom cursor
     *
     * @param texture The new texture for the cursor
     * @param point The pixel offset of the texture, where (0, 0) is the top left
     */
    fun setCursor(texture: Texture, point: Vec2i){
        // The data must be in RGBA 32-bit format
        val data = texture.getData().toByteArray().toList()
        val pixels = data.chunked(texture.numChannels)
        val flipped = pixels.chunked(texture.width).reversed().flatten()
        val rgba = flipped.flatMap { p -> List(4){ p.getOrElse(it){-1} } }

        val image = GLFWImage.create().set(texture.width, texture.height, rgba.toByteArray().toBuffer())
        val handle = glfwCreateCursor(image, point.x, point.y)
        setCursor(handle)
    }

    /**
     * Set the position of the cursor in the window
     *
     * @param x The horizontal position of the cursor, in pixels from the left
     * @param y The vertical position of the cursor, in pixels from the top
     */
    fun setCursorPos(x: Number, y: Number){
        glfwSetCursorPos(windowHandle, x.d, y.d)
    }

    /**
     * Set the position of the cursor
     *
     * @param pos The position of the cursor, in pixels from the top left
     */
    fun setCursorPos(pos: Vec2t<*>){
        setCursorPos(pos.x, pos.y)
    }

    /**
     * Set the window icon
     *
     * @param icon The pixel data for the new icon
     */
    fun setIcon(icon: GLFWImage.Buffer){
        glfwSetWindowIcon(windowHandle, icon)
    }

    /**
     * Set the window icon
     *
     * @param icon The input stream for the data for the new icon
     */
    fun setIcon(icon: InputStream){
        var iconByteBuffer = ResourcesLoader.ioResourceToByteBuffer(icon, 1024)
        val width = IntArray(1)
        val height = IntArray(1)
        val channel = IntArray(1)
        STBImage.stbi_set_flip_vertically_on_load(false)
        iconByteBuffer = STBImage.stbi_load_from_memory(iconByteBuffer, width, height, channel, 0) ?: return

        val iconBuffer = GLFWImage.create(1)
        val iconImage = GLFWImage.create().set(width[0], height[0], iconByteBuffer)
        iconBuffer.put(0, iconImage)
        setIcon(iconBuffer)

        STBImage.stbi_image_free(iconByteBuffer)
    }

    fun setResizeCallback(callback: (Window) -> Unit){
        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle) { _: Long, width: Int, height: Int ->
            defaultResizeCallback(width, height)
            callback(this)
        }
    }

    open fun update(){
        glfwSwapBuffers(windowHandle)
        glfwPollEvents()
    }

    fun windowShouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }

    fun setShouldClose(close: Boolean = true){
        glfwSetWindowShouldClose(windowHandle, close)
    }

    open fun close(handle: Long = windowHandle){
        audioDevice?.close()
    }

    companion object {

        fun getSize(handle: Long): Vec2i{
            val widths = IntArray(1)
            val heights = IntArray(1)
            glfwGetWindowSize(handle, widths, heights)

            return Vec2i(widths[0], heights[0])
        }
    }
}