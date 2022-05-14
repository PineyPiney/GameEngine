package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourceLoader
import com.pineypiney.game_engine.util.input.Inputs
import glm_.f
import glm_.i
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL
import org.lwjgl.stb.STBImage


class Window(title: String, var width: Int, var height: Int, vSync: Boolean) {

    var vSync: Boolean = vSync
        set(value) {
            field = value
            glfwSwapInterval(field.i)
        }

    var windowHandle = 0L; private set
    var aspectRatio = width.f/height.f

    val size: Vec2i; get() = Vec2i(this.width, this.height)
    val input: Inputs

    var fullScreen = false
        set(value) {
            field = value
            if(field) {
                glfwSetWindowAttrib(this.windowHandle, GLFW_DECORATED, GLFW_FALSE)
                glfwMaximizeWindow(this.windowHandle)
            }
            else {
                glfwSetWindowAttrib(this.windowHandle, GLFW_DECORATED, GLFW_TRUE)
                glfwRestoreWindow(this.windowHandle)
            }
        }

    init{
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

        // input must be set after the windowHandle has been set so that the callbacks are assigned correctly
        input = Inputs(this)

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

        windows[windowHandle] = this
    }

    fun setSize(width: Int, height: Int){
        glfwSetWindowSize(this.windowHandle, width, height)
    }

    fun setSize(size: Vec2i){
        setSize(size.x, size.y)
    }

    fun setTitle(title: String){
        glfwSetWindowTitle(this.windowHandle, title)
    }

    fun setIcon(icon: GLFWImage.Buffer){
        glfwSetWindowIcon(this.windowHandle, icon)
    }

    fun setIcon(icon: String){
        var iconByteBuffer = ResourceLoader.ioResourceToByteBuffer(ResourceLoader.getStream(icon), 1024)
        val width = IntArray(1)
        val height = IntArray(1)
        val channel = IntArray(1)
        iconByteBuffer = STBImage.stbi_load_from_memory(iconByteBuffer, width, height, channel, 0) ?: return

        val iconBuffer = GLFWImage.create(1)
        val iconImage = GLFWImage.create().set(width[0], height[0], iconByteBuffer)
        iconBuffer.put(0, iconImage)
        setIcon(iconBuffer)
    }

    // gameEngine.activeScreen.updateAspectRatio(this)
    fun setResizeCallback(callback: (Window) -> Unit){
        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle) { _: Long, width: Int, height: Int ->
            this.width = width
            this.height = height
            this.aspectRatio = width.f/height

            callback(this)
        }

    }

    fun update(){
        glfwSwapBuffers(windowHandle)
        glfwPollEvents()
    }

    fun windowShouldClose(): Boolean {
        return glfwWindowShouldClose(windowHandle)
    }

    fun setShouldClose(close: Boolean = true){
        glfwSetWindowShouldClose(windowHandle, close)
    }

    companion object {
        var windows = mutableMapOf<Long, Window>()

        val INSTANCE: Window = Window("GAME", 960, 540, false)

        fun getWindow(handle: Long): Window = windows[handle] ?: INSTANCE

    }
}