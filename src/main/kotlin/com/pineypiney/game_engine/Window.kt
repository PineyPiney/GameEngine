package com.pineypiney.game_engine

import com.pineypiney.game_engine.audio.AudioDevice
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.input.Inputs
import glm_.d
import glm_.f
import glm_.i
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.glViewport
import org.lwjgl.stb.STBImage
import java.io.InputStream


abstract class Window(title: String, var width: Int, var height: Int, vSync: Boolean) {

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

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)
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

    fun setCursor(cursor: Long){
        glfwSetCursor(windowHandle, cursor)
    }

    fun setCursor(cursor: Int){
        setCursor(glfwCreateStandardCursor(cursor))
    }

    fun setCursorPos(x: Number, y: Number){
        glfwSetCursorPos(windowHandle, x.d, y.d)
    }

    fun setCursorPos(pos: Vec2i){
        setCursorPos(pos.x, pos.y)
    }

    fun setIcon(icon: GLFWImage.Buffer){
        glfwSetWindowIcon(windowHandle, icon)
    }

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