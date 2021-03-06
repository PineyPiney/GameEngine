package com.pineypiney.game_engine

import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.util.input.Inputs
import glm_.d
import glm_.f
import glm_.i
import glm_.vec2.Vec2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
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
    var aspectRatio = width.f/height.f

    val size: Vec2i; get() = Vec2i(this.width, this.height)
    abstract val input: Inputs

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

    val defaultResizeCallback = {_: Long, width: Int, height: Int ->
        this.width = width
        this.height = height
        this.aspectRatio = width.f / height
        glViewport(0, 0, width, height)
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
        glfwSetWindowSize(windowHandle, width, height)
    }

    fun setSize(size: Vec2i){
        setSize(size.x, size.y)
    }

    fun setTitle(title: String){
        glfwSetWindowTitle(windowHandle, title)
    }

    fun setCursor(x: Number, y: Number){
        glfwSetCursorPos(windowHandle, x.d, y.d)
    }

    fun setCursor(pos: Vec2i){
        setCursor(pos.x, pos.y)
    }

    fun setIcon(icon: GLFWImage.Buffer){
        glfwSetWindowIcon(this.windowHandle, icon)
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

    // gameEngine.activeScreen.updateAspectRatio(this)
    fun setResizeCallback(callback: (Window) -> Unit){
        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle) { handle: Long, width: Int, height: Int ->
            defaultResizeCallback(handle, width, height)
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

        operator fun get(handle: Long): Window = windows.getValue(handle)
    }
}