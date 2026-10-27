package com.pineypiney.game_engine.window

import com.pineypiney.game_engine.resources.ResourcesLoader
import com.pineypiney.game_engine.resources.textures.TextureLoader
import glm_.vec2.Vec2i
import kool.free
import kool.toBuffer
import kool.toByteArray
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import java.io.InputStream

class GLFWCursor(override val handle: Long) : Cursor {

	constructor(texture: InputStream, point: Vec2i) : this(createHandle(texture, point))

	override fun delete() = GLFW.glfwDestroyCursor(handle)

	companion object {
		fun createHandle(texture: InputStream, point: Vec2i): Long {
			// The data must be in RGBA 32-bit format
			val bytes = ResourcesLoader.ioResourceToByteBuffer(texture)
			val (data, info) = TextureLoader.loadTextureData(bytes)
			bytes.free()
			val pixels = data?.toByteArray()?.toList()?.chunked(info.z) ?: return 0L
			val flipped = pixels.chunked(info.x).reversed().flatten()
			val rgba = flipped.flatMap { p -> List(4) { p.getOrElse(it) { -1 } } }

			val image = GLFWImage.create().set(info.x, info.y, rgba.toByteArray().toBuffer())
			return GLFW.glfwCreateCursor(image, point.x, point.y)
		}
	}
}