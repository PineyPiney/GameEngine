package com.pineypiney.game_engine.util

import com.pineypiney.game_engine.GameEngineI
import com.pineypiney.game_engine.objects.Deleteable
import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import glm_.vec2.Vec2i
import kool.toBuffer
import kool.toByteArray
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import java.io.InputStream

class Cursor(val handle: Long): Deleteable {

    constructor(texture: InputStream, point: Vec2i): this(createCursor(texture, point))

    constructor(engine: GameEngineI<*>, texture: String, point: Vec2i): this(engine.resourcesLoader.getStream(texture)!!, point)

    /**
     * Create a standard cursor
     *
     * @param shape One of the standard GLFW cursors. One of:
     * <br><table><tr><td>[GLFW.GLFW_ARROW_CURSOR]</td><td>[GLFW.GLFW_IBEAM_CURSOR]</td><td>[GLFW.GLFW_CROSSHAIR_CURSOR]</td><td>[GLFW.GLFW_POINTING_HAND_CURSOR]</td><td>[GLFW.GLFW_RESIZE_EW_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NS_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NWSE_CURSOR]</td><td>[GLFW.GLFW_RESIZE_NESW_CURSOR]</td><td>[GLFW.GLFW_RESIZE_ALL_CURSOR]</td><td>[GLFW.GLFW_NOT_ALLOWED_CURSOR]</td></tr></table>
     */
    constructor(shape: Int): this(GLFW.glfwCreateStandardCursor(shape))

    override fun delete() = GLFW.glfwDestroyCursor(handle)

    companion object{
        fun createCursor(texture: InputStream, point: Vec2i): Long{
            // The data must be in RGBA 32-bit format
            val bytes = ResourcesLoader.ioResourceToByteBuffer(texture, 2048)
            val (data, info) = TextureLoader.loadImageFromMemory(bytes)
            val pixels = data?.toByteArray()?.toList()?.chunked(info.z) ?: return 0L
            val flipped = pixels.chunked(info.x).reversed().flatten()
            val rgba = flipped.flatMap { p -> List(4){ p.getOrElse(it){-1} } }

            val image = GLFWImage.create().set(info.x, info.y, rgba.toByteArray().toBuffer())
            return GLFW.glfwCreateCursor(image, point.x, point.y)
        }
    }
}